package com.example.todolist.mapper;

import com.example.todolist.dto.AiStatsResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AiCallStatMapper {
    int insert(@Param("status") String status);

    AiStatsResponse getStats();
}
