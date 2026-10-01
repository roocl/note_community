package org.notes.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.UUID;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.notes.config.RabbitMQConfig;
import org.notes.mapper.MessageMapper;
import org.notes.mapper.OutboxMapper;
import org.notes.mapper.UserMapper;
import org.notes.model.dto.user.RegisterRequest;
import org.notes.model.entity.User;
import org.notes.model.enums.message.MessageType;
import org.notes.model.enums.redisKey.RedisKey;
import org.notes.repository.UserSearchRepository;
import org.notes.scope.RequestScopeData;
import org.notes.service.impl.EmailServiceImpl;
import org.notes.service.impl.MessageServiceImpl;
import org.notes.service.impl.UserServiceImpl;
import org.notes.task.email.WelcomeEmailTask;
import org.notes.task.notification.NotificationTask;
import org.notes.task.notification.NotificationTaskConsumer;
import org.notes.utils.JwtUtil;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@EnabledIfSystemProperty(named = "integration.real", matches = "true")
class OutboxIntegrationTest {
    private JdbcTemplate admin;
    private String database;
    private TransactionTemplate transaction;
    private OutboxService outbox;
    private JdbcTemplate jdbc;
    private OutboxMapper mapper;
    private DataSourceTransactionManager manager;

    @BeforeEach
    void setUp() throws Exception {
        String password = System.getenv("DB_PASSWORD");
        assertNotNull(password);
        String username = System.getenv().getOrDefault("DB_USERNAME", "root");
        admin = new JdbcTemplate(new DriverManagerDataSource("jdbc:mysql://localhost:3306/mysql", username, password));
        database = "note_outbox_" + UUID.randomUUID().toString().replace("-", "");
        admin.execute("CREATE DATABASE " + database);
        DriverManagerDataSource source = new DriverManagerDataSource("jdbc:mysql://localhost:3306/" + database, username, password);
        new ResourceDatabasePopulator(new ClassPathResource("db/outbox.sql")).execute(source);
        manager = new DataSourceTransactionManager(source);
        transaction = new TransactionTemplate(manager);
        jdbc = new JdbcTemplate(source);
        var factory = new SqlSessionFactoryBean();
        factory.setDataSource(source);
        factory.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath:mapper/OutboxMapper.xml"));
        mapper = new SqlSessionTemplate(factory.getObject()).getMapper(OutboxMapper.class);
        outbox = new OutboxService(mapper, new ObjectMapper(), manager);
    }

    @AfterEach
    void cleanUp() {
        if (database != null) admin.execute("DROP DATABASE " + database);
    }

    @Test
    void brokerFailureCanBeRetriedByNewWorkerWithStableEventId() {
        NotificationTask task = new NotificationTask();
        transaction.executeWithoutResult(status -> outbox.record(task));
        RabbitTemplate rabbit = mock(RabbitTemplate.class);
        doThrow(new IllegalStateException("broker offline"))
                .when(rabbit).send(anyString(), anyString(),
                        any(Message.class),
                        any(CorrelationData.class));
        new OutboxDispatcher(outbox, new ReliableRabbitPublisher(rabbit)).dispatch();
        assertTrue(outbox.claim().isEmpty());
        jdbc.update("UPDATE outbox_event SET available_at=TIMESTAMPADD(SECOND,-1,CURRENT_TIMESTAMP(6))");
        reset(rabbit);
        List<Message> messages = new ArrayList<>();
        doAnswer(call -> {
            messages.add(call.getArgument(2));
            CorrelationData correlation = call.getArgument(3);
            correlation.getFuture().set(new CorrelationData.Confirm(true, null));
            return null;
        }).when(rabbit).send(anyString(), anyString(),
                any(Message.class),
                any(CorrelationData.class));
        new OutboxDispatcher(new OutboxService(mapper, new ObjectMapper(), manager), new ReliableRabbitPublisher(rabbit)).dispatch();
        assertEquals(1, messages.size());
        assertEquals(task.getEventId(), messages.get(0).getMessageProperties().getMessageId());
        assertEquals(MessageDeliveryMode.PERSISTENT, messages.get(0).getMessageProperties().getDeliveryMode());
        assertTrue(outbox.claim().isEmpty());
    }

