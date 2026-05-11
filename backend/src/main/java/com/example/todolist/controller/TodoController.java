package com.example.todolist.controller;

import com.example.todolist.entity.TodoItem;
import com.example.todolist.entity.TodoSubtask;
import com.example.todolist.service.TodoService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/todos")
public class TodoController {

    @Autowired
    private TodoService todoService;

    private Long getUserId(HttpServletRequest request) {
        return (Long) request.getAttribute("userId");
    }

    @GetMapping
    public List<TodoItem> getAllTodos(HttpServletRequest request) {
        return todoService.getAllTodos(getUserId(request));
    }

    @PostMapping
    public ResponseEntity<?> createTodo(HttpServletRequest request, @RequestBody Map<String, Object> payload) {
        try {
            String title = getString(payload, "title");
            LocalDateTime dueDate = parseDueDate(payload, "dueDate");
            Integer priority = getInt(payload, "priority");
            String notes = getString(payload, "notes");
            TodoItem created = todoService.createTodo(getUserId(request), title, dueDate, priority, notes);
            return ResponseEntity.ok(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(errorBody(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTodo(
            HttpServletRequest request,
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload
    ) {
        try {
            boolean titleProvided = payload.containsKey("title");
            boolean dueDateProvided = payload.containsKey("dueDate");
            boolean isCompletedProvided = payload.containsKey("isCompleted");
            boolean priorityProvided = payload.containsKey("priority");
            boolean notesProvided = payload.containsKey("notes");

            String title = titleProvided ? getString(payload, "title") : null;
            LocalDateTime dueDate = parseDueDate(payload, "dueDate");
            Boolean isCompleted = isCompletedProvided ? parseBoolean(payload.get("isCompleted"), "isCompleted") : null;
            Integer priority = priorityProvided ? getInt(payload, "priority") : null;
            String notes = notesProvided ? getString(payload, "notes") : null;

            TodoItem updated = todoService.updateTodo(
                    getUserId(request),
                    id,
                    title,
                    titleProvided,
                    dueDate,
                    dueDateProvided,
                    isCompleted,
                    isCompletedProvided,
                    priority,
                    priorityProvided,
                    notes,
                    notesProvided
            );
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(errorBody(e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(errorBody(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public void deleteTodo(HttpServletRequest request, @PathVariable Long id) {
        todoService.deleteTodo(getUserId(request), id);
    }

    @PostMapping("/{todoId}/subtasks")
    public ResponseEntity<?> createSubtask(
            HttpServletRequest request,
            @PathVariable Long todoId,
            @RequestBody Map<String, Object> payload
    ) {
        try {
            String title = getString(payload, "title");
            TodoSubtask created = todoService.createSubtask(getUserId(request), todoId, title);
            return ResponseEntity.ok(created);
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(errorBody("Forbidden"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(errorBody(e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(errorBody(e.getMessage()));
        }
    }

    @PutMapping("/{todoId}/subtasks/{subtaskId}")
    public ResponseEntity<?> updateSubtask(
            HttpServletRequest request,
            @PathVariable Long todoId,
            @PathVariable Long subtaskId,
            @RequestBody Map<String, Object> payload
    ) {
        try {
            boolean titleProvided = payload.containsKey("title");
            boolean isCompletedProvided = payload.containsKey("isCompleted");

            String title = titleProvided ? getString(payload, "title") : null;
            Boolean isCompleted = isCompletedProvided ? parseBoolean(payload.get("isCompleted"), "isCompleted") : null;

            TodoSubtask updated = todoService.updateSubtask(
                    getUserId(request),
                    todoId,
                    subtaskId,
                    title,
                    titleProvided,
                    isCompleted,
                    isCompletedProvided
            );
            return ResponseEntity.ok(updated);
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(errorBody("Forbidden"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(errorBody(e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(errorBody(e.getMessage()));
        }
    }

    @DeleteMapping("/{todoId}/subtasks/{subtaskId}")
    public ResponseEntity<?> deleteSubtask(
            HttpServletRequest request,
            @PathVariable Long todoId,
            @PathVariable Long subtaskId
    ) {
        try {
            todoService.deleteSubtask(getUserId(request), todoId, subtaskId);
            return ResponseEntity.ok().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(errorBody("Forbidden"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(errorBody(e.getMessage()));
        }
    }

    private LocalDateTime parseDueDate(Map<String, Object> payload, String key) {
        if (!payload.containsKey(key)) {
            return null;
        }
        Object value = payload.get(key);
        if (value == null) {
            return null;
        }
        if (!(value instanceof String)) {
            throw new IllegalArgumentException("dueDate must be a string in yyyy-MM-ddTHH:mm format");
        }

        String raw = ((String) value).trim();
        if (raw.isEmpty()) {
            return null;
        }

        try {
            return LocalDateTime.parse(raw);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid dueDate format. Expected yyyy-MM-ddTHH:mm");
        }
    }

    private String getString(Map<String, Object> payload, String key) {
        if (!payload.containsKey(key)) {
            return null;
        }
        Object value = payload.get(key);
        if (value == null) {
            return null;
        }
        if (!(value instanceof String)) {
            throw new IllegalArgumentException(key + " must be a string");
        }
        return (String) value;
    }

    private Integer getInt(Map<String, Object> payload, String key) {
        if (!payload.containsKey(key)) {
            return null;
        }
        Object value = payload.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Integer) {
            return (Integer) value;
        }
        if (value instanceof String) {
            try {
                return Integer.parseInt(((String) value).trim());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(key + " must be an integer");
            }
        }
        throw new IllegalArgumentException(key + " must be an integer");
    }

    private Boolean parseBoolean(Object value, String fieldName) {
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof String) {
            String normalized = ((String) value).trim().toLowerCase();
            if ("true".equals(normalized)) {
                return true;
            }
            if ("false".equals(normalized)) {
                return false;
            }
        }
        throw new IllegalArgumentException(fieldName + " must be a boolean");
    }

    private Map<String, String> errorBody(String message) {
        Map<String, String> body = new HashMap<>();
        body.put("error", message == null || message.trim().isEmpty() ? "Invalid request" : message);
        return body;
    }
}
