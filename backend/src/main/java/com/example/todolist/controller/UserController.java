package com.example.todolist.controller;

import com.example.todolist.entity.User;
import com.example.todolist.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> payload) {
        try {
            User user = userService.register(payload.get("username"), payload.get("password"));
            Map<String, Object> response = new HashMap<>();
            response.put("message", "注册成功");
            response.put("userId", user.getId());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> payload) {
        try {
            System.out.println("Login attempt for username: " + payload.get("username"));
            String token = userService.login(payload.get("username"), payload.get("password"));
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("message", "登录成功");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            e.printStackTrace(); // 打印完整错误堆栈
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace(); // 打印其他未知错误
            return ResponseEntity.status(500).body(Map.of("error", "服务器内部错误"));
        }
    }
}
