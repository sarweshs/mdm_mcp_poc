package com.mdm.agent.core;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentRequest {
    
    private String requestId;
    private String agentType;
    private String operation;
    private Map<String, Object> parameters;
    private Map<String, Object> context;
    private LocalDateTime timestamp;
    private String userId;
    private String sessionId;
    private Map<String, Object> metadata;
    
    public static AgentRequest create(String agentType, String operation, Map<String, Object> parameters) {
        return AgentRequest.builder()
            .requestId(UUID.randomUUID().toString())
            .agentType(agentType)
            .operation(operation)
            .parameters(parameters)
            .timestamp(LocalDateTime.now())
            .build();
    }
    
    public static AgentRequest create(String agentType, String operation, Map<String, Object> parameters, Map<String, Object> context) {
        return AgentRequest.builder()
            .requestId(UUID.randomUUID().toString())
            .agentType(agentType)
            .operation(operation)
            .parameters(parameters)
            .context(context)
            .timestamp(LocalDateTime.now())
            .build();
    }
} 