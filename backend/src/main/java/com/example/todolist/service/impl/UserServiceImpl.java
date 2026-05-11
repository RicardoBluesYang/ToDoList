package com.example.todolist.service.impl;

import com.example.todolist.entity.User;
import com.example.todolist.mapper.UserMapper;
import com.example.todolist.service.UserService;
import com.example.todolist.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public User register(String username, String password) {
        // 1. 检查用户名是否存在
        User existingUser = userMapper.findByUsername(username);
        if (existingUser != null) {
            throw new RuntimeException("用户名已存在");
        }

        // 2. 创建新用户 (实际项目中密码必须加密，如 BCrypt，为了简便这里先明文)
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(password); // TODO: 应该加密

        userMapper.insert(newUser);
        return newUser;
    }

    @Override
    public String login(String username, String password) {
        System.out.println("---- 进入 login 方法 ----");
        System.out.println("接收到的 username: " + username);
        System.out.println("当前 userMapper 是否为 null: " + (userMapper == null));

        User user = userMapper.findByUsername(username);

        System.out.println("从数据库查出来的 user 是否为 null: " + (user == null));

        if (user == null || !user.getPassword().equals(password)) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 登录成功，生成 JWT Token
        return JwtUtils.generateToken(user.getId(), user.getUsername());
    }
}
