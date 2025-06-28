package com.mdm.mcp.agent;

import com.mdm.mcp.model.DataEntity;
import com.mdm.mcp.model.MatchCandidate;

import java.util.List;

public interface MatchingAgent extends Agent {
    
    /**
     * Find potential matches for a given entity
     */
    List<MatchCandidate> findMatches(DataEntity entity, List<DataEntity> candidateEntities);
    
    /**
     * Find all potential matches among a list of entities
     */
    List<MatchCandidate> findAllMatches(List<DataEntity> entities);
    
    /**
     * Calculate confidence score for a potential match
     */
    double calculateConfidence(DataEntity entity1, DataEntity entity2);
    
    /**
     * Get the matching criteria used by this agent
     */
    String getMatchingCriteria();
} 