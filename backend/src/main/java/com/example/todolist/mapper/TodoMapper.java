package com.example.todolist.mapper;

import com.example.todolist.entity.TodoItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface TodoMapper {
    List<TodoItem> findAllByUserId(Long userId);

    TodoItem findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    int insert(TodoItem todoItem);

    int updatePartial(
            @Param("id") Long id,
            @Param("userId") Long userId,
            @Param("title") String title,
            @Param("titleProvided") boolean titleProvided,
            @Param("dueDate") LocalDateTime dueDate,
            @Param("dueDateProvided") boolean dueDateProvided,
            @Param("isCompleted") Boolean isCompleted,
            @Param("isCompletedProvided") boolean isCompletedProvided
    );

    int deleteByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);
}
