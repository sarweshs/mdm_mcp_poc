package com.mdm.agent.core;

import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class AgentRegistry {
    
    private final Map<String, BaseBotAgent> agents = new ConcurrentHashMap<>();
    private final Map<String, List<BaseBotAgent>> agentsByType = new ConcurrentHashMap<>();
    
    /**
     * Register an agent
     */
    public void registerAgent(BaseBotAgent agent) {
        agents.put(agent.getAgentId(), agent);
        agentsByType.computeIfAbsent(agent.getAgentType(), k -> new ArrayList<>()).add(agent);
        log.info("Registered agent: {} of type: {}", agent.getAgentId(), agent.getAgentType());
    }
    
    /**
     * Unregister an agent
     */
    public void unregisterAgent(String agentId) {
        BaseBotAgent agent = agents.remove(agentId);
        if (agent != null) {
            agentsByType.get(agent.getAgentType()).remove(agent);
            log.info("Unregistered agent: {}", agentId);
        }
    }
    
    /**
     * Get agent by ID
     */
    public Optional<BaseBotAgent> getAgent(String agentId) {
        return Optional.ofNullable(agents.get(agentId));
    }
    
    /**
     * Get all agents of a specific type
     */
    public List<BaseBotAgent> getAgentsByType(String agentType) {
        return agentsByType.getOrDefault(agentType, new ArrayList<>());
    }
    
    /**
     * Get all registered agents
     */
    public List<BaseBotAgent> getAllAgents() {
        return new ArrayList<>(agents.values());
    }
    
    /**
     * Get all enabled agents
     */
    public List<BaseBotAgent> getEnabledAgents() {
        return agents.values().stream()
            .filter(BaseBotAgent::isEnabled)
            .toList();
    }
    
    /**
     * Get all enabled agents of a specific type
     */
    public List<BaseBotAgent> getEnabledAgentsByType(String agentType) {
        return getAgentsByType(agentType).stream()
            .filter(BaseBotAgent::isEnabled)
            .toList();
    }
    
    /**
     * Find agents that can handle a specific request
     */
    public List<BaseBotAgent> findAgentsForRequest(AgentRequest request) {
        return getEnabledAgentsByType(request.getAgentType()).stream()
            .filter(agent -> agent.canHandle(request))
            .toList();
    }
    
    /**
     * Get agent statistics
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalAgents", agents.size());
        stats.put("enabledAgents", getEnabledAgents().size());
        stats.put("agentTypes", new ArrayList<>(agentsByType.keySet()));
        
        Map<String, Integer> agentsByTypeCount = new HashMap<>();
        agentsByType.forEach((type, agentList) -> 
            agentsByTypeCount.put(type, agentList.size()));
        stats.put("agentsByType", agentsByTypeCount);
        
        return stats;
    }
    
    /**
     * Clear all agents
     */
    public void clear() {
        agents.clear();
        agentsByType.clear();
        log.info("Cleared all agents from registry");
    }
} 