package com.example.todolist.service.impl;

import com.example.todolist.entity.TodoItem;
import com.example.todolist.mapper.TodoMapper;
import com.example.todolist.service.TodoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TodoServiceImpl implements TodoService {

    @Autowired
    private TodoMapper todoMapper;

    @Override
    public List<TodoItem> getAllTodos(Long userId) {
        return todoMapper.findAllByUserId(userId);
    }

    @Override
    public TodoItem createTodo(Long userId, String title) {
        TodoItem item = new TodoItem();
        item.setUserId(userId);
        item.setTitle(title);
        item.setIsCompleted(false);
        todoMapper.insert(item);
        return item;
    }

    @Override
    public TodoItem updateTodoStatus(Long userId, Long id, Boolean isCompleted) {
        TodoItem item = new TodoItem();
        item.setId(id);
        item.setUserId(userId);
        item.setIsCompleted(isCompleted);
        todoMapper.update(item);
        return todoMapper.findByIdAndUserId(id, userId);
    }

    @Override
    public void deleteTodo(Long userId, Long id) {
        todoMapper.deleteByIdAndUserId(id, userId);
    }
}
