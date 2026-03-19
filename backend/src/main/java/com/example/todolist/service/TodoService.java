package com.example.todolist.service;

import com.example.todolist.entity.TodoItem;
import java.util.List;

public interface TodoService {
    List<TodoItem> getAllTodos(Long userId);
    
    TodoItem createTodo(Long userId, String title);
    
    TodoItem updateTodoStatus(Long userId, Long id, Boolean isCompleted);
    
    void deleteTodo(Long userId, Long id);
}
