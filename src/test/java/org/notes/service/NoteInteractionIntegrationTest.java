package org.notes.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.notes.mapper.*;
import org.notes.model.dto.note.NoteQueryParams;
import org.notes.model.entity.Note;
import org.notes.scope.RequestScopeData;
import org.notes.service.impl.*;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.*;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@EnabledIfSystemProperty(named = "integration.real", matches = "true")
class NoteInteractionIntegrationTest {
    private DriverManagerDataSource dataSource;
    private JdbcTemplate admin;
    private JdbcTemplate jdbc;
    private String database;
    private NoteService reads;
    private NoteLikeService likes;
    private RequestScopeData scope;
    private CollectionNoteService collections;
    private CollectionService folders;
    private CommentService comments;
    private OutboxService outbox;

    @BeforeEach
    void setUp() throws Exception {
        String username = System.getenv().getOrDefault("DB_USERNAME", "root");
        String password = System.getenv("DB_PASSWORD");
        assertNotNull(password, "真实测试必须设置 DB_PASSWORD");
        admin = new JdbcTemplate(new DriverManagerDataSource("jdbc:mysql://localhost:3306/mysql", username, password));
        database = "note_correctness_" + UUID.randomUUID().toString().replace("-", "");
        admin.execute("CREATE DATABASE " + database);
        dataSource = new DriverManagerDataSource("jdbc:mysql://localhost:3306/" + database
                + "?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai", username, password);
        jdbc = new JdbcTemplate(dataSource);
        jdbc.execute("CREATE TABLE note (note_id INT PRIMARY KEY, author_id BIGINT, question_id INT, content TEXT, search_vector TEXT, like_count INT NOT NULL DEFAULT 0, collect_count INT NOT NULL DEFAULT 0, comment_count INT NOT NULL DEFAULT 0, created_at DATETIME DEFAULT CURRENT_TIMESTAMP, updated_at DATETIME DEFAULT CURRENT_TIMESTAMP)");
        jdbc.execute("CREATE TABLE comment (comment_id INT PRIMARY KEY AUTO_INCREMENT, note_id INT, author_id BIGINT, parent_id INT, content TEXT, like_count INT DEFAULT 0, reply_count INT DEFAULT 0, created_at DATETIME DEFAULT CURRENT_TIMESTAMP, updated_at DATETIME DEFAULT CURRENT_TIMESTAMP)");
        jdbc.execute("CREATE TABLE note_like (user_id BIGINT NOT NULL, note_id INT NOT NULL, PRIMARY KEY(user_id,note_id))");
        jdbc.execute("CREATE TABLE collection (collection_id INT PRIMARY KEY, creator_id BIGINT, name VARCHAR(100), description TEXT, created_at DATETIME DEFAULT CURRENT_TIMESTAMP, updated_at DATETIME DEFAULT CURRENT_TIMESTAMP)");
        jdbc.execute("CREATE TABLE collection_note (collection_id INT NOT NULL, note_id INT NOT NULL, created_at DATETIME DEFAULT CURRENT_TIMESTAMP, updated_at DATETIME DEFAULT CURRENT_TIMESTAMP, PRIMARY KEY(collection_id,note_id))");
        jdbc.execute("INSERT INTO collection(collection_id,creator_id,name) VALUES(1,1,'one'),(2,1,'two')");
        jdbc.execute("INSERT INTO note(note_id,author_id,question_id,content) VALUES(7,3,10,'example')");
        Configuration configuration = new Configuration();
        configuration.setMapUnderscoreToCamelCase(true);
        SqlSessionFactoryBean factory = new SqlSessionFactoryBean();
        factory.setDataSource(dataSource);
        factory.setConfiguration(configuration);
        factory.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath:mapper/*.xml"));
        SqlSessionTemplate sessions = new SqlSessionTemplate(factory.getObject());
        NoteMapper notes = sessions.getMapper(NoteMapper.class);
        NoteLikeMapper likeMapper = sessions.getMapper(NoteLikeMapper.class);
        scope = new RequestScopeData();
        scope.setLogin(true);
        scope.setUserId(1L);
        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        ValueOperations<String,String> values = mock(ValueOperations.class);
        when(redis.opsForValue()).thenReturn(values);
        Map<String,String> cached = new ConcurrentHashMap<>();
        when(values.get(anyString())).thenAnswer(call -> cached.get(call.getArgument(0)));
        when(values.setIfAbsent(anyString(), anyString())).thenAnswer(call -> cached.putIfAbsent(call.getArgument(0), call.getArgument(1)) == null);
        doAnswer(call -> { cached.put(call.getArgument(0), call.getArgument(1)); return null; }).when(values).set(anyString(), anyString());
        doAnswer(call -> { cached.put(call.getArgument(0), call.getArgument(1)); return null; })
                .when(values).set(anyString(), anyString(), any(java.time.Duration.class));
        NoteListCache cache = new NoteListCache(notes, redis, new ObjectMapper().findAndRegisterModules());
        new org.springframework.jdbc.datasource.init.ResourceDatabasePopulator(
                new org.springframework.core.io.ClassPathResource("db/outbox.sql")).execute(dataSource);
        outbox = new OutboxService(sessions.getMapper(OutboxMapper.class), new ObjectMapper(), new DataSourceTransactionManager(dataSource));
        likes = transactional(new NoteLikeServiceImpl(likeMapper, notes, scope, outbox, cache), NoteLikeService.class);
        CollectionMapper folderMapper = sessions.getMapper(CollectionMapper.class);
        CollectionNoteMapper collectionMapper = sessions.getMapper(CollectionNoteMapper.class);
        collections = transactional(new CollectionNoteServiceImpl(folderMapper, collectionMapper, notes, scope, cache), CollectionNoteService.class);
        folders = transactional(new CollectionServiceImpl(scope, folderMapper, collectionMapper, collections), CollectionService.class);
        comments = transactional(new CommentServiceImpl(sessions.getMapper(CommentMapper.class), notes,
                mock(UserMapper.class), mock(CommentLikeMapper.class), outbox,
                scope, mock(MessageMapper.class), cache), CommentService.class);
        UserMapper users = mock(UserMapper.class);
        QuestionMapper questions = mock(QuestionMapper.class);

        when(users.findByIdBatch(any())).thenReturn(List.of());
        when(questions.findByIdBatch(any())).thenReturn(List.of());
        UserServiceImpl userService = new UserServiceImpl();
        ReflectionTestUtils.setField(userService, "userMapper", users);
        reads = new NoteServiceImpl(notes, cache, userService,
                new QuestionServiceImpl(questions, null, scope, notes, null), likes,
                collections, scope,
                null, null, redis, null, null, null);
    }

    private <T> T transactional(T target, Class<T> type) {
        ProxyFactory factory = new ProxyFactory(target);
        factory.addAdvice(new TransactionInterceptor(new DataSourceTransactionManager(dataSource),
                new AnnotationTransactionAttributeSource()));
        return type.cast(factory.getProxy());
    }

    @AfterEach
    void cleanUp() {
        if (database != null) {
            admin.execute("DROP DATABASE " + database);
        }
    }

    @Test
    void invalidBatchActionReturnsBadRequestWithoutChanges() throws Exception {
        org.notes.controller.CollectionNoteController controller = new org.notes.controller.CollectionNoteController();
        ReflectionTestUtils.setField(controller, "collectionNoteService", collections);
        org.springframework.test.web.servlet.MockMvc http = org.springframework.test.web.servlet.setup.MockMvcBuilders
                .standaloneSetup(controller).setControllerAdvice(new org.notes.exception.GlobalExceptionHandler()).build();
        http.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/collectionNotes/batch")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content("{\"noteId\":7,\"collections\":[{\"collectionId\":1,\"action\":\"invalid\"}]}"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.code").value(400));
        assertTrue(collections.findUserCollectedNoteIds(1L, List.of(7)).isEmpty());
    }

    @Test
    void existingLikeRemainsIdempotentInsideRepeatableReadTransaction() {
        org.springframework.transaction.support.TransactionTemplate transaction =
                new org.springframework.transaction.support.TransactionTemplate(new DataSourceTransactionManager(dataSource));
        transaction.setIsolationLevel(org.springframework.transaction.TransactionDefinition.ISOLATION_REPEATABLE_READ);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            transaction.executeWithoutResult(status -> {
                assertEquals(0, reads.getNotes(new NoteQueryParams()).getData().get(0).getLikeCount());
                try {
                    executor.submit(() -> likes.likeNote(7)).get(15, TimeUnit.SECONDS);
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }
                likes.likeNote(7);
            });
        } finally {
            executor.shutdownNow();
        }
        assertEquals(1, reads.getNotes(new NoteQueryParams()).getData().get(0).getLikeCount());
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM outbox_event", Integer.class));
    }

    @Test
    void repeatedBatchOperationsRemainIdempotent() throws Exception {
        org.notes.model.dto.collectionNote.UpdateCollectionNoteBatchBody body = new org.notes.model.dto.collectionNote.UpdateCollectionNoteBatchBody();
        body.setNoteId(7);
        org.notes.model.dto.collectionNote.UpdateCollectionNoteBatchBody.UpdateItem first = new org.notes.model.dto.collectionNote.UpdateCollectionNoteBatchBody.UpdateItem();
        first.setCollectionId(2);
        first.setAction(org.notes.model.dto.collectionNote.UpdateCollectionNoteBatchBody.Action.CREATE);
        org.notes.model.dto.collectionNote.UpdateCollectionNoteBatchBody.UpdateItem second = new org.notes.model.dto.collectionNote.UpdateCollectionNoteBatchBody.UpdateItem();
        second.setCollectionId(1);
        second.setAction(org.notes.model.dto.collectionNote.UpdateCollectionNoteBatchBody.Action.CREATE);
        body.setCollections(new org.notes.model.dto.collectionNote.UpdateCollectionNoteBatchBody.UpdateItem[]{first, second, first});
        body = new ObjectMapper().readValue(new ObjectMapper().writeValueAsString(body),
                org.notes.model.dto.collectionNote.UpdateCollectionNoteBatchBody.class);
        first = body.getCollections()[0];
        second = body.getCollections()[1];
        collections.batchModifyCollection(body);
        collections.batchModifyCollection(body);
        assertEquals(1, reads.getNotes(new NoteQueryParams()).getData().get(0).getCollectCount());
        for (org.notes.model.dto.collectionNote.UpdateCollectionNoteBatchBody.UpdateItem item : body.getCollections()) {
            item.setAction(org.notes.model.dto.collectionNote.UpdateCollectionNoteBatchBody.Action.DELETE);
        }
        collections.batchModifyCollection(body);
        collections.batchModifyCollection(body);
        assertEquals(0, reads.getNotes(new NoteQueryParams()).getData().get(0).getCollectCount());
    }

    @Test
    void migrationAddsUniqueConstraintsAndRebuildsCounts() throws Exception {
        jdbc.execute("ALTER TABLE note_like DROP PRIMARY KEY");
        jdbc.execute("ALTER TABLE collection_note DROP PRIMARY KEY");
        jdbc.execute("UPDATE note SET like_count=99, collect_count=99, comment_count=99");
        String script;
        try (java.io.InputStream input = new org.springframework.core.io.ClassPathResource("db/note_interaction_constraints.sql").getInputStream()) {
            script = new String(input.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        }
        String[] sections = script.split("(?m)^DELIMITER[^\\r\\n]*\\R");
        for (int run = 0; run < 2; run++) {
            for (String statement : sections[1].split("\\$\\$")) {
                if (!statement.isBlank()) {
                    jdbc.execute(statement);
                }
            }
            for (String statement : sections[2].split(";")) {
                if (!statement.isBlank()) {
                    jdbc.execute(statement);
                }
            }
        }
        assertEquals(0, reads.getNotes(new NoteQueryParams()).getData().get(0).getLikeCount());
        likes.likeNote(7);
        likes.likeNote(7);
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM outbox_event", Integer.class));
        assertEquals(1, reads.getNotes(new NoteQueryParams()).getData().get(0).getLikeCount());
        assertThrows(org.springframework.dao.DuplicateKeyException.class,
                () -> jdbc.execute("INSERT INTO note_like(user_id,note_id) VALUES(1,7)"));
        org.notes.model.dto.collectionNote.UpdateCollectionNoteBody body = new org.notes.model.dto.collectionNote.UpdateCollectionNoteBody();
        body.setNoteId(7);
        collections.createCollectionNote(1, body);
        assertThrows(org.springframework.dao.DuplicateKeyException.class,
                () -> jdbc.execute("INSERT INTO collection_note(collection_id,note_id) VALUES(1,7)"));
    }

    @Test
    void rolledBackLikeDoesNotLeakIntoCache() {
        assertEquals(0, reads.getNotes(new NoteQueryParams()).getData().get(0).getLikeCount());
        org.springframework.transaction.support.TransactionTemplate transaction =
                new org.springframework.transaction.support.TransactionTemplate(new DataSourceTransactionManager(dataSource));
        transaction.executeWithoutResult(status -> {
            likes.likeNote(7);
            assertEquals(1, reads.getNotes(new NoteQueryParams()).getData().get(0).getLikeCount());
            status.setRollbackOnly();
        });
        assertEquals(0, reads.getNotes(new NoteQueryParams()).getData().get(0).getLikeCount());
        assertTrue(likes.findUserLikedNoteIds(1L, List.of(7)).isEmpty());
        assertTrue(outbox.claim().isEmpty());
    }

    @Test
    void deletingCommentUpdatesNoteCount() {
        jdbc.execute("INSERT INTO comment(comment_id,note_id,author_id,content) VALUES(1,7,1,'comment')");
        jdbc.execute("UPDATE note SET comment_count=1 WHERE note_id=7");
        assertEquals(1, reads.getNotes(new NoteQueryParams()).getData().get(0).getCommentCount());
        comments.deleteComment(1);
        assertEquals(0, reads.getNotes(new NoteQueryParams()).getData().get(0).getCommentCount());
    }

    @Test
    void deletingFolderPreservesOtherFoldersAndUpdatesCount() {
        org.notes.model.dto.collectionNote.UpdateCollectionNoteBody body = new org.notes.model.dto.collectionNote.UpdateCollectionNoteBody();
        body.setNoteId(7);
        collections.createCollectionNote(1, body);
        collections.createCollectionNote(2, body);
        folders.deleteCollection(1);
        assertEquals(1, reads.getNotes(new NoteQueryParams()).getData().get(0).getCollectCount());
        folders.deleteCollection(2);
        assertEquals(0, reads.getNotes(new NoteQueryParams()).getData().get(0).getCollectCount());
    }

    @Test
    void concurrentLikesAndUnlikesKeepCountsConsistent() throws Exception {
        runConcurrently(() -> likes.likeNote(7));
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM outbox_event", Integer.class));
        assertEquals(1, reads.getNotes(new NoteQueryParams()).getData().get(0).getLikeCount());
        runConcurrently(() -> likes.unlikeNote(7));
        assertEquals(0, reads.getNotes(new NoteQueryParams()).getData().get(0).getLikeCount());
    }

    @Test
    void concurrentFoldersCountOneUserOnce() throws Exception {
        org.notes.model.dto.collectionNote.UpdateCollectionNoteBody body = new org.notes.model.dto.collectionNote.UpdateCollectionNoteBody();
        body.setNoteId(7);
        java.util.concurrent.atomic.AtomicInteger next = new java.util.concurrent.atomic.AtomicInteger();
        runConcurrently(() -> collections.createCollectionNote(next.getAndIncrement() % 2 + 1, body));
        assertEquals(1, reads.getNotes(new NoteQueryParams()).getData().get(0).getCollectCount());
        runConcurrently(() -> collections.deleteCollectionNote(next.getAndIncrement() % 2 + 1, body));
        assertEquals(0, reads.getNotes(new NoteQueryParams()).getData().get(0).getCollectCount());
    }

    private void runConcurrently(Runnable action) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(8);
        CountDownLatch ready = new CountDownLatch(8);
        CountDownLatch start = new CountDownLatch(1);
        try {
            List<Future<?>> futures = new ArrayList<>();
            for (int i = 0; i < 8; i++) {
                futures.add(executor.submit(() -> {
                    ready.countDown();
                    try {
                        assertTrue(start.await(10, TimeUnit.SECONDS));
                        action.run();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new IllegalStateException(e);
                    }
                }));
            }
            assertTrue(ready.await(10, TimeUnit.SECONDS));
            start.countDown();
            for (Future<?> future : futures) {
                future.get(20, TimeUnit.SECONDS);
            }
        } finally {
            start.countDown();
            executor.shutdownNow();
        }
    }

    @Test
    void repeatedCollectionChangesCountDistinctUsers() {
        org.notes.model.dto.collectionNote.UpdateCollectionNoteBody body = new org.notes.model.dto.collectionNote.UpdateCollectionNoteBody();
        body.setNoteId(7);
        collections.createCollectionNote(1, body);
        collections.createCollectionNote(1, body);
        collections.createCollectionNote(2, body);
        assertEquals(1, reads.getNotes(new NoteQueryParams()).getData().get(0).getCollectCount());
        collections.deleteCollectionNote(1, body);
        collections.deleteCollectionNote(1, body);
        assertEquals(1, reads.getNotes(new NoteQueryParams()).getData().get(0).getCollectCount());
        collections.deleteCollectionNote(2, body);
        assertEquals(0, reads.getNotes(new NoteQueryParams()).getData().get(0).getCollectCount());
    }

    @Test
    void repeatedLikeAndUnlikeAreIdempotent() {
        likes.likeNote(7);
        likes.likeNote(7);
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM outbox_event", Integer.class));
        assertEquals(1, reads.getNotes(new NoteQueryParams()).getData().get(0).getLikeCount());
        likes.unlikeNote(7);
        likes.unlikeNote(7);
        assertEquals(0, reads.getNotes(new NoteQueryParams()).getData().get(0).getLikeCount());
    }
}
