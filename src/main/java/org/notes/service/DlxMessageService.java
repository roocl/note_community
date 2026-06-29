package org.notes.service;

import org.notes.model.entity.DlxMessage;

import java.util.List;

public interface DlxMessageService {

    List<DlxMessage> listMessages();

    DlxMessage getMessage(Long id);

    void retryMessage(Long id);

    void deleteMessage(Long id);
}
