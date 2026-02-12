package org.notes.aspect;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Aspect
@Component
public class PutTraceIdAspect {
    private static final String TRACE_ID_KEY = "traceId";

    @Before("execution(* org.notes..*(..))")
    public void addTraceIdToLog() {
        if (MDC.get(TRACE_ID_KEY) == null) {
            String traceId = UUID.randomUUID().toString();
            MDC.put(TRACE_ID_KEY, traceId);
        }
    }
}