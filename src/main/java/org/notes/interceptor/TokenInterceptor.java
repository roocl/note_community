package org.notes.interceptor;

import org.notes.mapper.UserMapper;
import org.notes.model.entity.User;
import org.notes.scope.RequestScopeData;
import org.notes.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private UserMapper userMapper;

    @Override
    public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response, final Object handler) throws Exception {
        String token = request.getHeader("Authorization");

        if (token == null) {
            requestScopeData.setLogin(false);
            requestScopeData.setToken(null);
            requestScopeData.setUserId(null);
            requestScopeData.setIsAdmin(null);
            return true;
        }

        token = token.replace("Bearer ", "");

        if (jwtUtil.validateToken(token)) {
            Long userId = jwtUtil.getUserIdFromToken(token);
            User user = userMapper.findById(userId);

            if (userId == null || user == null) {
                requestScopeData.setLogin(false);
                requestScopeData.setToken(null);
                requestScopeData.setUserId(null);
                requestScopeData.setIsAdmin(null);
                return true;
            }

            requestScopeData.setLogin(true);
            requestScopeData.setToken(token);
            requestScopeData.setUserId(userId);
            requestScopeData.setIsAdmin(user.getIsAdmin());
        } else {
            requestScopeData.setLogin(false);
            requestScopeData.setToken(null);
            requestScopeData.setUserId(null);
            requestScopeData.setIsAdmin(null);
        }
        return HandlerInterceptor.super.preHandle(request, response, handler);
    }
}
