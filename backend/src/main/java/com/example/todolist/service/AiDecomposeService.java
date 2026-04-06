package com.example.todolist.service;

import com.example.todolist.dto.AiDecomposeResponse;

import java.time.LocalDateTime;

public interface AiDecomposeService {
    AiDecomposeResponse decompose(String goalTitle, LocalDateTime dueDate);
}
