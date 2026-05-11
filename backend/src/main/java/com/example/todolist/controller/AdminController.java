package com.example.todolist.controller;

import com.example.todolist.dto.AdminOverviewResponse;
import com.example.todolist.dto.AdminUserStatsResponse;
import com.example.todolist.dto.AiStatsResponse;
import com.example.todolist.mapper.AdminMapper;
import com.example.todolist.mapper.AiCallStatMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private AdminMapper adminMapper;

    @Autowired
    private AiCallStatMapper aiCallStatMapper;

    @GetMapping("/overview")
    public ResponseEntity<?> overview(HttpServletRequest request) {
        if (!isAdmin(request)) {
            return ResponseEntity.status(403).body(errorBody("Forbidden"));
        }

        AdminOverviewResponse overview = adminMapper.getOverview();
        return ResponseEntity.ok(overview);
    }

    @GetMapping("/users")
    public ResponseEntity<?> users(HttpServletRequest request) {
        if (!isAdmin(request)) {
            return ResponseEntity.status(403).body(errorBody("Forbidden"));
        }

        List<AdminUserStatsResponse> users = adminMapper.listUsers();
        return ResponseEntity.ok(users);
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(HttpServletRequest request, @PathVariable Long id) {
        if (!isAdmin(request)) {
            return ResponseEntity.status(403).body(errorBody("Forbidden"));
        }

        String username = adminMapper.findUsernameById(id);
        if (username == null) {
            return ResponseEntity.status(404).body(errorBody("User not found"));
        }
        if ("admin".equals(username)) {
            return ResponseEntity.status(403).body(errorBody("Admin user cannot be deleted"));
        }

        int affectedRows = adminMapper.deleteUserById(id);
        if (affectedRows == 0) {
            return ResponseEntity.status(404).body(errorBody("User not found"));
        }

        Map<String, String> body = new HashMap<>();
        body.put("message", "User deleted");
        return ResponseEntity.ok(body);
    }

    @GetMapping("/ai-stats")
    public ResponseEntity<?> aiStats(HttpServletRequest request) {
        if (!isAdmin(request)) {
            return ResponseEntity.status(403).body(errorBody("Forbidden"));
        }

        AiStatsResponse stats = aiCallStatMapper.getStats();
        return ResponseEntity.ok(stats);
    }

    private boolean isAdmin(HttpServletRequest request) {
        return "admin".equals(request.getAttribute("username"));
    }

    private Map<String, String> errorBody(String message) {
        Map<String, String> body = new HashMap<>();
        body.put("error", message);
        return body;
    }
}