    @Test
    void rollbackDoesNotPublishAndCommittedEventSurvivesNewWorker() {
        NotificationTask task = new NotificationTask();
        task.setReceiverId(3L);
        transaction.executeWithoutResult(status -> {
            outbox.record(task);
            status.setRollbackOnly();
        });
        assertTrue(outbox.claim().isEmpty());
        transaction.executeWithoutResult(status -> outbox.record(task));
        OutboxService.Delivery delivery = new OutboxService(mapper, new ObjectMapper(), manager).claim().orElseThrow();
        assertEquals(task.getEventId(), delivery.eventId());
        assertTrue(outbox.claim().isEmpty());
        outbox.complete(delivery);
        assertTrue(outbox.claim().isEmpty());
    }

    @Test
    void expiredLeaseRecoversAndOldWorkerCannotOverwriteNewOwner() {
        NotificationTask task = new NotificationTask();
        transaction.executeWithoutResult(status -> outbox.record(task));
        var first = outbox.claim().orElseThrow();
        assertTrue(outbox.claim().isEmpty());
        jdbc.update("UPDATE outbox_event SET lease_until=TIMESTAMPADD(SECOND,-1,CURRENT_TIMESTAMP(6))");
        var second = new OutboxService(mapper, new ObjectMapper(), manager).claim().orElseThrow();
        assertEquals(first.eventId(), second.eventId());
        assertNotEquals(first.leaseToken(), second.leaseToken());
        outbox.complete(first);
        outbox.retry(first, new IllegalStateException("stale worker"));
        assertTrue(outbox.claim().isEmpty());
        outbox.retry(second, new IllegalStateException("retry current owner"));
        jdbc.update("UPDATE outbox_event SET available_at=TIMESTAMPADD(SECOND,-1,CURRENT_TIMESTAMP(6))");
        var third = outbox.claim().orElseThrow();
        assertEquals(first.eventId(), third.eventId());
        assertEquals(3, third.attempts());
        outbox.complete(third);
        assertTrue(outbox.claim().isEmpty());
    }

    @Test
    void concurrentWorkersDoNotClaimSameEvent() throws Exception {
        transaction.executeWithoutResult(status -> outbox.record(new NotificationTask()));
        var workers = Executors.newFixedThreadPool(2);
        var start = new CyclicBarrier(2);
        try {
            Callable<Optional<OutboxService.Delivery>> claim = () -> {
                start.await(5, TimeUnit.SECONDS);
                return new OutboxService(mapper, new ObjectMapper(), manager).claim();
            };
            var first = workers.submit(claim);
            var second = workers.submit(claim);
            assertEquals(1, Stream.of(first.get(10, TimeUnit.SECONDS),
                    second.get(10, TimeUnit.SECONDS)).filter(Optional::isPresent).count());
        } finally {
            workers.shutdownNow();
        }
    }

