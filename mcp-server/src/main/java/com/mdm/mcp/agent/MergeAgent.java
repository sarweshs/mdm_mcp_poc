package com.mdm.mcp.agent;

import com.mdm.mcp.model.DataEntity;
import com.mdm.mcp.model.MatchCandidate;
import com.mdm.mcp.model.MergeResult;

import java.util.List;
import java.util.Map;

public interface MergeAgent extends Agent {
    
    /**
     * Merge two entities based on a match candidate
     */
    MergeResult mergeEntities(MatchCandidate matchCandidate);
    
    /**
     * Perform bulk merge of multiple match candidates
     */
    List<MergeResult> bulkMerge(List<MatchCandidate> matchCandidates);
    
    /**
     * Apply survivorship rules to determine which attributes to keep
     */
    Map<String, Object> applySurvivorshipRules(DataEntity entity1, DataEntity entity2);
    
    /**
     * Get the merge strategy used by this agent
     */
    String getMergeStrategy();
    
    /**
     * Validate if two entities can be merged
     */
    boolean canMerge(DataEntity entity1, DataEntity entity2);
} 