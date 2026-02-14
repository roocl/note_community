package org.notes.task.email;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.notes.config.RabbitMQConfig;
import org.notes.model.enums.redisKey.RedisKey;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class EmailTaskConsumer {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private org.thymeleaf.TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String from;

    @Value("${mail.verify-code.template-path}")
    private String templatePath;

    // 监听rabbitmq队列
    @RabbitListener(queues = RabbitMQConfig.EMAIL_QUEUE)
    public void processEmail(EmailTask emailTask) {
        log.info("rabbitmq收到发信任务：{}", emailTask.getEmail());

        try {
            String email = emailTask.getEmail();
            String verificationCode = emailTask.getCode();

            // 准备thymeleaf上下文
            org.thymeleaf.context.Context context = new org.thymeleaf.context.Context();
            context.setVariable("verifyCode", verificationCode);
            context.setVariable("operationType", "注册/身份验证");

            // 渲染模板
            String emailContent = templateEngine.process(templatePath, context);

            javax.mail.internet.MimeMessage message = mailSender.createMimeMessage();
            org.springframework.mail.javamail.MimeMessageHelper helper = new org.springframework.mail.javamail.MimeMessageHelper(
                    message, true, "UTF-8");

            helper.setFrom(from);
            helper.setTo(email);
            helper.setSubject("roocl-笔记社区-验证码");
            helper.setText(emailContent, true);

            mailSender.send(message);
            log.info("邮件发送成功！");
        } catch (Exception e) {
            log.error("验证码发送失败", e);
            throw new RuntimeException("邮件发送失败，触发重试", e);
        }
    }
}
