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
            response.put("message", "Register success");
            response.put("userId", user.getId());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(errorBody(e, "Register failed"));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> payload) {
        try {
            if (payload == null || payload.get("username") == null || payload.get("password") == null) {
                return ResponseEntity.badRequest().body(errorBody(null, "Username or password cannot be empty"));
            }

            String token = userService.login(payload.get("username"), payload.get("password"));
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("message", "Login success");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(errorBody(e, "Invalid username or password"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(errorBody(e, "Internal server error"));
        }
    }

    private Map<String, String> errorBody(Throwable throwable, String fallbackMessage) {
        String message = throwable == null ? null : throwable.getMessage();
        if (message == null || message.trim().isEmpty()) {
            message = fallbackMessage;
        }
        Map<String, String> body = new HashMap<>();
        body.put("error", message);
        return body;
    }
}
