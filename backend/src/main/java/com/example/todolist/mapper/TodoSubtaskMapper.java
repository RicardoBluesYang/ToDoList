package com.example.todolist.mapper;

import com.example.todolist.entity.TodoSubtask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TodoSubtaskMapper {
    List<TodoSubtask> findAllByUserId(@Param("userId") Long userId);

    List<TodoSubtask> findAllByTodoId(@Param("todoId") Long todoId);

    TodoSubtask findByIdAndTodoId(@Param("id") Long id, @Param("todoId") Long todoId);

    int insert(TodoSubtask subtask);

    int updatePartial(
            @Param("id") Long id,
            @Param("todoId") Long todoId,
            @Param("title") String title,
            @Param("titleProvided") boolean titleProvided,
            @Param("isCompleted") Boolean isCompleted,
            @Param("isCompletedProvided") boolean isCompletedProvided
    );

    int deleteByIdAndTodoId(@Param("id") Long id, @Param("todoId") Long todoId);
}
