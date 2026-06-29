package org.notes.service.impl;

import lombok.RequiredArgsConstructor;
import org.notes.exception.NotFoundException;
import org.notes.mapper.DlxMessageMapper;
import org.notes.model.entity.DlxMessage;
import org.notes.service.DlxMessageService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DlxMessageServiceImpl implements DlxMessageService {

    private final DlxMessageMapper dlxMessageMapper;

    private final RabbitTemplate rabbitTemplate;

    @Override
    public List<DlxMessage> listMessages() {
        return dlxMessageMapper.findAll();
    }

    @Override
    public DlxMessage getMessage(Long id) {
        DlxMessage message = dlxMessageMapper.findById(id);
        if (message == null) {
            throw new NotFoundException("死信消息不存在");
        }
        return message;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void retryMessage(Long id) {
        DlxMessage dlxMessage = getMessage(id);
        Message message = MessageBuilder
                .withBody(dlxMessage.getMessageBody().getBytes(StandardCharsets.UTF_8))
                .setContentType(MessageProperties.CONTENT_TYPE_JSON)
                .build();

        rabbitTemplate.send(dlxMessage.getOriginQueue(), message);
        dlxMessageMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMessage(Long id) {
        dlxMessageMapper.deleteById(id);
    }
}
