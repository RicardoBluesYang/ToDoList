package com.example.todolist.mapper;

import com.example.todolist.dto.AdminOverviewResponse;
import com.example.todolist.dto.AdminUserStatsResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AdminMapper {
    AdminOverviewResponse getOverview();

    List<AdminUserStatsResponse> listUsers();

    String findUsernameById(@Param("id") Long id);

    int deleteUserById(@Param("id") Long id);
}
