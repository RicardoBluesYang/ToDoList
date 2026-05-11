package com.example.todolist.dto;

import lombok.Data;

import java.util.List;

@Data
public class AiDecomposeCommitRequest {
    private String parentTitle;
    private String dueDate;
    private List<String> subtasks;
}
