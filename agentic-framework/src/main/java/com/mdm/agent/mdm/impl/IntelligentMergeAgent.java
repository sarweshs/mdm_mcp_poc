package com.mdm.agent.mdm.impl;

import com.mdm.agent.core.AgentRequest;
import com.mdm.agent.core.AgentResponse;
import com.mdm.agent.core.MCPClient;
import com.mdm.agent.mdm.MergeAgent;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
public class IntelligentMergeAgent implements MergeAgent {
    
    private static final String AGENT_ID = "INTELLIGENT_MERGE_AGENT";
    private static final String AGENT_TYPE = "MERGE_AGENT";
    private static final double CONFIDENCE_THRESHOLD = 0.8;
    
    private final MCPClient mcpClient;
    
    public IntelligentMergeAgent(MCPClient mcpClient) {
        this.mcpClient = mcpClient;
    }
    
    public String getAgentId() { return AGENT_ID; }
    public String getAgentType() { return AGENT_TYPE; }
    public double getConfidenceThreshold() { return CONFIDENCE_THRESHOLD; }
    public boolean isEnabled() { return true; }
    
    public AgentResponse execute(AgentRequest request) {
        try {
            if ("MERGE_ENTITIES".equals(request.getOperation())) {
                @SuppressWarnings("unchecked")
                Map<String, Object> entity1 = (Map<String, Object>) request.getParameters().get("entity1");
                @SuppressWarnings("unchecked")
                Map<String, Object> entity2 = (Map<String, Object>) request.getParameters().get("entity2");
                
                if (entity1 == null || entity2 == null) {
                    return AgentResponse.error(
                        request.getRequestId(),
                        getAgentId(),
                        getAgentType(),
                        "Both entities are required for merging"
                    );
                }
                
                Map<String, Object> mergedEntity = mergeEntities(entity1, entity2);
                
                return AgentResponse.success(
                    request.getRequestId(),
                    getAgentId(),
                    getAgentType(),
                    Map.of(
                        "mergedEntity", mergedEntity,
                        "sourceEntities", Arrays.asList(entity1, entity2),
                        "mergeId", UUID.randomUUID().toString()
                    ),
                    0.95
                );
                
            } else if ("BULK_MERGE".equals(request.getOperation())) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> entities = (List<Map<String, Object>>) request.getParameters().get("entities");
                
                if (entities == null || entities.isEmpty()) {
                    return AgentResponse.error(
                        request.getRequestId(),
                        getAgentId(),
                        getAgentType(),
                        "No entities provided for bulk merge"
                    );
                }
                
                List<Map<String, Object>> mergedEntities = bulkMerge(entities);
                
                return AgentResponse.success(
                    request.getRequestId(),
                    getAgentId(),
                    getAgentType(),
                    Map.of(
                        "mergedEntities", mergedEntities,
                        "originalCount", entities.size(),
                        "mergedCount", mergedEntities.size()
                    ),
                    0.9
                );
            }
            
            return AgentResponse.error(
                request.getRequestId(),
                getAgentId(),
                getAgentType(),
                "Unsupported operation: " + request.getOperation()
            );
            
        } catch (Exception e) {
            log.error("Error in intelligent merge agent: {}", e.getMessage(), e);
            return AgentResponse.error(
                request.getRequestId(),
                getAgentId(),
                getAgentType(),
                "Merge failed: " + e.getMessage()
            );
        }
    }
    
    @Override
    public Map<String, Object> mergeEntities(Map<String, Object> entity1, Map<String, Object> entity2) {
        if (!canMerge(entity1, entity2)) {
            throw new IllegalArgumentException("Entities cannot be merged");
        }
        
        Map<String, Object> mergedAttributes = applySurvivorshipRules(entity1, entity2);
        
        Map<String, Object> mergedEntity = new HashMap<>();
        mergedEntity.put("entityId", "MERGED_" + UUID.randomUUID().toString().substring(0, 8));
        mergedEntity.put("entityType", entity1.get("entityType"));
        mergedEntity.put("sourceSystem", "MERGED");
        mergedEntity.put("confidenceScore", Math.max(
            getDoubleValue(entity1, "confidenceScore", 0.0),
            getDoubleValue(entity2, "confidenceScore", 0.0)
        ));
        mergedEntity.put("status", "ACTIVE");
        mergedEntity.put("createdAt", LocalDateTime.now().toString());
        mergedEntity.put("updatedAt", LocalDateTime.now().toString());
        mergedEntity.put("attributes", mergedAttributes);
        mergedEntity.put("sourceEntityIds", Arrays.asList(
            getStringValue(entity1, "entityId"),
            getStringValue(entity2, "entityId")
        ));
        
        log.info("Agent {} merged entities {} and {} (via MCP server)", 
            getAgentId(), 
            getStringValue(entity1, "entityId"), 
            getStringValue(entity2, "entityId"));
        
        return mergedEntity;
    }
    
    @Override
    public List<Map<String, Object>> bulkMerge(List<Map<String, Object>> entities) {
        List<Map<String, Object>> mergedEntities = new ArrayList<>();
        
        // Simple bulk merge - merge pairs of entities
        for (int i = 0; i < entities.size() - 1; i += 2) {
            Map<String, Object> entity1 = entities.get(i);
            Map<String, Object> entity2 = entities.get(i + 1);
            
            if (canMerge(entity1, entity2)) {
                Map<String, Object> merged = mergeEntities(entity1, entity2);
                mergedEntities.add(merged);
            } else {
                // If can't merge, keep both entities
                mergedEntities.add(entity1);
                mergedEntities.add(entity2);
            }
        }
        
        // Handle odd number of entities
        if (entities.size() % 2 != 0) {
            mergedEntities.add(entities.get(entities.size() - 1));
        }
        
        log.info("Agent {} completed bulk merge: {} entities -> {} entities", 
            getAgentId(), entities.size(), mergedEntities.size());
        
        return mergedEntities;
    }
    
    @Override
    public Map<String, Object> applySurvivorshipRules(Map<String, Object> entity1, Map<String, Object> entity2) {
        // Use MCP server for survivorship rules
        return mcpClient.applySurvivorshipRules(entity1, entity2);
    }
    
    private String getStringValue(Map<String, Object> entity, String key) {
        Object value = entity.get(key);
        return value != null ? value.toString() : null;
    }
    
    private double getDoubleValue(Map<String, Object> entity, String key, double defaultValue) {
        Object value = entity.get(key);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return defaultValue;
    }
    
    @Override
    public String getMergeStrategy() {
        return "MCP server-based intelligent merge";
    }
    
    @Override
    public boolean canMerge(Map<String, Object> entity1, Map<String, Object> entity2) {
        // Basic validation
        if (entity1 == null || entity2 == null) {
            return false;
        }
        
        String entityType1 = getStringValue(entity1, "entityType");
        String entityType2 = getStringValue(entity2, "entityType");
        
        // Entities must be of the same type
        if (!Objects.equals(entityType1, entityType2)) {
            return false;
        }
        
        // Check if entities are already merged
        String status1 = getStringValue(entity1, "status");
        String status2 = getStringValue(entity2, "status");
        
        if ("MERGED".equals(status1) || "MERGED".equals(status2)) {
            return false;
        }
        
        return true;
    }
} 