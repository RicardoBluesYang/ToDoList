package com.example.todolist.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminUserStatsResponse {
    private Long id;
    private String username;
    private LocalDateTime createdAt;
    private Long todoCount;
    private Long completedTodoCount;
    private Long activeTodoCount;
    private Long overdueTodoCount;
    private Long subtaskCount;
}
