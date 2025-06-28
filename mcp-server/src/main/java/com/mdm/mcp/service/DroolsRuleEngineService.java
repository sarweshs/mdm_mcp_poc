package com.mdm.mcp.service;

import com.mdm.mcp.model.DataEntity;
import com.mdm.mcp.model.MergeResult;
import com.mdm.mcp.model.MatchCandidate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class DroolsRuleEngineService {
    
    private boolean droolsAvailable = false;
    
    public DroolsRuleEngineService() {
        try {
            // Try to initialize Drools
            Class.forName("org.kie.api.KieServices");
            droolsAvailable = true;
            log.info("Drools rule engine is available");
        } catch (ClassNotFoundException e) {
            log.warn("Drools not available, using fallback rule engine");
            droolsAvailable = false;
        }
    }
    
    // New methods for MCP server endpoints
    
    public List<Map<String, Object>> findMatchCandidates(List<Map<String, Object>> entities) {
        log.info("Finding match candidates for {} entities", entities.size());
        
        List<Map<String, Object>> matches = new ArrayList<>();
        
        // Simple fallback logic: find entities with same email
        for (int i = 0; i < entities.size(); i++) {
            for (int j = i + 1; j < entities.size(); j++) {
                Map<String, Object> entity1 = entities.get(i);
                Map<String, Object> entity2 = entities.get(j);
                
                @SuppressWarnings("unchecked")
                Map<String, Object> attrs1 = (Map<String, Object>) entity1.get("attributes");
                @SuppressWarnings("unchecked")
                Map<String, Object> attrs2 = (Map<String, Object>) entity2.get("attributes");
                
                String email1 = attrs1 != null ? (String) attrs1.get("email") : null;
                String email2 = attrs2 != null ? (String) attrs2.get("email") : null;
                
                if (email1 != null && email1.equals(email2)) {
                    Map<String, Object> match = new HashMap<>();
                    match.put("entity1", entity1);
                    match.put("entity2", entity2);
                    match.put("confidenceScore", 0.85);
                    match.put("matchReason", "Email match");
                    match.put("ruleName", "FALLBACK_EMAIL_RULE");
                    matches.add(match);
                }
            }
        }
        
        log.info("Fallback match candidates found: {} candidates", matches.size());
        return matches;
    }
    
    public double calculateConfidence(Map<String, Object> entity1, Map<String, Object> entity2) {
        @SuppressWarnings("unchecked")
        Map<String, Object> attrs1 = (Map<String, Object>) entity1.get("attributes");
        @SuppressWarnings("unchecked")
        Map<String, Object> attrs2 = (Map<String, Object>) entity2.get("attributes");
        
        if (attrs1 == null || attrs2 == null) {
            return 0.0;
        }
        
        double confidence = 0.0;
        int matchCount = 0;
        int totalFields = 0;
        
        // Check email
        String email1 = (String) attrs1.get("email");
        String email2 = (String) attrs2.get("email");
        if (email1 != null && email2 != null) {
            totalFields++;
            if (email1.equalsIgnoreCase(email2)) {
                matchCount++;
                confidence += 0.4; // Email is worth 40%
            }
        }
        
        // Check firstName
        String firstName1 = (String) attrs1.get("firstName");
        String firstName2 = (String) attrs2.get("firstName");
        if (firstName1 != null && firstName2 != null) {
            totalFields++;
            if (firstName1.equalsIgnoreCase(firstName2)) {
                matchCount++;
                confidence += 0.3; // First name is worth 30%
            }
        }
        
        // Check lastName
        String lastName1 = (String) attrs1.get("lastName");
        String lastName2 = (String) attrs2.get("lastName");
        if (lastName1 != null && lastName2 != null) {
            totalFields++;
            if (lastName1.equalsIgnoreCase(lastName2)) {
                matchCount++;
                confidence += 0.3; // Last name is worth 30%
            }
        }
        
        return Math.min(confidence, 1.0);
    }
    
    public Map<String, Object> mergeEntities(Map<String, Object> entity1, Map<String, Object> entity2) {
        log.info("Merging entities: {} and {}", entity1.get("entityId"), entity2.get("entityId"));
        
        Map<String, Object> mergedEntity = new HashMap<>();
        mergedEntity.put("entityId", "MERGED_" + UUID.randomUUID().toString().substring(0, 8));
        mergedEntity.put("entityType", entity1.get("entityType"));
        mergedEntity.put("sourceSystem", "MERGED");
        mergedEntity.put("status", "ACTIVE");
        mergedEntity.put("createdAt", LocalDateTime.now().toString());
        mergedEntity.put("updatedAt", LocalDateTime.now().toString());
        
        // Apply survivorship rules
        Map<String, Object> mergedAttributes = applySurvivorshipRules(entity1, entity2);
        mergedEntity.put("attributes", mergedAttributes);
        
        // Add source entity IDs
        List<String> sourceIds = new ArrayList<>();
        sourceIds.add((String) entity1.get("entityId"));
        sourceIds.add((String) entity2.get("entityId"));
        mergedEntity.put("sourceEntityIds", sourceIds);
        
        return mergedEntity;
    }
    
    public Map<String, Object> applySurvivorshipRules(Map<String, Object> entity1, Map<String, Object> entity2) {
        @SuppressWarnings("unchecked")
        Map<String, Object> attrs1 = (Map<String, Object>) entity1.get("attributes");
        @SuppressWarnings("unchecked")
        Map<String, Object> attrs2 = (Map<String, Object>) entity2.get("attributes");
        
        Map<String, Object> mergedAttributes = new HashMap<>();
        
        if (attrs1 != null && attrs2 != null) {
            // Simple survivorship: prefer non-null values, then prefer entity1
            for (String key : attrs1.keySet()) {
                Object value1 = attrs1.get(key);
                Object value2 = attrs2.get(key);
                
                if (value1 != null && !value1.toString().trim().isEmpty()) {
                    mergedAttributes.put(key, value1);
                } else if (value2 != null && !value2.toString().trim().isEmpty()) {
                    mergedAttributes.put(key, value2);
                }
            }
            
            // Add any attributes from entity2 that aren't in entity1
            for (String key : attrs2.keySet()) {
                if (!mergedAttributes.containsKey(key)) {
                    Object value2 = attrs2.get(key);
                    if (value2 != null && !value2.toString().trim().isEmpty()) {
                        mergedAttributes.put(key, value2);
                    }
                }
            }
        }
        
        return mergedAttributes;
    }
    
    public Map<String, Object> getRules(String ruleType) {
        Map<String, Object> rules = new HashMap<>();
        
        switch (ruleType.toLowerCase()) {
            case "matching":
                rules.put("email_match", "Match entities with same email");
                rules.put("name_match", "Match entities with same first and last name");
                rules.put("phone_match", "Match entities with same phone number");
                break;
            case "survivorship":
                rules.put("prefer_non_null", "Prefer non-null values");
                rules.put("prefer_source1", "Prefer values from source entity 1");
                rules.put("prefer_most_recent", "Prefer most recent values");
                break;
            case "merge":
                rules.put("same_type_only", "Only merge entities of same type");
                rules.put("confidence_threshold", "Require minimum confidence score");
                break;
            default:
                rules.put("default", "Default rule");
        }
        
        return rules;
    }
    
    public Map<String, Object> executeRule(String ruleName, Map<String, Object> facts) {
        Map<String, Object> result = new HashMap<>();
        result.put("ruleName", ruleName);
        result.put("executed", true);
        result.put("timestamp", LocalDateTime.now().toString());
        
        // Simple rule execution logic
        switch (ruleName) {
            case "email_match":
                String email1 = (String) facts.get("email1");
                String email2 = (String) facts.get("email2");
                result.put("match", email1 != null && email1.equals(email2));
                result.put("confidence", email1 != null && email1.equals(email2) ? 0.9 : 0.0);
                break;
            case "name_match":
                String name1 = (String) facts.get("name1");
                String name2 = (String) facts.get("name2");
                result.put("match", name1 != null && name1.equalsIgnoreCase(name2));
                result.put("confidence", name1 != null && name1.equalsIgnoreCase(name2) ? 0.8 : 0.0);
                break;
            default:
                result.put("match", false);
                result.put("confidence", 0.0);
        }
        
        return result;
    }
    
    // Original methods for backward compatibility
    
    public List<MergeResult> executeEntityMerging(List<DataEntity> entities) {
        log.info("Starting entity merging process for {} entities", entities.size());
        
        if (!droolsAvailable) {
            return executeFallbackMerging(entities);
        }
        
        // For now, return fallback behavior since Drools initialization is complex
        return executeFallbackMerging(entities);
    }
    
    public List<MatchCandidate> findMatchCandidates(List<DataEntity> entities) {
        log.info("Finding match candidates for {} entities", entities.size());
        
        if (!droolsAvailable) {
            return findFallbackMatchCandidates(entities);
        }
        
        // For now, return fallback behavior
        return findFallbackMatchCandidates(entities);
    }
    
    public MergeResult mergeEntities(DataEntity entity1, DataEntity entity2) {
        log.info("Merging entities: {} and {}", entity1.getEntityId(), entity2.getEntityId());
        
        if (!droolsAvailable) {
            return mergeEntitiesFallback(entity1, entity2);
        }
        
        // For now, return fallback behavior
        return mergeEntitiesFallback(entity1, entity2);
    }
    
    private List<MergeResult> executeFallbackMerging(List<DataEntity> entities) {
        List<MergeResult> results = new ArrayList<>();
        
        // Simple fallback logic: find entities with same email and merge them
        for (int i = 0; i < entities.size(); i++) {
            for (int j = i + 1; j < entities.size(); j++) {
                DataEntity entity1 = entities.get(i);
                DataEntity entity2 = entities.get(j);
                
                String email1 = entity1.getAttributes().get("email");
                String email2 = entity2.getAttributes().get("email");
                
                if (email1 != null && email1.equals(email2)) {
                    MergeResult result = MergeResult.builder()
                        .mergeId(UUID.randomUUID().toString())
                        .status("MERGED")
                        .message("Entities merged based on email match")
                        .sourceEntityIds(List.of(entity1.getEntityId(), entity2.getEntityId()))
                        .timestamp(LocalDateTime.now())
                        .survivorshipDecisions(new HashMap<>() {{
                            put("email", entity1.getEntityId() + ":FALLBACK_RULE");
                            put("firstName", entity1.getEntityId() + ":FALLBACK_RULE");
                            put("lastName", entity1.getEntityId() + ":FALLBACK_RULE");
                        }})
                        .build();
                    results.add(result);
                }
            }
        }
        
        log.info("Fallback merging completed: {} results", results.size());
        return results;
    }
    
    private List<MatchCandidate> findFallbackMatchCandidates(List<DataEntity> entities) {
        List<MatchCandidate> candidates = new ArrayList<>();
        
        // Simple fallback logic: find entities with same email
        for (int i = 0; i < entities.size(); i++) {
            for (int j = i + 1; j < entities.size(); j++) {
                DataEntity entity1 = entities.get(i);
                DataEntity entity2 = entities.get(j);
                
                String email1 = entity1.getAttributes().get("email");
                String email2 = entity2.getAttributes().get("email");
                
                if (email1 != null && email1.equals(email2)) {
                    MatchCandidate candidate = MatchCandidate.builder()
                        .entity1(entity1)
                        .entity2(entity2)
                        .confidenceScore(0.85)
                        .matchReason("Email match")
                        .ruleName("FALLBACK_EMAIL_RULE")
                        .build();
                    candidates.add(candidate);
                }
            }
        }
        
        log.info("Fallback match candidates found: {} candidates", candidates.size());
        return candidates;
    }
    
    private MergeResult mergeEntitiesFallback(DataEntity entity1, DataEntity entity2) {
        String email1 = entity1.getAttributes().get("email");
        String email2 = entity2.getAttributes().get("email");
        
        if (email1 != null && email1.equals(email2)) {
            return MergeResult.builder()
                .mergeId(UUID.randomUUID().toString())
                .status("MERGED")
                .message("Entities merged based on email match")
                .sourceEntityIds(List.of(entity1.getEntityId(), entity2.getEntityId()))
                .timestamp(LocalDateTime.now())
                .survivorshipDecisions(new HashMap<>() {{
                    put("email", entity1.getEntityId() + ":FALLBACK_RULE");
                    put("firstName", entity1.getEntityId() + ":FALLBACK_RULE");
                    put("lastName", entity1.getEntityId() + ":FALLBACK_RULE");
                }})
                .build();
        } else {
            return MergeResult.builder()
                .mergeId(UUID.randomUUID().toString())
                .status("NO_MATCH")
                .message("No matching criteria found for these entities")
                .sourceEntityIds(List.of(entity1.getEntityId(), entity2.getEntityId()))
                .timestamp(LocalDateTime.now())
                .build();
        }
    }
    
    public void reloadRules() {
        log.info("Reloading rules (fallback mode)");
        // In fallback mode, nothing to reload
    }
    
    public boolean isDroolsAvailable() {
        return droolsAvailable;
    }
} 