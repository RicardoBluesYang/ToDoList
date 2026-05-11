package com.example.todolist.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AiStatsResponse {
    private Long totalCalls;
    private Long successCalls;
    private Long failedCalls;
    private Double successRate;
    private LocalDateTime latestCallAt;
}
