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
        // 放行 OPTIONS 请求 (CORS 预检请求)
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }

        // 1. 获取请求头中的 Token
        String token = request.getHeader("Authorization");
        
        // 2. 检查 Token 格式 (通常带有 Bearer 前缀)
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7); // 去掉 "Bearer "
            
            // 3. 校验并解析 Token
            Long userId = JwtUtils.getUserIdFromToken(token);
            if (userId != null) {
                // 4. 将 userId 存入 request 属性中，方便后续 Controller 使用
                request.setAttribute("userId", userId);
                return true; // 放行
            }
        }

        // 5. 拦截未登录或 Token 无效的请求
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=utf-8");
        response.getWriter().write("{\"error\": \"Unauthorized\", \"message\": \"请先登录\"}");
        return false;
    }
}