    @Test
    void eventRequiresBusinessTransaction() {
        assertThrows(IllegalStateException.class, () -> outbox.record(new NotificationTask()));
        transaction.setReadOnly(true);
        assertThrows(IllegalStateException.class, () -> transaction.executeWithoutResult(status -> outbox.record(new NotificationTask())));
        assertTrue(outbox.claim().isEmpty());
    }    @Test
    @EnabledIfSystemProperty(named = "integration.rabbit", matches = "true")
    void ackBeforeWorkerCrashCanRedeliverWithoutDuplicateNotification() throws Exception {
        var connection = new CachingConnectionFactory(
                System.getProperty("integration.rabbit.host", "localhost"), Integer.getInteger("integration.rabbit.port", 25672));
        connection.setPublisherConfirmType(CachingConnectionFactory.ConfirmType.CORRELATED);
        connection.setPublisherReturns(true);
        var rabbit = new RabbitTemplate(connection);
        rabbit.setMandatory(true);
        var broker = new RabbitAdmin(connection);
        String queue = RabbitMQConfig.NOTIFICATION_QUEUE;
        broker.declareQueue(new Queue(queue, true));
        try {
            NotificationTask task = new NotificationTask();
            task.setReceiverId(3L);
            task.setSenderId(1L);
            task.setType(MessageType.SYSTEM);
            task.setContent("one notification");
            transaction.executeWithoutResult(status -> outbox.record(task));
            var first = outbox.claim().orElseThrow();
            var properties = new MessageProperties();
            properties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
            properties.setMessageId(first.eventId());
            var publisher = new ReliableRabbitPublisher(rabbit);
            publisher.sendRaw(queue, new Message(first.payload().getBytes(StandardCharsets.UTF_8), properties), first.traceId());
            jdbc.update("UPDATE outbox_event SET lease_until=TIMESTAMPADD(SECOND,-1,CURRENT_TIMESTAMP(6))");
            new OutboxDispatcher(new OutboxService(mapper, new ObjectMapper(), manager), publisher).dispatch();
            jdbc.execute("CREATE TABLE message(message_id INT PRIMARY KEY AUTO_INCREMENT,event_id VARCHAR(36) UNIQUE,receiver_id BIGINT,sender_id BIGINT,type INT,target_id INT,target_type INT,content TEXT,is_read BOOLEAN,created_at DATETIME DEFAULT CURRENT_TIMESTAMP,updated_at DATETIME DEFAULT CURRENT_TIMESTAMP)");
            var factory = new SqlSessionFactoryBean();
            factory.setDataSource(jdbc.getDataSource());
            var configuration = new Configuration();
            configuration.setMapUnderscoreToCamelCase(true);
            factory.setConfiguration(configuration);
            factory.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath:mapper/MessageMapper.xml"));
            var mapper = new SqlSessionTemplate(factory.getObject()).getMapper(MessageMapper.class);
            var users = new UserServiceImpl();
            var userMapper = mock(UserMapper.class);
            var sender = new User();
            sender.setUserId(1L);
            sender.setUsername("sender");
            when(userMapper.findByIdBatch(any())).thenReturn(List.of(sender));
            ReflectionTestUtils.setField(users, "userMapper", userMapper);
            var scope = new RequestScopeData();
            scope.setUserId(3L);
            var messages = new MessageServiceImpl(mapper, users, scope, null, null, null);
            var consumer = new NotificationTaskConsumer(messages);
            for (int index = 0; index < 2; index++) {
                var received = rabbit.receive(queue, 5000);
                assertNotNull(received);
                consumer.processNotification(new ObjectMapper().readValue(received.getBody(), NotificationTask.class), null);
            }
            assertEquals(1, messages.getMessages().size());
            assertEquals("one notification", messages.getMessages().get(0).getContent());
            assertTrue(outbox.claim().isEmpty());
            assertThrows(IllegalStateException.class, () -> publisher.sendRaw("missing." + UUID.randomUUID(),
                    new Message(new byte[0], new MessageProperties()), null));
        } finally {
            broker.deleteQueue(queue);
            connection.destroy();
        }
    }    @Test
    void registrationCommitsWelcomeEventWithUserAndRollbackRemovesBoth() throws Exception {
        jdbc.execute("CREATE TABLE user(user_id BIGINT PRIMARY KEY AUTO_INCREMENT,account VARCHAR(100) UNIQUE,username VARCHAR(100),password VARCHAR(255),email VARCHAR(255) UNIQUE,last_login_at DATETIME)");
        var factory = new SqlSessionFactoryBean();
        factory.setDataSource(jdbc.getDataSource());
        var configuration = new Configuration();
        configuration.setMapUnderscoreToCamelCase(true);
        factory.setConfiguration(configuration);
        factory.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath:mapper/UserMapper.xml"));
        var usersMapper = new SqlSessionTemplate(factory.getObject()).getMapper(UserMapper.class);
        var users = new UserServiceImpl();
        var emails = new EmailServiceImpl();
        RedisTemplate<String,String> redis = mock(RedisTemplate.class);
        ValueOperations<String,String> codes = mock(ValueOperations.class);
        when(redis.opsForValue()).thenReturn(codes);
        when(codes.get(RedisKey.registerVerificationCode("new@example.com"))).thenReturn("123456");
        ReflectionTestUtils.setField(emails, "redisTemplate", redis);
        var jwt = new JwtUtil();
        ReflectionTestUtils.setField(jwt, "secret", Base64.getEncoder().encodeToString("test-signing-key".repeat(8).getBytes(StandardCharsets.UTF_8)));
        ReflectionTestUtils.setField(jwt, "expiration", 3600L);
        ReflectionTestUtils.setField(users, "userMapper", usersMapper);
        ReflectionTestUtils.setField(users, "passwordEncoder", new BCryptPasswordEncoder());
        ReflectionTestUtils.setField(users, "jwtUtil", jwt);
        ReflectionTestUtils.setField(users, "emailService", emails);
        ReflectionTestUtils.setField(users, "outboxService", outbox);
        ReflectionTestUtils.setField(users, "postCommitExecutor", new PostCommitExecutor(Runnable::run));
        ReflectionTestUtils.setField(users, "userSearchRepository", mock(UserSearchRepository.class));
        var request = new RegisterRequest();
        request.setAccount("new-user");
        request.setUsername("New User");
        request.setPassword("password");
        request.setEmail("new@example.com");
        request.setVerifyCode("123456");
        transaction.executeWithoutResult(status -> {
            users.register(request);
            status.setRollbackOnly();
        });
        assertNull(usersMapper.findByAccount("new-user"));
        assertTrue(outbox.claim().isEmpty());
        var registered = transaction.execute(status -> users.register(request));
        assertNotNull(registered);
        assertNotNull(usersMapper.findByAccount("new-user"));
        var delivery = outbox.claim().orElseThrow();
        assertEquals(RabbitMQConfig.WELCOME_EMAIL_QUEUE, delivery.queue());
        var welcome = new ObjectMapper().readValue(delivery.payload(), WelcomeEmailTask.class);
        assertEquals("new@example.com", welcome.getEmail());
        assertEquals("New User", welcome.getUsername());
        assertEquals(delivery.eventId(), welcome.getEventId());
    }

