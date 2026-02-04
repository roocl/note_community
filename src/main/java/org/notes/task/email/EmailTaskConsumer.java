package org.notes.task.email;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.notes.model.enums.RedisKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class EmailTaskConsumer {
    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Value("${spring.mail.username}")
    private String from;

    @Scheduled(fixedDelay = 3000)
    public void resume() throws JsonProcessingException {
        String emailQueueKey = RedisKey.emailTaskQueue();

        while (true) {
            String emailTaskJson = redisTemplate.opsForList().rightPop(emailQueueKey);

            if (emailTaskJson == null) {
                break;
            }

            EmailTask emailTask = objectMapper.readValue(emailTaskJson, EmailTask.class);
            String email = emailTask.getEmail();
            String verificationCode = emailTask.getCode();

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(email);
            message.setSubject("roocl-笔记社区-验证码");
            message.setText("您的验证码是：" + verificationCode + "，有效期" + 5 + "分钟，请勿泄露给他人。");
            mailSender.send(message);

            redisTemplate.opsForValue().set(RedisKey.registerVerificationCode(email), verificationCode, 5, TimeUnit.MINUTES);
        }
    }
}
