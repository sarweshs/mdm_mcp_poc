package com.mdm.mcp.agent.impl;

import com.mdm.mcp.agent.MergeAgent;
import com.mdm.mcp.model.DataEntity;
import com.mdm.mcp.model.MatchCandidate;
import com.mdm.mcp.model.MergeResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;

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
                .ruleExecutionDetails("Agent: " + AGENT_ID + ", Strategy: " + getMergeStrategy())
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
        Map<String, Object> attributes1 = entity1.getAttributes() != null ? entity1.getAttributes() : new HashMap<>();
        Map<String, Object> attributes2 = entity2.getAttributes() != null ? entity2.getAttributes() : new HashMap<>();
        
        // Combine all unique attributes
        Set<String> allKeys = new HashSet<>();
        allKeys.addAll(attributes1.keySet());
        allKeys.addAll(attributes2.keySet());
        
        for (String key : allKeys) {
            Object value1 = attributes1.get(key);
            Object value2 = attributes2.get(key);
            
            Object selectedValue = selectBestValue(key, value1, value2, entity1, entity2);
            mergedAttributes.put(key, selectedValue);
        }
        
        return mergedAttributes;
    }
    
    private Object selectBestValue(String attributeName, Object value1, Object value2, 
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
    
    private Object selectEmail(Object value1, Object value2, DataEntity entity1, DataEntity entity2) {
        String email1 = String.valueOf(value1);
        String email2 = String.valueOf(value2);
        
        // Prefer email from higher confidence entity
        if (entity1.getConfidenceScore() > entity2.getConfidenceScore()) {
            return email1;
        } else if (entity2.getConfidenceScore() > entity1.getConfidenceScore()) {
            return email2;
        }
        
        // If confidence is equal, prefer the more complete email
        if (email1.contains("@") && !email2.contains("@")) {
            return email1;
        }
        if (email2.contains("@") && !email1.contains("@")) {
            return email2;
        }
        
        // Default to first entity
        return email1;
    }
    
    private Object selectPhone(Object value1, Object value2, DataEntity entity1, DataEntity entity2) {
        String phone1 = String.valueOf(value1);
        String phone2 = String.valueOf(value2);
        
        // Prefer phone from higher confidence entity
        if (entity1.getConfidenceScore() > entity2.getConfidenceScore()) {
            return phone1;
        } else if (entity2.getConfidenceScore() > entity1.getConfidenceScore()) {
            return phone2;
        }
        
        // If confidence is equal, prefer the longer phone number (more complete)
        if (phone1.length() > phone2.length()) {
            return phone1;
        } else if (phone2.length() > phone1.length()) {
            return phone2;
        }
        
        // Default to first entity
        return phone1;
    }
    
    private Object selectName(Object value1, Object value2, DataEntity entity1, DataEntity entity2) {
        String name1 = String.valueOf(value1);
        String name2 = String.valueOf(value2);
        
        // Prefer name from higher confidence entity
        if (entity1.getConfidenceScore() > entity2.getConfidenceScore()) {
            return name1;
        } else if (entity2.getConfidenceScore() > entity1.getConfidenceScore()) {
            return name2;
        }
        
        // If confidence is equal, prefer the longer name (more complete)
        if (name1.length() > name2.length()) {
            return name1;
        } else if (name2.length() > name1.length()) {
            return name2;
        }
        
        // Default to first entity
        return name1;
    }
    
    private Object selectAddress(Object value1, Object value2, DataEntity entity1, DataEntity entity2) {
        String address1 = String.valueOf(value1);
        String address2 = String.valueOf(value2);
        
        // Prefer address from higher confidence entity
        if (entity1.getConfidenceScore() > entity2.getConfidenceScore()) {
            return address1;
        } else if (entity2.getConfidenceScore() > entity1.getConfidenceScore()) {
            return address2;
        }
        
        // If confidence is equal, prefer the longer address (more complete)
        if (address1.length() > address2.length()) {
            return address1;
        } else if (address2.length() > address1.length()) {
            return address2;
        }
        
        // Default to first entity
        return address1;
    }
    
    private Object selectByConfidence(Object value1, Object value2, DataEntity entity1, DataEntity entity2) {
        // Default rule: prefer value from higher confidence entity
        if (entity1.getConfidenceScore() > entity2.getConfidenceScore()) {
            return value1;
        } else if (entity2.getConfidenceScore() > entity1.getConfidenceScore()) {
            return value2;
        }
        
        // If confidence is equal, prefer first entity
        return value1;
    }
    
    private DataEntity createMergedEntity(DataEntity entity1, DataEntity entity2, Map<String, Object> mergedAttributes) {
        return DataEntity.builder()
            .entityId("MERGED_" + UUID.randomUUID().toString().substring(0, 8))
            .entityType(entity1.getEntityType())
            .sourceSystem("MERGED")
            .confidenceScore(Math.max(entity1.getConfidenceScore(), entity2.getConfidenceScore()))
            .status("ACTIVE")
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .attributes(mergedAttributes)
            .relationships(null)
            .build();
    }
    
    private Map<String, String> createSurvivorshipDecisions(Map<String, Object> mergedAttributes) {
        Map<String, String> decisions = new HashMap<>();
        for (String key : mergedAttributes.keySet()) {
            decisions.put(key, "MERGED_ATTRIBUTE");
        }
        return decisions;
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
            .ruleExecutionDetails("Agent: " + AGENT_ID + " - " + reason)
            .survivorshipDecisions(null)
            .relationshipChanges(null)
            .build();
    }
    
    @Override
    public String getMergeStrategy() {
        return "Intelligent survivorship with confidence-based selection";
    }
    
    @Override
    public boolean canMerge(DataEntity entity1, DataEntity entity2) {
        // Basic validation
        if (entity1 == null || entity2 == null) {
            return false;
        }
        
        if (entity1.getEntityType() == null || entity2.getEntityType() == null) {
            return false;
        }
        
        // Entities must be of the same type
        if (!entity1.getEntityType().equals(entity2.getEntityType())) {
            return false;
        }
        
        // Entities must have different IDs
        if (entity1.getEntityId().equals(entity2.getEntityId())) {
            return false;
        }
        
        return true;
    }
} 