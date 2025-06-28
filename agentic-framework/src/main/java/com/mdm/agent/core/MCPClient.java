package com.mdm.agent.core;

import java.util.List;
import java.util.Map;

/**
 * MCPClient interface for agents to call the MCP server for rule-based operations.
 * This is the primary interface through which agents interact with the central rule engine.
 */
public interface MCPClient {
    
    /**
     * Find match candidates for a list of entities using rules from MCP server
     */
    List<Map<String, Object>> findMatchCandidates(List<Map<String, Object>> entities);
    
    /**
     * Calculate confidence score between two entities using rules from MCP server
     */
    double calculateConfidence(Map<String, Object> entity1, Map<String, Object> entity2);
    
    /**
     * Merge two entities using survivorship rules from MCP server
     */
    Map<String, Object> mergeEntities(Map<String, Object> entity1, Map<String, Object> entity2);
    
    /**
     * Apply survivorship rules to determine which attributes to keep when merging
     */
    Map<String, Object> applySurvivorshipRules(Map<String, Object> entity1, Map<String, Object> entity2);
    
    /**
     * Get the latest rules from the MCP server
     */
    Map<String, Object> getRules(String ruleType);
    
    /**
     * Execute a specific rule operation
     */
    Map<String, Object> executeRule(String ruleName, Map<String, Object> facts);
    
    /**
     * Health check for MCP server connectivity
     */
    boolean isHealthy();
} 