package org.notes.service;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.concurrent.Executor;

@Slf4j
@Service
public class PostCommitExecutor {

    private final Executor executor;

    public PostCommitExecutor(@Qualifier("postCommitTaskExecutor") Executor executor) {
        this.executor = executor;
    }

    public void execute(Runnable action) {
        String traceId = MDC.get(ReliableRabbitPublisher.TRACE_ID_HEADER);
        Runnable contextAwareAction = () -> {
            if (traceId != null) {
                MDC.put(ReliableRabbitPublisher.TRACE_ID_HEADER, traceId);
            }
            try {
                action.run();
            } finally {
                MDC.remove(ReliableRabbitPublisher.TRACE_ID_HEADER);
            }
        };
        try {
            executor.execute(contextAwareAction);
        } catch (RuntimeException e) {
            log.warn("Post-commit executor saturated; running task on caller thread", e);
            contextAwareAction.run();
        }
    }
}
