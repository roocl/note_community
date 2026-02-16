package org.notes.aspect;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.notes.annotation.NeedLogin;
import org.notes.exception.UnauthorizedException;
import org.notes.scope.RequestScopeData;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class NeedLoginAspect {

    private final RequestScopeData requestScopeData;

    @Around("@annotation(needLogin)")
    public Object around(ProceedingJoinPoint joinPoint, NeedLogin needLogin) throws Throwable {

        if (!requestScopeData.isLogin()) {
            throw new UnauthorizedException("用户未登录");
        }

        if (requestScopeData.getUserId() == null) {
            throw new UnauthorizedException("用户 ID 异常");
        }
        return joinPoint.proceed();
    }
}
