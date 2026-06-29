package org.notes.task.email;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.notes.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.internet.MimeMessage;

@Slf4j
@Component
@RequiredArgsConstructor
public class WelcomeEmailTaskConsumer {

    private final JavaMailSender mailSender;

    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String from;

    @RabbitListener(queues = RabbitMQConfig.WELCOME_EMAIL_QUEUE)
    public void processWelcomeEmail(WelcomeEmailTask task) {
        log.info("收到欢迎邮件任务：{}", task.getEmail());

        try {
            Context context = new Context();
            context.setVariable("username", task.getUsername());

            String emailContent = templateEngine.process("mail/welcome", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(from);
            helper.setTo(task.getEmail());
            helper.setSubject("欢迎加入笔记社区！");
            helper.setText(emailContent, true);

            mailSender.send(message);
            log.info("欢迎邮件发送成功：{}", task.getEmail());
        } catch (Exception e) {
            log.error("欢迎邮件发送失败：{}", task.getEmail(), e);
            throw new RuntimeException("欢迎邮件发送失败，触发重试", e);
        }
    }
}
