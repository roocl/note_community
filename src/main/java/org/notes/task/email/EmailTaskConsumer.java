package org.notes.task.email;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.notes.model.enums.redisKey.RedisKey;
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
public class EmailTaskConsumer {
    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private org.thymeleaf.TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String from;

    @Value("${mail.verify-code.template-path}")
    private String templatePath;

    @Scheduled(fixedDelay = 3000)
    public void resume() throws MessagingException, JsonProcessingException {
        String emailQueueKey = RedisKey.emailTaskQueue();

        while (true) {
            String emailTaskJson = redisTemplate.opsForList().rightPop(emailQueueKey);

            if (emailTaskJson == null) {
                break;
            }

            EmailTask emailTask = objectMapper.readValue(emailTaskJson, EmailTask.class);
            String email = emailTask.getEmail();
            String verificationCode = emailTask.getCode();

            // 准备 Thymeleaf 上下文
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

            redisTemplate.opsForValue().set(RedisKey.registerVerificationCode(email), verificationCode, 5,
                    TimeUnit.MINUTES);
        }
    }
}
