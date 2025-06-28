package com.mdm.agent.core;

import com.mdm.shared.api.MCPClient;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Slf4j
public abstract class BaseBotAgent {
    
    protected final MCPClient mcpClient;
    protected final String agentId;
    protected final String agentType;
    protected final double confidenceThreshold;
    protected final boolean enabled;
    
    public BaseBotAgent(MCPClient mcpClient, String agentId, String agentType, double confidenceThreshold) {
        this.mcpClient = mcpClient;
        this.agentId = agentId;
        this.agentType = agentType;
        this.confidenceThreshold = confidenceThreshold;
        this.enabled = true;
    }
    
    /**
     * Execute the agent's main functionality
     */
    public abstract AgentResponse execute(AgentRequest request);
    
    /**
     * Get the unique identifier for this agent
     */
    public String getAgentId() {
        return agentId;
    }
    
    /**
     * Get the type of this agent
     */
    public String getAgentType() {
        return agentType;
    }
    
    /**
     * Get the confidence threshold for this agent's decisions
     */
    public double getConfidenceThreshold() {
        return confidenceThreshold;
    }
    
    /**
     * Check if the agent is enabled
     */
    public boolean isEnabled() {
        return enabled;
    }
    
    /**
     * Get agent metadata
     */
    public Map<String, Object> getMetadata() {
        return Map.of(
            "agentId", agentId,
            "agentType", agentType,
            "confidenceThreshold", confidenceThreshold,
            "enabled", enabled,
            "version", "1.0.0"
        );
    }
    
    /**
     * Validate if the agent can handle the given request
     */
    public abstract boolean canHandle(AgentRequest request);
    
    /**
     * Get the agent's capabilities
     */
    public abstract Map<String, Object> getCapabilities();
} 