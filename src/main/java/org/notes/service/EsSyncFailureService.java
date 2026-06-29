package org.notes.service;

import org.notes.model.entity.EsSyncFailure;

import java.util.List;

public interface EsSyncFailureService {

    void recordFailure(String entityType, Long entityId, String operation, Exception exception);

    List<EsSyncFailure> listFailures();

    EsSyncFailure getFailure(Long id);

    void retryFailure(Long id);

    void retryPendingFailures(int limit);

    int reconcileAll();

    int reconcileNotes();

    int reconcileUsers();

    void deleteFailure(Long id);
}
