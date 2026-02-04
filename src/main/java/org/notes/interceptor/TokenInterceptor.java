package org.notes.interceptor;

import io.jsonwebtoken.Jwt;
import org.notes.scope.RequestScopeData;
import org.notes.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class TokenInterceptor implements HandlerInterceptor {
    @Autowired
    private RequestScopeData requestScopeData;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response, final Object handler) throws Exception {
        String token = request.getHeader("Authorization");

        if (token == null) {
            requestScopeData.setLogin(false);
            requestScopeData.setToken(null);
            requestScopeData.setUserId(null);
            return true;
        }

        token = token.replace("Bearer ", "");

        if (jwtUtil.validateToken(token)) {
            Long userId = jwtUtil.getUserIdFromToken(token);
            requestScopeData.setLogin(true);
            requestScopeData.setToken(token);
            requestScopeData.setUserId(userId);
        } else {
            requestScopeData.setLogin(false);
        }
        return HandlerInterceptor.super.preHandle(request, response, handler);
    }
}
