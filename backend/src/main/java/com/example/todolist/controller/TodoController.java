package com.example.todolist.controller;

import com.example.todolist.entity.TodoItem;
import com.example.todolist.service.TodoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/todos")
public class TodoController {

    @Autowired
    private TodoService todoService;

    // 辅助方法：从请求中获取当前登录用户的 ID
    private Long getUserId(HttpServletRequest request) {
        return (Long) request.getAttribute("userId");
    }

    @GetMapping
    public List<TodoItem> getAllTodos(HttpServletRequest request) {
        return todoService.getAllTodos(getUserId(request));
    }

    @PostMapping
    public TodoItem createTodo(HttpServletRequest request, @RequestBody Map<String, String> payload) {
        return todoService.createTodo(getUserId(request), payload.get("title"));
    }

    @PutMapping("/{id}")
    public TodoItem updateTodo(HttpServletRequest request, @PathVariable Long id, @RequestBody Map<String, Boolean> payload) {
        return todoService.updateTodoStatus(getUserId(request), id, payload.get("isCompleted"));
    }

    @DeleteMapping("/{id}")
    public void deleteTodo(HttpServletRequest request, @PathVariable Long id) {
        todoService.deleteTodo(getUserId(request), id);
    }
}
