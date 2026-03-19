package com.example.todolist.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TodoItem {
    private Long id;
    private Long userId; // 新增 userId 字段
    private String title;
    private Boolean isCompleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
