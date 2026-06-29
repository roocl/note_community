package org.notes.task.es;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.notes.service.EsSyncFailureService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EsSyncFailureRetryTask {

    private final EsSyncFailureService esSyncFailureService;

    @Scheduled(cron = "0 */10 * * * ?")
    public void retryPendingFailures() {
        log.info("[ES同步补偿] 重试待处理的失败");
        esSyncFailureService.retryPendingFailures(50);
    }

    @Scheduled(cron = "0 0 4 * * ?")
    public void reconcileEsDocuments() {
        int mismatchCount = esSyncFailureService.reconcileAll();
        log.info("[ES同步对账] 检测到{}条不一致的文档", mismatchCount);
    }
}
