package com.mdm.agent.core;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentResponse {
    
    private String requestId;
    private String agentId;
    private String agentType;
    private String status;
    private String message;
    private Object result;
    private double confidenceScore;
    private LocalDateTime timestamp;
    private Long executionTimeMs;
    private Map<String, Object> metadata;
    private Map<String, Object> context;
    
    public static AgentResponse success(String requestId, String agentId, String agentType, Object result) {
        return AgentResponse.builder()
            .requestId(requestId)
            .agentId(agentId)
            .agentType(agentType)
            .status("SUCCESS")
            .message("Operation completed successfully")
            .result(result)
            .confidenceScore(1.0)
            .timestamp(LocalDateTime.now())
            .build();
    }
    
    public static AgentResponse success(String requestId, String agentId, String agentType, Object result, double confidenceScore) {
        return AgentResponse.builder()
            .requestId(requestId)
            .agentId(agentId)
            .agentType(agentType)
            .status("SUCCESS")
            .message("Operation completed successfully")
            .result(result)
            .confidenceScore(confidenceScore)
            .timestamp(LocalDateTime.now())
            .build();
    }
    
    public static AgentResponse error(String requestId, String agentId, String agentType, String errorMessage) {
        return AgentResponse.builder()
            .requestId(requestId)
            .agentId(agentId)
            .agentType(agentType)
            .status("ERROR")
            .message(errorMessage)
            .result(null)
            .confidenceScore(0.0)
            .timestamp(LocalDateTime.now())
            .build();
    }
    
    public static AgentResponse partial(String requestId, String agentId, String agentType, Object result, double confidenceScore, String message) {
        return AgentResponse.builder()
            .requestId(requestId)
            .agentId(agentId)
            .agentType(agentType)
            .status("PARTIAL_SUCCESS")
            .message(message)
            .result(result)
            .confidenceScore(confidenceScore)
            .timestamp(LocalDateTime.now())
            .build();
    }
} 