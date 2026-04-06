package com.example.todolist.service;

import com.example.todolist.entity.TodoItem;
import com.example.todolist.entity.TodoSubtask;

import java.time.LocalDateTime;
import java.util.List;

public interface TodoService {
    List<TodoItem> getAllTodos(Long userId);

    TodoItem createTodo(Long userId, String title);

    TodoItem createTodo(Long userId, String title, LocalDateTime dueDate);

    TodoItem updateTodo(
            Long userId,
            Long id,
            String title,
            boolean titleProvided,
            LocalDateTime dueDate,
            boolean dueDateProvided,
            Boolean isCompleted,
            boolean isCompletedProvided
    );

    void deleteTodo(Long userId, Long id);

    TodoSubtask createSubtask(Long userId, Long todoId, String title);

    TodoSubtask updateSubtask(
            Long userId,
            Long todoId,
            Long subtaskId,
            String title,
            boolean titleProvided,
            Boolean isCompleted,
            boolean isCompletedProvided
    );

    void deleteSubtask(Long userId, Long todoId, Long subtaskId);
}
