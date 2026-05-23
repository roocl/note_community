package org.notes.aspect;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.notes.annotation.NeedAdmin;
import org.notes.exception.ForbiddenException;
import org.notes.exception.UnauthorizedException;
import org.notes.scope.RequestScopeData;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class NeedAdminAspect {

    private final RequestScopeData requestScopeData;

    @Around("@annotation(needAdmin)")
    public Object around(ProceedingJoinPoint joinPoint, NeedAdmin needAdmin) throws Throwable {
        if (!requestScopeData.isLogin()) {
            throw new UnauthorizedException("用户未登录");
        }

        if (requestScopeData.getUserId() == null) {
            throw new UnauthorizedException("用户 ID 异常");
        }

        Integer isAdmin = requestScopeData.getIsAdmin();
        if (isAdmin == null || isAdmin != 1) {
            throw new ForbiddenException("需要管理员权限");
        }

        return joinPoint.proceed();
    }
}
