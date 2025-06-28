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