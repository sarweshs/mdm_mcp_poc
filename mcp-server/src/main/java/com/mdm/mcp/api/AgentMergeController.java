package com.mdm.mcp.api;

import com.mdm.mcp.model.AuditLog;
import com.mdm.mcp.model.DataEntity;
import com.mdm.mcp.model.MatchCandidate;
import com.mdm.mcp.model.MergeResult;
import com.mdm.mcp.service.AgentOrchestrationService;
import com.mdm.mcp.service.AuditService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/agent-merge")
@CrossOrigin(originPatterns = "*")
@Slf4j
public class AgentMergeController {
    
    @Autowired
    private AgentOrchestrationService agentOrchestrationService;
    
    @Autowired
    private AuditService auditService;
    
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now());
        response.put("service", "Agent Merge Service");
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/agent-status")
    public ResponseEntity<Map<String, Object>> getAgentStatus() {
        try {
            Map<String, Object> status = agentOrchestrationService.getAgentStatus();
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            log.error("Failed to get agent status: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to get agent status: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    @PostMapping("/load-sample-data")
    public ResponseEntity<Map<String, Object>> loadSampleData() {
        try {
            log.info("Loading sample data for agent-based entity merge testing");
            
            List<DataEntity> sampleEntities = createSampleEntities();
            
            // Log entity creation
            for (DataEntity entity : sampleEntities) {
                auditService.logEntityCreated(entity, "SAMPLE_DATA_LOAD");
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Sample data loaded successfully");
            response.put("entitiesLoaded", sampleEntities.size());
            response.put("entities", sampleEntities);
            response.put("timestamp", LocalDateTime.now());
            
            log.info("Sample data loaded: {} entities", sampleEntities.size());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Failed to load sample data: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to load sample data: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    @GetMapping("/find-matches")
    public ResponseEntity<Map<String, Object>> findMatches() {
        try {
            log.info("Finding matches using intelligent agents");
            
            List<DataEntity> entities = createSampleEntities();
            List<MatchCandidate> matches = agentOrchestrationService.findMatchesWithAgents(entities);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Matches found successfully");
            response.put("totalEntities", entities.size());
            response.put("matchesFound", matches.size());
            response.put("matches", matches);
            response.put("timestamp", LocalDateTime.now());
            
            log.info("Found {} matches among {} entities", matches.size(), entities.size());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Failed to find matches: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to find matches: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    @PostMapping("/bulk-merge")
    public ResponseEntity<Map<String, Object>> bulkMerge() {
        try {
            log.info("Performing bulk merge using intelligent agents");
            
            List<DataEntity> entities = createSampleEntities();
            List<MatchCandidate> matches = agentOrchestrationService.findMatchesWithAgents(entities);
            List<MergeResult> mergeResults = agentOrchestrationService.mergeEntitiesWithAgents(matches);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Bulk merge completed");
            response.put("totalEntities", entities.size());
            response.put("matchesFound", matches.size());
            response.put("mergesCompleted", mergeResults.size());
            response.put("successfulMerges", mergeResults.stream().filter(r -> "MERGED".equals(r.getStatus())).count());
            response.put("failedMerges", mergeResults.stream().filter(r -> "FAILED".equals(r.getStatus())).count());
            response.put("mergeResults", mergeResults);
            response.put("timestamp", LocalDateTime.now());
            
            log.info("Bulk merge completed: {} merges from {} matches", mergeResults.size(), matches.size());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Failed to perform bulk merge: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to perform bulk merge: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    @PostMapping("/complete-workflow")
    public ResponseEntity<Map<String, Object>> executeCompleteWorkflow() {
        try {
            log.info("Executing complete agent-based workflow");
            
            List<DataEntity> entities = createSampleEntities();
            Map<String, Object> workflowResult = agentOrchestrationService.executeCompleteWorkflow(entities);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Complete workflow executed successfully");
            response.putAll(workflowResult);
            response.put("timestamp", LocalDateTime.now());
            
            log.info("Complete workflow executed: {} entities, {} matches, {} successful merges", 
                workflowResult.get("totalEntities"), workflowResult.get("matchesFound"), workflowResult.get("successfulMerges"));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Failed to execute complete workflow: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to execute complete workflow: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    @GetMapping("/audit-logs")
    public ResponseEntity<Map<String, Object>> getAuditLogs(
            @RequestParam(defaultValue = "50") int limit) {
        try {
            List<AuditLog> auditLogs = auditService.getRecentAuditLogs(limit);
            Map<String, Object> statistics = auditService.getAuditStatistics();
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Audit logs retrieved successfully");
            response.put("auditLogs", auditLogs);
            response.put("statistics", statistics);
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Failed to get audit logs: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to get audit logs: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    @GetMapping("/audit-logs/entity/{entityId}")
    public ResponseEntity<Map<String, Object>> getAuditLogsForEntity(@PathVariable String entityId) {
        try {
            List<AuditLog> auditLogs = auditService.getAuditLogsForEntity(entityId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Entity audit logs retrieved successfully");
            response.put("entityId", entityId);
            response.put("auditLogs", auditLogs);
            response.put("totalLogs", auditLogs.size());
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Failed to get audit logs for entity {}: {}", entityId, e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to get audit logs for entity: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    @GetMapping("/audit-logs/agent/{agentId}")
    public ResponseEntity<Map<String, Object>> getAuditLogsForAgent(@PathVariable String agentId) {
        try {
            List<AuditLog> auditLogs = auditService.getAuditLogsForAgent(agentId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Agent audit logs retrieved successfully");
            response.put("agentId", agentId);
            response.put("auditLogs", auditLogs);
            response.put("totalLogs", auditLogs.size());
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Failed to get audit logs for agent {}: {}", agentId, e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to get audit logs for agent: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    private List<DataEntity> createSampleEntities() {
        List<DataEntity> entities = new ArrayList<>();
        
        // Entity 1 - CRM System
        Map<String, String> attributes1 = new HashMap<>();
        attributes1.put("firstName", "John");
        attributes1.put("lastName", "Doe");
        attributes1.put("email", "john.doe@email.com");
        attributes1.put("phone", "555-123-4567");
        attributes1.put("address", "123 Main St, Anytown, USA");
        
        DataEntity entity1 = DataEntity.builder()
            .entityId("CRM_001")
            .entityType("PERSON")
            .sourceSystem("CRM")
            .confidenceScore(0.95)
            .status("ACTIVE")
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .attributes(attributes1)
            .relationships(null)
            .build();
        
        // Entity 2 - ERP System (similar to CRM_001)
        Map<String, String> attributes2 = new HashMap<>();
        attributes2.put("firstName", "John");
        attributes2.put("lastName", "Doe");
        attributes2.put("email", "john.doe@email.com");
        attributes2.put("phone", "555-123-4567");
        attributes2.put("address", "123 Main Street, Anytown, USA");
        attributes2.put("department", "Engineering");
        
        DataEntity entity2 = DataEntity.builder()
            .entityId("ERP_001")
            .entityType("PERSON")
            .sourceSystem("ERP")
            .confidenceScore(0.90)
            .status("ACTIVE")
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .attributes(attributes2)
            .relationships(null)
            .build();
        
        // Entity 3 - Different person
        Map<String, String> attributes3 = new HashMap<>();
        attributes3.put("firstName", "Jane");
        attributes3.put("lastName", "Smith");
        attributes3.put("email", "jane.smith@email.com");
        attributes3.put("phone", "555-987-6543");
        attributes3.put("address", "456 Oak Ave, Somewhere, USA");
        
        DataEntity entity3 = DataEntity.builder()
            .entityId("CRM_002")
            .entityType("PERSON")
            .sourceSystem("CRM")
            .confidenceScore(0.88)
            .status("ACTIVE")
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .attributes(attributes3)
            .relationships(null)
            .build();
        
        entities.add(entity1);
        entities.add(entity2);
        entities.add(entity3);
        
        return entities;
    }
} 