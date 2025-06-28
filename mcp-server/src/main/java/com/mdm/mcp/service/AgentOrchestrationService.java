package com.mdm.mcp.service;

import com.mdm.mcp.agent.MatchingAgent;
import com.mdm.mcp.agent.MergeAgent;
import com.mdm.mcp.model.DataEntity;
import com.mdm.mcp.model.MatchCandidate;
import com.mdm.mcp.model.MergeResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class AgentOrchestrationService {
    
    @Autowired
    private List<MatchingAgent> matchingAgents;
    
    @Autowired
    private List<MergeAgent> mergeAgents;
    
    @Autowired
    private AuditService auditService;
    
    /**
     * Find matches using all available matching agents
     */
    public List<MatchCandidate> findMatchesWithAgents(List<DataEntity> entities) {
        List<MatchCandidate> allMatches = new ArrayList<>();
        
        for (MatchingAgent agent : matchingAgents) {
            if (!agent.isEnabled()) {
                log.info("Skipping disabled matching agent: {}", agent.getAgentId());
                continue;
            }
            
            long startTime = System.currentTimeMillis();
            
            try {
                List<MatchCandidate> matches = agent.findAllMatches(entities);
                
                // Log each match found
                for (MatchCandidate match : matches) {
                    long executionTime = System.currentTimeMillis() - startTime;
                    auditService.logMatchFound(agent, match, executionTime);
                }
                
                allMatches.addAll(matches);
                
                long totalExecutionTime = System.currentTimeMillis() - startTime;
                auditService.logBulkOperation(
                    agent, 
                    "MATCHING_COMPLETED", 
                    entities.size(), 
                    matches.size(), 
                    totalExecutionTime, 
                    "Found " + matches.size() + " matches"
                );
                
                log.info("Agent {} found {} matches in {}ms", agent.getAgentId(), matches.size(), totalExecutionTime);
                
            } catch (Exception e) {
                log.error("Agent {} failed to find matches: {}", agent.getAgentId(), e.getMessage(), e);
                auditService.logAgentActivity(
                    agent,
                    "MATCHING_FAILED",
                    "ALL_ENTITIES",
                    null,
                    "Matching failed: " + e.getMessage(),
                    "FAILED",
                    System.currentTimeMillis() - startTime,
                    Map.of("error", e.getMessage())
                );
            }
        }
        
        // Remove duplicates (same entity pairs)
        allMatches = removeDuplicateMatches(allMatches);
        
        log.info("Total unique matches found by all agents: {}", allMatches.size());
        return allMatches;
    }
    
    /**
     * Merge entities using all available merge agents
     */
    public List<MergeResult> mergeEntitiesWithAgents(List<MatchCandidate> matchCandidates) {
        List<MergeResult> allResults = new ArrayList<>();
        
        for (MergeAgent agent : mergeAgents) {
            if (!agent.isEnabled()) {
                log.info("Skipping disabled merge agent: {}", agent.getAgentId());
                continue;
            }
            
            long startTime = System.currentTimeMillis();
            
            try {
                // Log merge initiation for each candidate
                for (MatchCandidate candidate : matchCandidates) {
                    auditService.logMergeInitiated(agent, candidate);
                }
                
                List<MergeResult> results = agent.bulkMerge(matchCandidates);
                
                // Log each merge result
                for (MergeResult result : results) {
                    long executionTime = System.currentTimeMillis() - startTime;
                    auditService.logMergeCompleted(agent, result, executionTime);
                }
                
                allResults.addAll(results);
                
                long totalExecutionTime = System.currentTimeMillis() - startTime;
                auditService.logBulkOperation(
                    agent,
                    "MERGING_COMPLETED",
                    matchCandidates.size(),
                    results.size(),
                    totalExecutionTime,
                    "Merged " + results.size() + " entities"
                );
                
                log.info("Agent {} completed {} merges in {}ms", agent.getAgentId(), results.size(), totalExecutionTime);
                
            } catch (Exception e) {
                log.error("Agent {} failed to merge entities: {}", agent.getAgentId(), e.getMessage(), e);
                auditService.logAgentActivity(
                    agent,
                    "MERGING_FAILED",
                    "ALL_CANDIDATES",
                    null,
                    "Merging failed: " + e.getMessage(),
                    "FAILED",
                    System.currentTimeMillis() - startTime,
                    Map.of("error", e.getMessage())
                );
            }
        }
        
        log.info("Total merge results from all agents: {}", allResults.size());
        return allResults;
    }
    
    /**
     * Complete workflow: find matches and merge entities
     */
    public Map<String, Object> executeCompleteWorkflow(List<DataEntity> entities) {
        long workflowStartTime = System.currentTimeMillis();
        
        log.info("Starting complete workflow with {} entities", entities.size());
        
        // Step 1: Find matches
        List<MatchCandidate> matches = findMatchesWithAgents(entities);
        
        // Step 2: Merge entities
        List<MergeResult> mergeResults = new ArrayList<>();
        if (!matches.isEmpty()) {
            mergeResults = mergeEntitiesWithAgents(matches);
        }
        
        long totalExecutionTime = System.currentTimeMillis() - workflowStartTime;
        
        // Calculate statistics
        long successfulMerges = mergeResults.stream()
            .filter(result -> "MERGED".equals(result.getStatus()))
            .count();
        
        long failedMerges = mergeResults.stream()
            .filter(result -> "FAILED".equals(result.getStatus()))
            .count();
        
        Map<String, Object> workflowResult = Map.of(
            "totalEntities", entities.size(),
            "matchesFound", matches.size(),
            "mergesInitiated", matches.size(),
            "successfulMerges", successfulMerges,
            "failedMerges", failedMerges,
            "totalExecutionTimeMs", totalExecutionTime,
            "matches", matches,
            "mergeResults", mergeResults
        );
        
        log.info("Complete workflow finished: {} entities, {} matches, {} successful merges in {}ms", 
            entities.size(), matches.size(), successfulMerges, totalExecutionTime);
        
        return workflowResult;
    }
    
    /**
     * Get agent status information
     */
    public Map<String, Object> getAgentStatus() {
        Map<String, Object> matchingAgentStatus = Map.of(
            "totalAgents", matchingAgents.size(),
            "enabledAgents", matchingAgents.stream().filter(MatchingAgent::isEnabled).count(),
            "agents", matchingAgents.stream().map(agent -> Map.of(
                "agentId", agent.getAgentId(),
                "agentType", agent.getAgentType(),
                "enabled", agent.isEnabled(),
                "confidenceThreshold", agent.getConfidenceThreshold()
            )).toList()
        );
        
        Map<String, Object> mergeAgentStatus = Map.of(
            "totalAgents", mergeAgents.size(),
            "enabledAgents", mergeAgents.stream().filter(MergeAgent::isEnabled).count(),
            "agents", mergeAgents.stream().map(agent -> Map.of(
                "agentId", agent.getAgentId(),
                "agentType", agent.getAgentType(),
                "enabled", agent.isEnabled(),
                "confidenceThreshold", agent.getConfidenceThreshold(),
                "mergeStrategy", agent.getMergeStrategy()
            )).toList()
        );
        
        return Map.of(
            "matchingAgents", matchingAgentStatus,
            "mergeAgents", mergeAgentStatus
        );
    }
    
    /**
     * Remove duplicate matches (same entity pairs)
     */
    private List<MatchCandidate> removeDuplicateMatches(List<MatchCandidate> matches) {
        List<MatchCandidate> uniqueMatches = new ArrayList<>();
        
        for (MatchCandidate match : matches) {
            boolean isDuplicate = false;
            
            for (MatchCandidate existing : uniqueMatches) {
                if (isSameEntityPair(match, existing)) {
                    // Keep the one with higher confidence
                    if (match.getConfidenceScore() > existing.getConfidenceScore()) {
                        uniqueMatches.remove(existing);
                        uniqueMatches.add(match);
                    }
                    isDuplicate = true;
                    break;
                }
            }
            
            if (!isDuplicate) {
                uniqueMatches.add(match);
            }
        }
        
        return uniqueMatches;
    }
    
    /**
     * Check if two matches involve the same entity pair
     */
    private boolean isSameEntityPair(MatchCandidate match1, MatchCandidate match2) {
        String entity1Id1 = match1.getEntity1().getEntityId();
        String entity2Id1 = match1.getEntity2().getEntityId();
        String entity1Id2 = match2.getEntity1().getEntityId();
        String entity2Id2 = match2.getEntity2().getEntityId();
        
        return (entity1Id1.equals(entity1Id2) && entity2Id1.equals(entity2Id2)) ||
               (entity1Id1.equals(entity2Id2) && entity2Id1.equals(entity1Id2));
    }
} 