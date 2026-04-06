package com.example.todolist.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TodoSubtask {
    private Long id;
    private Long todoId;
    private String title;
    private Boolean isCompleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
