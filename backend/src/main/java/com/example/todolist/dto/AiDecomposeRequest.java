package com.example.todolist.dto;

import lombok.Data;

@Data
public class AiDecomposeRequest {
    private String goalTitle;
    private String dueDate;
}
