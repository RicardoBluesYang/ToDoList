package com.example.todolist.controller;

import com.example.todolist.dto.AiCommitResponse;
import com.example.todolist.dto.AiDecomposeCommitRequest;
import com.example.todolist.dto.AiDecomposeRequest;
import com.example.todolist.dto.AiDecomposeResponse;
import com.example.todolist.entity.TodoItem;
import com.example.todolist.entity.TodoSubtask;
import com.example.todolist.mapper.AiCallStatMapper;
import com.example.todolist.service.AiDecomposeService;
import com.example.todolist.service.TodoService;
import com.example.todolist.service.impl.AiUpstreamException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private static final int TITLE_MAX_LENGTH = 255;

    @Autowired
    private AiDecomposeService aiDecomposeService;

    @Autowired
    private TodoService todoService;

    @Autowired
    private AiCallStatMapper aiCallStatMapper;

    @PostMapping("/decompose")
    public ResponseEntity<?> decompose(HttpServletRequest request, @RequestBody AiDecomposeRequest body) {
        try {
            requireUserId(request);
            if (body == null) {
                throw new IllegalArgumentException("Request body is required");
            }

            String goalTitle = normalizeTitle(body.getGoalTitle(), "goalTitle");
            LocalDateTime dueDate = parseDueDate(body.getDueDate());
            AiDecomposeResponse result = aiDecomposeService.decompose(goalTitle, dueDate);
            recordAiCall("SUCCESS");
            return ResponseEntity.ok(result);
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(errorBody("Unauthorized"));
        } catch (IllegalArgumentException e) {
            recordAiCall("FAILED");
            return ResponseEntity.badRequest().body(errorBody(e.getMessage()));
        } catch (AiUpstreamException e) {
            recordAiCall("FAILED");
            return ResponseEntity.status(502).body(errorBody(e.getMessage()));
        } catch (Exception e) {
            recordAiCall("FAILED");
            return ResponseEntity.status(502).body(errorBody("Decompose failed, please retry"));
        }
    }

    @PostMapping("/decompose/commit")
    public ResponseEntity<?> commit(HttpServletRequest request, @RequestBody AiDecomposeCommitRequest body) {
        try {
            Long userId = requireUserId(request);
            if (body == null) {
                throw new IllegalArgumentException("Request body is required");
            }
            String parentTitle = normalizeTitle(body.getParentTitle(), "parentTitle");
            LocalDateTime dueDate = parseDueDate(body.getDueDate());
            List<String> subtasks = normalizeSubtasks(body.getSubtasks());

            TodoItem todo = todoService.createTodo(userId, parentTitle, dueDate);
            List<TodoSubtask> createdSubtasks = new ArrayList<>();
            for (String subtaskTitle : subtasks) {
                createdSubtasks.add(todoService.createSubtask(userId, todo.getId(), subtaskTitle));
            }
            todo.setSubtasks(createdSubtasks);

            return ResponseEntity.ok(new AiCommitResponse(todo));
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(errorBody("Unauthorized"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(errorBody(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(errorBody("Commit failed"));
        }
    }

    private Long requireUserId(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            throw new SecurityException("Unauthorized");
        }
        return userId;
    }

    private LocalDateTime parseDueDate(String dueDate) {
        if (dueDate == null) {
            return null;
        }
        String raw = dueDate.trim();
        if (raw.isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(raw);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid dueDate format, expected yyyy-MM-ddTHH:mm");
        }
    }

    private List<String> normalizeSubtasks(List<String> subtasks) {
        if (subtasks == null) {
            throw new IllegalArgumentException("At least one subtask is required");
        }

        List<String> normalized = new ArrayList<>();
        for (String item : subtasks) {
            if (item == null) {
                continue;
            }
            String title = item.trim();
            if (title.isEmpty()) {
                continue;
            }
            if (title.length() > TITLE_MAX_LENGTH) {
                throw new IllegalArgumentException("Subtask title length exceeds 255");
            }
            normalized.add(title);
        }

        List<String> deduplicated = new ArrayList<>(new LinkedHashSet<>(normalized));
        if (deduplicated.isEmpty()) {
            throw new IllegalArgumentException("At least one subtask is required");
        }
        return deduplicated;
    }

    private String normalizeTitle(String title, String fieldName) {
        if (title == null) {
            throw new IllegalArgumentException(fieldName + " cannot be empty");
        }
        String normalized = title.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be empty");
        }
        if (normalized.length() > TITLE_MAX_LENGTH) {
            throw new IllegalArgumentException(fieldName + " length exceeds 255");
        }
        return normalized;
    }

    private Map<String, String> errorBody(String message) {
        Map<String, String> body = new HashMap<>();
        body.put("error", (message == null || message.trim().isEmpty()) ? "Request failed" : message);
        return body;
    }

    private void recordAiCall(String status) {
        try {
            aiCallStatMapper.insert(status);
        } catch (Exception ignored) {
            // Statistics must not block the AI user flow.
        }
    }
}
