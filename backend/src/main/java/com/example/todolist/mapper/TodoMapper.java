package com.example.todolist.mapper;

import com.example.todolist.entity.TodoItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface TodoMapper {
    List<TodoItem> findAllByUserId(Long userId);
    
    TodoItem findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);
    
    int insert(TodoItem todoItem);
    
    int update(TodoItem todoItem);
    
    int deleteByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);
}
