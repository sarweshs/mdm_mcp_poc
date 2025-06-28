package com.mdm.agent.mdm.impl;

import com.mdm.agent.core.AgentRequest;
import com.mdm.agent.core.AgentResponse;
import com.mdm.agent.core.MCPClient;
import com.mdm.agent.mdm.MatchingAgent;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public class IntelligentMatchingAgent implements MatchingAgent {
    
    private static final String AGENT_ID = "INTELLIGENT_MATCHING_AGENT";
    private static final String AGENT_TYPE = "MATCHING_AGENT";
    private static final double CONFIDENCE_THRESHOLD = 0.7;
    
    private final MCPClient mcpClient;
    
    public IntelligentMatchingAgent(MCPClient mcpClient) {
        this.mcpClient = mcpClient;
    }
    
    public String getAgentId() { return AGENT_ID; }
    public String getAgentType() { return AGENT_TYPE; }
    public double getConfidenceThreshold() { return CONFIDENCE_THRESHOLD; }
    public boolean isEnabled() { return true; }
    
    public AgentResponse execute(AgentRequest request) {
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> entities = (List<Map<String, Object>>) request.getParameters().get("entities");
            
            if (entities == null || entities.isEmpty()) {
                return AgentResponse.error(
                    request.getRequestId(),
                    getAgentId(),
                    getAgentType(),
                    "No entities provided for matching"
                );
            }
            
            List<Map<String, Object>> matches = findMatches(entities);
            
            return AgentResponse.success(
                request.getRequestId(),
                getAgentId(),
                getAgentType(),
                Map.of(
                    "matches", matches,
                    "totalEntities", entities.size(),
                    "matchCount", matches.size()
                ),
                0.9
            );
            
        } catch (Exception e) {
            log.error("Error in intelligent matching agent: {}", e.getMessage(), e);
            return AgentResponse.error(
                request.getRequestId(),
                getAgentId(),
                getAgentType(),
                "Matching failed: " + e.getMessage()
            );
        }
    }
    
    @Override
    public List<Map<String, Object>> findMatches(List<Map<String, Object>> entities) {
        // Use MCP server for matching logic
        List<Map<String, Object>> matches = mcpClient.findMatchCandidates(entities);
        log.info("Agent {} found {} matches for {} entities (via MCP server)", getAgentId(), matches.size(), entities.size());
        return matches;
    }
    
    @Override
    public double calculateConfidence(Map<String, Object> entity1, Map<String, Object> entity2) {
        // Use MCP server for confidence calculation
        return mcpClient.calculateConfidence(entity1, entity2);
    }
    
    @Override
    public String getMatchingCriteria() {
        return "MCP server-based intelligent matching";
    }
} 