package com.example.todolist.entity;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class TodoItem {
    private Long id;
    private Long userId;
    private String title;
    private Boolean isCompleted;
    private Integer priority;
    private String notes;
    private LocalDateTime dueDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<TodoSubtask> subtasks;
}
