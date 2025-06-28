package com.mdm.agent.mdm;

import com.mdm.agent.core.BaseBotAgent;
import com.mdm.agent.core.AgentRequest;
import com.mdm.agent.core.AgentResponse;

import java.util.List;
import java.util.Map;

public interface MergeAgent extends BaseBotAgent {
    
    /**
     * Merge two entities
     */
    Map<String, Object> mergeEntities(Map<String, Object> entity1, Map<String, Object> entity2);
    
    /**
     * Perform bulk merge of multiple entities
     */
    List<Map<String, Object>> bulkMerge(List<Map<String, Object>> entities);
    
    /**
     * Apply survivorship rules to determine which attributes to keep
     */
    Map<String, Object> applySurvivorshipRules(Map<String, Object> entity1, Map<String, Object> entity2);
    
    /**
     * Get the merge strategy used by this agent
     */
    String getMergeStrategy();
    
    /**
     * Validate if two entities can be merged
     */
    boolean canMerge(Map<String, Object> entity1, Map<String, Object> entity2);
    
    @Override
    default boolean canHandle(AgentRequest request) {
        return "MERGING".equals(request.getOperation()) || 
               "MERGE_ENTITIES".equals(request.getOperation()) ||
               "BULK_MERGE".equals(request.getOperation());
    }
    
    @Override
    default Map<String, Object> getCapabilities() {
        return Map.of(
            "operation", "MERGING",
            "mergeStrategy", getMergeStrategy(),
            "confidenceThreshold", getConfidenceThreshold()
        );
    }
} 