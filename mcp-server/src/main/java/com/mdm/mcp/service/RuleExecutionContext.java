package com.mdm.mcp.service;

import com.mdm.mcp.model.DataEntity;
import com.mdm.mcp.model.MatchCandidate;
import com.mdm.mcp.model.MergeResult;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class RuleExecutionContext {
    
    private List<MatchCandidate> matchCandidates = new ArrayList<>();
    private List<MergeResult> mergeResults = new ArrayList<>();
    private List<DataEntity> mergedEntities = new ArrayList<>();
    private Map<String, String> survivorshipDecisions = new HashMap<>();
    private Map<String, Object> executionDetails = new HashMap<>();
    
    public void addMatchCandidate(DataEntity entity1, DataEntity entity2, Double confidenceScore, String matchReason) {
        MatchCandidate candidate = new MatchCandidate(entity1, entity2, confidenceScore, matchReason);
        matchCandidates.add(candidate);
    }
    
    public void addMergeResult(MergeResult result) {
        mergeResults.add(result);
    }
    
    public void addMergedEntity(DataEntity entity) {
        mergedEntities.add(entity);
    }
    
    public void addSurvivorshipDecision(String attribute, String sourceEntityId, String rule) {
        survivorshipDecisions.put(attribute, sourceEntityId + ":" + rule);
    }
    
    public void addExecutionDetail(String key, Object value) {
        executionDetails.put(key, value);
    }
    
    public List<MatchCandidate> getMatchCandidates() {
        return matchCandidates;
    }
    
    public List<MergeResult> getMergeResults() {
        return mergeResults;
    }
    
    public List<DataEntity> getMergedEntities() {
        return mergedEntities;
    }
    
    public Map<String, String> getSurvivorshipDecisions() {
        return survivorshipDecisions;
    }
    
    public Map<String, Object> getExecutionDetails() {
        return executionDetails;
    }
    
    public void clear() {
        matchCandidates.clear();
        mergeResults.clear();
        mergedEntities.clear();
        survivorshipDecisions.clear();
        executionDetails.clear();
    }
} 