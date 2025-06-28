package com.mdm.mcp.agent;

import com.mdm.mcp.model.DataEntity;
import com.mdm.mcp.model.MatchCandidate;
import com.mdm.mcp.model.MergeResult;

import java.util.List;

public interface Agent {
    
    /**
     * Get the unique identifier for this agent
     */
    String getAgentId();
    
    /**
     * Get the type of this agent
     */
    String getAgentType();
    
    /**
     * Get the confidence threshold for this agent's decisions
     */
    double getConfidenceThreshold();
    
    /**
     * Check if the agent is enabled
     */
    boolean isEnabled();
} 