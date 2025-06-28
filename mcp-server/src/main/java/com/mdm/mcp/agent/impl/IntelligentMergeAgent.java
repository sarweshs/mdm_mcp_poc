package com.mdm.mcp.agent.impl;

import com.mdm.mcp.agent.MergeAgent;
import com.mdm.mcp.model.DataEntity;
import com.mdm.mcp.model.MatchCandidate;
import com.mdm.mcp.model.MergeResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class IntelligentMergeAgent implements MergeAgent {
    
    private static final String AGENT_ID = "INTELLIGENT_MERGE_AGENT";
    private static final String AGENT_TYPE = "MERGE_AGENT";
    private static final double CONFIDENCE_THRESHOLD = 0.8;
    
    @Override
    public String getAgentId() {
        return AGENT_ID;
    }
    
    @Override
    public String getAgentType() {
        return AGENT_TYPE;
    }
    
    @Override
    public double getConfidenceThreshold() {
        return CONFIDENCE_THRESHOLD;
    }
    
    @Override
    public boolean isEnabled() {
        return true;
    }
    
    @Override
    public MergeResult mergeEntities(MatchCandidate matchCandidate) {
        long startTime = System.currentTimeMillis();
        
        try {
            DataEntity entity1 = matchCandidate.getEntity1();
            DataEntity entity2 = matchCandidate.getEntity2();
            
            if (!canMerge(entity1, entity2)) {
                return createFailedMergeResult(matchCandidate, "Entities cannot be merged - validation failed");
            }
            
            // Apply survivorship rules
            Map<String, Object> mergedAttributes = applySurvivorshipRules(entity1, entity2);
            
            // Create merged entity
            DataEntity mergedEntity = createMergedEntity(entity1, entity2, mergedAttributes);
            
            long executionTime = System.currentTimeMillis() - startTime;
            
            Map<String, Object> ruleExecutionDetails = new HashMap<>();
            ruleExecutionDetails.put("agent", AGENT_ID);
            ruleExecutionDetails.put("strategy", getMergeStrategy());
            ruleExecutionDetails.put("executionTimeMs", executionTime);
            
            MergeResult result = MergeResult.builder()
                .mergeId(UUID.randomUUID().toString())
                .status("MERGED")
                .message("Entities merged successfully by " + AGENT_ID)
                .timestamp(LocalDateTime.now())
                .sourceEntityIds(Arrays.asList(entity1.getEntityId(), entity2.getEntityId()))
                .mergedEntityId(mergedEntity.getEntityId())
                .duplicateEntityIds(Arrays.asList(entity1.getEntityId(), entity2.getEntityId()))
                .appliedRuleName("INTELLIGENT_MERGE_RULE")
                .confidenceScore(matchCandidate.getConfidenceScore())
                .ruleExecutionDetails(ruleExecutionDetails)
                .survivorshipDecisions(createSurvivorshipDecisions(mergedAttributes))
                .relationshipChanges(null)
                .build();
            
            log.info("Agent {} successfully merged entities {} and {} in {}ms", 
                AGENT_ID, entity1.getEntityId(), entity2.getEntityId(), executionTime);
            
            return result;
            
        } catch (Exception e) {
            log.error("Agent {} failed to merge entities: {}", AGENT_ID, e.getMessage(), e);
            return createFailedMergeResult(matchCandidate, "Merge failed: " + e.getMessage());
        }
    }
    
    @Override
    public List<MergeResult> bulkMerge(List<MatchCandidate> matchCandidates) {
        List<MergeResult> results = new ArrayList<>();
        
        for (MatchCandidate candidate : matchCandidates) {
            if (candidate.getConfidenceScore() >= CONFIDENCE_THRESHOLD) {
                MergeResult result = mergeEntities(candidate);
                results.add(result);
            } else {
                log.warn("Agent {} skipping merge for low confidence match: {}", 
                    AGENT_ID, candidate.getConfidenceScore());
            }
        }
        
        log.info("Agent {} completed bulk merge of {} candidates, {} successful", 
            AGENT_ID, matchCandidates.size(), results.size());
        
        return results;
    }
    
    @Override
    public Map<String, Object> applySurvivorshipRules(DataEntity entity1, DataEntity entity2) {
        Map<String, Object> mergedAttributes = new HashMap<>();
        Map<String, String> attributes1 = entity1.getAttributes() != null ? entity1.getAttributes() : new HashMap<>();
        Map<String, String> attributes2 = entity2.getAttributes() != null ? entity2.getAttributes() : new HashMap<>();
        
        // Combine all unique attributes
        Set<String> allKeys = new HashSet<>();
        allKeys.addAll(attributes1.keySet());
        allKeys.addAll(attributes2.keySet());
        
        for (String key : allKeys) {
            String value1 = attributes1.get(key);
            String value2 = attributes2.get(key);
            
            Object selectedValue = selectBestValue(key, value1, value2, entity1, entity2);
            mergedAttributes.put(key, selectedValue);
        }
        
        return mergedAttributes;
    }
    
    private Object selectBestValue(String attributeName, String value1, String value2, 
                                 DataEntity entity1, DataEntity entity2) {
        // If only one value exists, use it
        if (value1 == null && value2 != null) {
            return value2;
        }
        if (value2 == null && value1 != null) {
            return value1;
        }
        if (value1 == null && value2 == null) {
            return null;
        }
        
        // If values are identical, use either
        if (Objects.equals(value1, value2)) {
            return value1;
        }
        
        // Apply survivorship rules based on attribute type
        switch (attributeName.toLowerCase()) {
            case "email":
                return selectEmail(value1, value2, entity1, entity2);
            case "phone":
                return selectPhone(value1, value2, entity1, entity2);
            case "firstname":
            case "lastname":
                return selectName(value1, value2, entity1, entity2);
            case "address":
                return selectAddress(value1, value2, entity1, entity2);
            default:
                return selectByConfidence(value1, value2, entity1, entity2);
        }
    }
    
    private Object selectEmail(String value1, String value2, DataEntity entity1, DataEntity entity2) {
        // Prefer email from higher confidence entity
        if (entity1.getConfidenceScore() > entity2.getConfidenceScore()) {
            return value1;
        } else if (entity2.getConfidenceScore() > entity1.getConfidenceScore()) {
            return value2;
        }
        
        // If confidence is equal, prefer the more complete email
        if (value1.contains("@") && !value2.contains("@")) {
            return value1;
        }
        if (value2.contains("@") && !value1.contains("@")) {
            return value2;
        }
        
        // Default to first entity
        return value1;
    }
    
    private Object selectPhone(String value1, String value2, DataEntity entity1, DataEntity entity2) {
        // Prefer phone from higher confidence entity
        if (entity1.getConfidenceScore() > entity2.getConfidenceScore()) {
            return value1;
        } else if (entity2.getConfidenceScore() > entity1.getConfidenceScore()) {
            return value2;
        }
        
        // If confidence is equal, prefer the longer phone number (more complete)
        if (value1.length() > value2.length()) {
            return value1;
        } else if (value2.length() > value1.length()) {
            return value2;
        }
        
        // Default to first entity
        return value1;
    }
    
    private Object selectName(String value1, String value2, DataEntity entity1, DataEntity entity2) {
        // Prefer name from higher confidence entity
        if (entity1.getConfidenceScore() > entity2.getConfidenceScore()) {
            return value1;
        } else if (entity2.getConfidenceScore() > entity1.getConfidenceScore()) {
            return value2;
        }
        
        // If confidence is equal, prefer the longer name (more complete)
        if (value1.length() > value2.length()) {
            return value1;
        } else if (value2.length() > value1.length()) {
            return value2;
        }
        
        // Default to first entity
        return value1;
    }
    
    private Object selectAddress(String value1, String value2, DataEntity entity1, DataEntity entity2) {
        // Prefer address from higher confidence entity
        if (entity1.getConfidenceScore() > entity2.getConfidenceScore()) {
            return value1;
        } else if (entity2.getConfidenceScore() > entity1.getConfidenceScore()) {
            return value2;
        }
        
        // If confidence is equal, prefer the longer address (more complete)
        if (value1.length() > value2.length()) {
            return value1;
        } else if (value2.length() > value1.length()) {
            return value2;
        }
        
        // Default to first entity
        return value1;
    }
    
    private Object selectByConfidence(String value1, String value2, DataEntity entity1, DataEntity entity2) {
        // Prefer value from higher confidence entity
        if (entity1.getConfidenceScore() > entity2.getConfidenceScore()) {
            return value1;
        } else if (entity2.getConfidenceScore() > entity1.getConfidenceScore()) {
            return value2;
        }
        
        // If confidence is equal, prefer the longer value (more complete)
        if (value1.length() > value2.length()) {
            return value1;
        } else if (value2.length() > value1.length()) {
            return value2;
        }
        
        // Default to first entity
        return value1;
    }
    
    private DataEntity createMergedEntity(DataEntity entity1, DataEntity entity2, Map<String, Object> mergedAttributes) {
        // Convert Map<String, Object> to Map<String, String> for DataEntity
        Map<String, String> stringAttributes = new HashMap<>();
        for (Map.Entry<String, Object> entry : mergedAttributes.entrySet()) {
            if (entry.getValue() != null) {
                stringAttributes.put(entry.getKey(), entry.getValue().toString());
            }
        }
        
        return DataEntity.builder()
            .entityType(entity1.getEntityType())
            .sourceSystem("MERGED")
            .confidenceScore(Math.max(entity1.getConfidenceScore(), entity2.getConfidenceScore()))
            .status("ACTIVE")
            .attributes(stringAttributes)
            .build();
    }
    
    private Map<String, String> createSurvivorshipDecisions(Map<String, Object> mergedAttributes) {
        return mergedAttributes.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> "Selected from merged attributes"
            ));
    }
    
    private MergeResult createFailedMergeResult(MatchCandidate matchCandidate, String reason) {
        return MergeResult.builder()
            .mergeId(UUID.randomUUID().toString())
            .status("FAILED")
            .message(reason)
            .timestamp(LocalDateTime.now())
            .sourceEntityIds(Arrays.asList(
                matchCandidate.getEntity1().getEntityId(), 
                matchCandidate.getEntity2().getEntityId()))
            .mergedEntityId(null)
            .duplicateEntityIds(null)
            .appliedRuleName("INTELLIGENT_MERGE_RULE")
            .confidenceScore(matchCandidate.getConfidenceScore())
            .ruleExecutionDetails(Map.of("error", reason))
            .survivorshipDecisions(null)
            .relationshipChanges(null)
            .build();
    }
    
    @Override
    public String getMergeStrategy() {
        return "Confidence-based survivorship with attribute-specific rules";
    }
    
    @Override
    public boolean canMerge(DataEntity entity1, DataEntity entity2) {
        // Basic validation - entities must be of the same type
        return entity1.getEntityType().equals(entity2.getEntityType());
    }
} 