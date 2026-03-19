package com.example.todolist.service;

import com.example.todolist.entity.User;

public interface UserService {
    User register(String username, String password);
    
    String login(String username, String password);
}