    @Test
    @EnabledIfSystemProperty(named = "integration.rabbit", matches = "true")
    void realConnectionFailureRecoversWithSameEvent() {
        var unavailable = new CachingConnectionFactory("localhost", 25674);
        unavailable.setConnectionTimeout(1000);
        unavailable.setPublisherConfirmType(CachingConnectionFactory.ConfirmType.CORRELATED);
        var recovered = new CachingConnectionFactory(
                System.getProperty("integration.rabbit.host", "localhost"), Integer.getInteger("integration.rabbit.port", 25672));
        recovered.setPublisherConfirmType(CachingConnectionFactory.ConfirmType.CORRELATED);
        recovered.setPublisherReturns(true);
        var admin = new RabbitAdmin(recovered);
        String queue = RabbitMQConfig.NOTIFICATION_QUEUE;
        admin.declareQueue(new Queue(queue, true));
        try {
            NotificationTask task = new NotificationTask();
            transaction.executeWithoutResult(status -> outbox.record(task));
            new OutboxDispatcher(outbox, new ReliableRabbitPublisher(new RabbitTemplate(unavailable))).dispatch();
            assertTrue(outbox.claim().isEmpty());
            jdbc.update("UPDATE outbox_event SET available_at=TIMESTAMPADD(SECOND,-1,CURRENT_TIMESTAMP(6))");
            var rabbit = new RabbitTemplate(recovered);
            rabbit.setMandatory(true);
            new OutboxDispatcher(new OutboxService(mapper, new ObjectMapper(), manager), new ReliableRabbitPublisher(rabbit)).dispatch();
            var received = rabbit.receive(queue, 5000);
            assertNotNull(received);
            assertEquals(task.getEventId(), received.getMessageProperties().getMessageId());
            assertTrue(outbox.claim().isEmpty());
        } finally {
            admin.deleteQueue(queue);
            unavailable.destroy();
            recovered.destroy();
        }
    }}
