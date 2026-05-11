package com.example.todolist.dto;

import lombok.Data;

@Data
public class AdminOverviewResponse {
    private Long totalUsers;
    private Long totalTodos;
    private Long completedTodos;
    private Long activeTodos;
    private Long overdueTodos;
    private Long totalSubtasks;
    private Double completionRate;
}
