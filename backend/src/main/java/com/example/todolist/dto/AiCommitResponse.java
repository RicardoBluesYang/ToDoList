package com.example.todolist.dto;

import com.example.todolist.entity.TodoItem;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiCommitResponse {
    private TodoItem todo;
}
