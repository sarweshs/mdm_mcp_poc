package com.mdm.mcp.api;

import com.mdm.agent.core.AgentOrchestrator;
import com.mdm.agent.core.AgentRequest;
import com.mdm.agent.core.AgentResponse;
import com.mdm.agent.mdm.impl.IntelligentMatchingAgent;
import com.mdm.agent.mdm.impl.IntelligentMergeAgent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/agents")
@Slf4j
public class AgentController {

    @Autowired
    private AgentOrchestrator agentOrchestrator;

    @Autowired
    private IntelligentMatchingAgent matchingAgent;

    @Autowired
    private IntelligentMergeAgent mergeAgent;

    /**
     * Health check for agents
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "MCP Agent Orchestrator");
        response.put("matchingAgent", matchingAgent.isEnabled());
        response.put("mergeAgent", mergeAgent.isEnabled());
        return ResponseEntity.ok(response);
    }

    /**
     * Get agent status
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getAgentStatus() {
        Map<String, Object> response = new HashMap<>();
        response.put("matchingAgent", Map.of(
            "id", matchingAgent.getAgentId(),
            "type", matchingAgent.getAgentType(),
            "enabled", matchingAgent.isEnabled(),
            "confidenceThreshold", matchingAgent.getConfidenceThreshold()
        ));
        response.put("mergeAgent", Map.of(
            "id", mergeAgent.getAgentId(),
            "type", mergeAgent.getAgentType(),
            "enabled", mergeAgent.isEnabled(),
            "confidenceThreshold", mergeAgent.getConfidenceThreshold()
        ));
        return ResponseEntity.ok(response);
    }

    /**
     * Execute matching agent
     */
    @PostMapping("/match")
    public ResponseEntity<Map<String, Object>> executeMatching(@RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> entities = (List<Map<String, Object>>) request.get("entities");

            if (entities == null || entities.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "No entities provided for matching");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            AgentRequest agentRequest = AgentRequest.create(
                "MATCHING_AGENT",
                "FIND_MATCHES",
                Map.of("entities", entities)
            );

            AgentResponse agentResponse = matchingAgent.execute(agentRequest);

            Map<String, Object> response = new HashMap<>();
            response.put("requestId", agentResponse.getRequestId());
            response.put("agentId", agentResponse.getAgentId());
            response.put("status", agentResponse.getStatus());
            response.put("message", agentResponse.getMessage());
            response.put("result", agentResponse.getResult());
            response.put("confidenceScore", agentResponse.getConfidenceScore());
            response.put("executionTimeMs", agentResponse.getExecutionTimeMs());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error executing matching agent: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Execute merge agent
     */
    @PostMapping("/merge")
    public ResponseEntity<Map<String, Object>> executeMerge(@RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> entity1 = (Map<String, Object>) request.get("entity1");
            @SuppressWarnings("unchecked")
            Map<String, Object> entity2 = (Map<String, Object>) request.get("entity2");

            if (entity1 == null || entity2 == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Both entities are required for merging");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            AgentRequest agentRequest = AgentRequest.create(
                "MERGE_AGENT",
                "MERGE_ENTITIES",
                Map.of("entity1", entity1, "entity2", entity2)
            );

            AgentResponse agentResponse = mergeAgent.execute(agentRequest);

            Map<String, Object> response = new HashMap<>();
            response.put("requestId", agentResponse.getRequestId());
            response.put("agentId", agentResponse.getAgentId());
            response.put("status", agentResponse.getStatus());
            response.put("message", agentResponse.getMessage());
            response.put("result", agentResponse.getResult());
            response.put("confidenceScore", agentResponse.getConfidenceScore());
            response.put("executionTimeMs", agentResponse.getExecutionTimeMs());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error executing merge agent: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Execute bulk merge agent
     */
    @PostMapping("/bulk-merge")
    public ResponseEntity<Map<String, Object>> executeBulkMerge(@RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> entities = (List<Map<String, Object>>) request.get("entities");

            if (entities == null || entities.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "No entities provided for bulk merge");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            AgentRequest agentRequest = AgentRequest.create(
                "MERGE_AGENT",
                "BULK_MERGE",
                Map.of("entities", entities)
            );

            AgentResponse agentResponse = mergeAgent.execute(agentRequest);

            Map<String, Object> response = new HashMap<>();
            response.put("requestId", agentResponse.getRequestId());
            response.put("agentId", agentResponse.getAgentId());
            response.put("status", agentResponse.getStatus());
            response.put("message", agentResponse.getMessage());
            response.put("result", agentResponse.getResult());
            response.put("confidenceScore", agentResponse.getConfidenceScore());
            response.put("executionTimeMs", agentResponse.getExecutionTimeMs());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error executing bulk merge agent: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Execute complete workflow (match + merge)
     */
    @PostMapping("/workflow")
    public ResponseEntity<Map<String, Object>> executeWorkflow(@RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> entities = (List<Map<String, Object>>) request.get("entities");

            if (entities == null || entities.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "No entities provided for workflow");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            log.info("Executing complete workflow for {} entities", entities.size());

            // Step 1: Find matches
            AgentRequest matchRequest = AgentRequest.create(
                "MATCHING_AGENT",
                "FIND_MATCHES",
                Map.of("entities", entities)
            );
            AgentResponse matchResponse = matchingAgent.execute(matchRequest);

            // Step 2: Merge matched entities
            Map<String, Object> matchResult = (Map<String, Object>) matchResponse.getResult();
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> matches = (List<Map<String, Object>>) matchResult.get("matches");

            List<Map<String, Object>> mergedEntities = new ArrayList<>();
            for (Map<String, Object> match : matches) {
                Map<String, Object> entity1 = (Map<String, Object>) match.get("entity1");
                Map<String, Object> entity2 = (Map<String, Object>) match.get("entity2");

                AgentRequest mergeRequest = AgentRequest.create(
                    "MERGE_AGENT",
                    "MERGE_ENTITIES",
                    Map.of("entity1", entity1, "entity2", entity2)
                );
                AgentResponse mergeResponse = mergeAgent.execute(mergeRequest);
                Map<String, Object> mergeResult = (Map<String, Object>) mergeResponse.getResult();
                mergedEntities.add((Map<String, Object>) mergeResult.get("mergedEntity"));
            }

            Map<String, Object> response = new HashMap<>();
            response.put("workflowId", UUID.randomUUID().toString());
            response.put("totalEntities", entities.size());
            response.put("matchesFound", matches.size());
            response.put("entitiesMerged", mergedEntities.size());
            response.put("mergedEntities", mergedEntities);
            response.put("status", "COMPLETED");

            log.info("Workflow completed: {} entities -> {} matches -> {} merged entities", 
                entities.size(), matches.size(), mergedEntities.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error executing workflow: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
} 