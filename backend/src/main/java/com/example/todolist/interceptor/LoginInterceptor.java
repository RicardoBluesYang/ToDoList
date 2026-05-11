package com.example.todolist.interceptor;

import com.example.todolist.utils.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }

        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);

            Long userId = JwtUtils.getUserIdFromToken(token);
            String username = JwtUtils.getUsernameFromToken(token);
            if (userId != null && username != null && !username.trim().isEmpty()) {
                request.setAttribute("userId", userId);
                request.setAttribute("username", username);
                return true;
            }
        }

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=utf-8");
        response.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"请先登录\"}");
        return false;
    }
}
