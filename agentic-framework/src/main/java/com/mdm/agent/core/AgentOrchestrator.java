package com.mdm.agent.core;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class AgentOrchestrator {
    
    private final AgentRegistry agentRegistry;
    private final ExecutorService executorService;
    
    public AgentOrchestrator() {
        this.agentRegistry = new AgentRegistry();
        this.executorService = Executors.newFixedThreadPool(10);
    }
    
    public AgentOrchestrator(AgentRegistry agentRegistry) {
        this.agentRegistry = agentRegistry;
        this.executorService = Executors.newFixedThreadPool(10);
    }
    
    /**
     * Execute a single agent request
     */
    public AgentResponse executeRequest(AgentRequest request) {
        long startTime = System.currentTimeMillis();
        
        try {
            log.info("Executing request: {} for agent type: {}", request.getRequestId(), request.getAgentType());
            
            List<BaseBotAgent> agents = agentRegistry.findAgentsForRequest(request);
            
            if (agents.isEmpty()) {
                return AgentResponse.error(
                    request.getRequestId(),
                    "NO_AGENT",
                    request.getAgentType(),
                    "No agents found to handle request of type: " + request.getAgentType()
                );
            }
            
            // For now, use the first available agent
            BaseBotAgent agent = agents.get(0);
            AgentResponse response = agent.execute(request);
            
            long executionTime = System.currentTimeMillis() - startTime;
            response.setExecutionTimeMs(executionTime);
            
            log.info("Request {} completed by agent {} in {}ms", 
                request.getRequestId(), agent.getAgentId(), executionTime);
            
            return response;
            
        } catch (Exception e) {
            log.error("Error executing request {}: {}", request.getRequestId(), e.getMessage(), e);
            return AgentResponse.error(
                request.getRequestId(),
                "ORCHESTRATOR",
                request.getAgentType(),
                "Execution failed: " + e.getMessage()
            );
        }
    }
    
    /**
     * Execute multiple agent requests in parallel
     */
    public List<AgentResponse> executeRequests(List<AgentRequest> requests) {
        List<CompletableFuture<AgentResponse>> futures = requests.stream()
            .map(request -> CompletableFuture.supplyAsync(() -> executeRequest(request), executorService))
            .toList();
        
        return futures.stream()
            .map(CompletableFuture::join)
            .toList();
    }
    
    /**
     * Execute a workflow with multiple steps
     */
    public AgentResponse executeWorkflow(List<AgentRequest> workflowSteps) {
        long startTime = System.currentTimeMillis();
        String workflowId = UUID.randomUUID().toString();
        
        log.info("Starting workflow: {} with {} steps", workflowId, workflowSteps.size());
        
        List<AgentResponse> stepResults = new ArrayList<>();
        Map<String, Object> workflowContext = new HashMap<>();
        
        for (int i = 0; i < workflowSteps.size(); i++) {
            AgentRequest step = workflowSteps.get(i);
            
            // Add workflow context to step
            step.setContext(workflowContext);
            
            log.info("Executing workflow step {}/{}: {}", i + 1, workflowSteps.size(), step.getOperation());
            
            AgentResponse stepResponse = executeRequest(step);
            stepResults.add(stepResponse);
            
            // If step failed, return error
            if ("ERROR".equals(stepResponse.getStatus())) {
                long executionTime = System.currentTimeMillis() - startTime;
                return AgentResponse.error(
                    workflowId,
                    "WORKFLOW_ORCHESTRATOR",
                    "WORKFLOW",
                    "Workflow failed at step " + (i + 1) + ": " + stepResponse.getMessage()
                ).toBuilder()
                    .executionTimeMs(executionTime)
                    .build();
            }
            
            // Add step result to context for next steps
            if (stepResponse.getResult() != null) {
                workflowContext.put("step_" + (i + 1) + "_result", stepResponse.getResult());
            }
        }
        
        long executionTime = System.currentTimeMillis() - startTime;
        
        log.info("Workflow {} completed successfully in {}ms", workflowId, executionTime);
        
        return AgentResponse.success(
            workflowId,
            "WORKFLOW_ORCHESTRATOR",
            "WORKFLOW",
            Map.of(
                "stepResults", stepResults,
                "workflowContext", workflowContext,
                "totalSteps", workflowSteps.size(),
                "successfulSteps", stepResults.size()
            )
        ).toBuilder()
            .executionTimeMs(executionTime)
            .build();
    }
    
    /**
     * Get agent registry
     */
    public AgentRegistry getAgentRegistry() {
        return agentRegistry;
    }
    
    /**
     * Register an agent
     */
    public void registerAgent(BaseBotAgent agent) {
        agentRegistry.registerAgent(agent);
    }
    
    /**
     * Get agent statistics
     */
    public Map<String, Object> getStatistics() {
        return agentRegistry.getStatistics();
    }
    
    /**
     * Shutdown the orchestrator
     */
    public void shutdown() {
        executorService.shutdown();
        log.info("Agent orchestrator shutdown");
    }
} 