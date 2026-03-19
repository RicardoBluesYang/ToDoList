package com.example.todolist.mapper;

import com.example.todolist.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {
    User findByUsername(String username);
    
    int insert(User user);
}
