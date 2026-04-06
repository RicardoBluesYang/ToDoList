package com.example.todolist.service.impl;

import com.example.todolist.entity.TodoItem;
import com.example.todolist.entity.TodoSubtask;
import com.example.todolist.mapper.TodoMapper;
import com.example.todolist.mapper.TodoSubtaskMapper;
import com.example.todolist.service.TodoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TodoServiceImpl implements TodoService {

    private static final int TITLE_MAX_LENGTH = 255;

    @Autowired
    private TodoMapper todoMapper;

    @Autowired
    private TodoSubtaskMapper todoSubtaskMapper;

    @Override
    public List<TodoItem> getAllTodos(Long userId) {
        List<TodoItem> todos = todoMapper.findAllByUserId(userId);
        List<TodoSubtask> subtasks = todoSubtaskMapper.findAllByUserId(userId);

        Map<Long, List<TodoSubtask>> subtaskMap = new HashMap<>();
        for (TodoSubtask subtask : subtasks) {
            subtaskMap.computeIfAbsent(subtask.getTodoId(), k -> new ArrayList<>()).add(subtask);
        }

        for (TodoItem todo : todos) {
            todo.setSubtasks(subtaskMap.getOrDefault(todo.getId(), new ArrayList<>()));
        }
        return todos;
    }

    @Override
    public TodoItem createTodo(Long userId, String title) {
        return createTodo(userId, title, null);
    }

    @Override
    public TodoItem createTodo(Long userId, String title, LocalDateTime dueDate) {
        TodoItem item = new TodoItem();
        item.setUserId(userId);
        item.setTitle(normalizeTitle(title));
        item.setIsCompleted(false);
        item.setDueDate(dueDate);
        todoMapper.insert(item);
        item.setSubtasks(new ArrayList<>());
        return item;
    }

    @Override
    public TodoItem updateTodo(
            Long userId,
            Long id,
            String title,
            boolean titleProvided,
            LocalDateTime dueDate,
            boolean dueDateProvided,
            Boolean isCompleted,
            boolean isCompletedProvided
    ) {
        if (!titleProvided && !dueDateProvided && !isCompletedProvided) {
            throw new IllegalArgumentException("No updatable fields provided");
        }

        String normalizedTitle = title;
        if (titleProvided) {
            normalizedTitle = normalizeTitle(title);
        }

        int affectedRows = todoMapper.updatePartial(
                id,
                userId,
                normalizedTitle,
                titleProvided,
                dueDate,
                dueDateProvided,
                isCompleted,
                isCompletedProvided
        );
        if (affectedRows == 0) {
            throw new RuntimeException("Todo not found");
        }

        TodoItem updated = todoMapper.findByIdAndUserId(id, userId);
        if (updated == null) {
            throw new RuntimeException("Todo not found");
        }
        updated.setSubtasks(todoSubtaskMapper.findAllByTodoId(updated.getId()));
        return updated;
    }

    @Override
    public void deleteTodo(Long userId, Long id) {
        todoMapper.deleteByIdAndUserId(id, userId);
    }

    @Override
    public TodoSubtask createSubtask(Long userId, Long todoId, String title) {
        ensureTodoBelongsToUser(userId, todoId);

        TodoSubtask subtask = new TodoSubtask();
        subtask.setTodoId(todoId);
        subtask.setTitle(normalizeTitle(title));
        subtask.setIsCompleted(false);
        todoSubtaskMapper.insert(subtask);
        return subtask;
    }

    @Override
    public TodoSubtask updateSubtask(
            Long userId,
            Long todoId,
            Long subtaskId,
            String title,
            boolean titleProvided,
            Boolean isCompleted,
            boolean isCompletedProvided
    ) {
        if (!titleProvided && !isCompletedProvided) {
            throw new IllegalArgumentException("No updatable fields provided");
        }

        ensureTodoBelongsToUser(userId, todoId);

        String normalizedTitle = title;
        if (titleProvided) {
            normalizedTitle = normalizeTitle(title);
        }

        int affectedRows = todoSubtaskMapper.updatePartial(
                subtaskId,
                todoId,
                normalizedTitle,
                titleProvided,
                isCompleted,
                isCompletedProvided
        );
        if (affectedRows == 0) {
            throw new RuntimeException("Subtask not found");
        }

        TodoSubtask updated = todoSubtaskMapper.findByIdAndTodoId(subtaskId, todoId);
        if (updated == null) {
            throw new RuntimeException("Subtask not found");
        }
        return updated;
    }

    @Override
    public void deleteSubtask(Long userId, Long todoId, Long subtaskId) {
        ensureTodoBelongsToUser(userId, todoId);
        int affectedRows = todoSubtaskMapper.deleteByIdAndTodoId(subtaskId, todoId);
        if (affectedRows == 0) {
            throw new RuntimeException("Subtask not found");
        }
    }

    private void ensureTodoBelongsToUser(Long userId, Long todoId) {
        TodoItem todo = todoMapper.findByIdAndUserId(todoId, userId);
        if (todo == null) {
            throw new SecurityException("Forbidden");
        }
    }

    private String normalizeTitle(String title) {
        if (title == null) {
            throw new IllegalArgumentException("Title cannot be empty");
        }
        String normalized = title.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("Title cannot be empty");
        }
        if (normalized.length() > TITLE_MAX_LENGTH) {
            throw new IllegalArgumentException("Title length exceeds 255 characters");
        }
        return normalized;
    }
}
