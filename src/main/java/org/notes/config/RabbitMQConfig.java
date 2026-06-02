package org.notes.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // 业务队列
    public static final String EMAIL_QUEUE = "email.queue";
    public static final String NOTIFICATION_QUEUE = "notification.queue";
    public static final String WELCOME_EMAIL_QUEUE = "welcome.email.queue";

    // 死信
    public static final String DLX_EXCHANGE = "dlx.exchange";
    public static final String DLX_EMAIL_QUEUE = "email.queue.dlx";
    public static final String DLX_NOTIFICATION_QUEUE = "notification.queue.dlx";
    public static final String DLX_WELCOME_EMAIL_QUEUE = "welcome.email.queue.dlx";

    // 死信交换机
    @Bean
    public DirectExchange dlxExchange() {
        return new DirectExchange(DLX_EXCHANGE);
    }

    // 业务队列（含 DLX）
    @Bean
    public Queue emailQueue() {
        return QueueBuilder.durable(EMAIL_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", "dlx.email")
                .build();
    }

    @Bean
    public Queue notificationQueue() {
        return QueueBuilder.durable(NOTIFICATION_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", "dlx.notification")
                .build();
    }

    @Bean
    public Queue welcomeEmailQueue() {
        return QueueBuilder.durable(WELCOME_EMAIL_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", "dlx.welcome")
                .build();
    }

    // 死信队列
    @Bean
    public Queue dlxEmailQueue() {
        return new Queue(DLX_EMAIL_QUEUE, true);
    }

    @Bean
    public Queue dlxNotificationQueue() {
        return new Queue(DLX_NOTIFICATION_QUEUE, true);
    }

    @Bean
    public Queue dlxWelcomeEmailQueue() {
        return new Queue(DLX_WELCOME_EMAIL_QUEUE, true);
    }

    // 死信队列绑定
    @Bean
    public Binding dlxEmailBinding() {
        return BindingBuilder.bind(dlxEmailQueue()).to(dlxExchange()).with("dlx.email");
    }

    @Bean
    public Binding dlxNotificationBinding() {
        return BindingBuilder.bind(dlxNotificationQueue()).to(dlxExchange()).with("dlx.notification");
    }

    @Bean
    public Binding dlxWelcomeEmailBinding() {
        return BindingBuilder.bind(dlxWelcomeEmailQueue()).to(dlxExchange()).with("dlx.welcome");
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
