package com.mdm.mcp.api;

import com.mdm.mcp.model.DataEntity;
import com.mdm.mcp.model.MatchCandidate;
import com.mdm.mcp.model.MergeResult;
import com.mdm.mcp.service.DroolsRuleEngineService;
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
@RequestMapping("/api/entity-merge")
@CrossOrigin(originPatterns = "*")
@Slf4j
public class EntityMergeController {
    
    @Autowired
    private DroolsRuleEngineService ruleEngineService;
    
    // Store sample entities for demo purposes
    private List<DataEntity> sampleEntities = new ArrayList<>();
    
    @PostMapping("/load-sample-data")
    public ResponseEntity<Map<String, Object>> loadSampleData() {
        try {
            log.info("Loading sample data for entity merge testing");
            
            // Create sample entities
            sampleEntities = new ArrayList<>();
            
            // Entity 1: John Smith from CRM
            DataEntity entity1 = DataEntity.builder()
                .entityId("CRM_001")
                .entityType("PERSON")
                .sourceSystem("CRM")
                .confidenceScore(0.95)
                .status("ACTIVE")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .attributes(new HashMap<>() {{
                    put("firstName", "John");
                    put("lastName", "Smith");
                    put("email", "john.smith@email.com");
                    put("phone", "+1-555-0123");
                    put("address", "123 Main St, New York, NY");
                }})
                .build();
            
            // Entity 2: John Smith from ERP (slight variation)
            DataEntity entity2 = DataEntity.builder()
                .entityId("ERP_001")
                .entityType("PERSON")
                .sourceSystem("ERP")
                .confidenceScore(0.90)
                .status("ACTIVE")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .attributes(new HashMap<>() {{
                    put("firstName", "John");
                    put("lastName", "Smith");
                    put("email", "john.smith@email.com");
                    put("phone", "+1-555-0123");
                    put("address", "123 Main Street, New York, NY");
                }})
                .build();
            
            // Entity 3: Jane Doe (different person)
            DataEntity entity3 = DataEntity.builder()
                .entityId("CRM_002")
                .entityType("PERSON")
                .sourceSystem("CRM")
                .confidenceScore(0.85)
                .status("ACTIVE")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .attributes(new HashMap<>() {{
                    put("firstName", "Jane");
                    put("lastName", "Doe");
                    put("email", "jane.doe@email.com");
                    put("phone", "+1-555-0456");
                    put("address", "456 Oak Ave, Los Angeles, CA");
                }})
                .build();
            
            sampleEntities.add(entity1);
            sampleEntities.add(entity2);
            sampleEntities.add(entity3);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Sample data loaded successfully");
            response.put("entities", sampleEntities);
            response.put("count", sampleEntities.size());
            
            log.info("Sample data loaded: {} entities", sampleEntities.size());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error loading sample data", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to load sample data");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    @GetMapping("/find-match-candidates")
    public ResponseEntity<Map<String, Object>> findMatchCandidates() {
        try {
            if (sampleEntities.isEmpty()) {
                // Load sample data if not already loaded
                loadSampleData();
            }
            
            log.info("Finding match candidates for {} entities", sampleEntities.size());
            
            List<MatchCandidate> candidates = ruleEngineService.findMatchCandidates(sampleEntities);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Match candidates found");
            response.put("candidates", candidates);
            response.put("count", candidates.size());
            
            log.info("Found {} match candidates", candidates.size());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error finding match candidates", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to find match candidates");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    @PostMapping("/find-matches")
    public ResponseEntity<Map<String, Object>> findMatchCandidatesWithBody(@RequestBody List<DataEntity> entities) {
        try {
            log.info("Finding match candidates for {} entities", entities.size());
            
            List<MatchCandidate> candidates = ruleEngineService.findMatchCandidates(entities);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Match candidates found");
            response.put("candidates", candidates);
            response.put("count", candidates.size());
            
            log.info("Found {} match candidates", candidates.size());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error finding match candidates", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to find match candidates");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    @PostMapping("/bulk-merge")
    public ResponseEntity<Map<String, Object>> bulkMerge() {
        try {
            if (sampleEntities.isEmpty()) {
                // Load sample data if not already loaded
                loadSampleData();
            }
            
            log.info("Performing bulk merge for {} entities", sampleEntities.size());
            
            List<MergeResult> results = ruleEngineService.executeEntityMerging(sampleEntities);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Bulk merge completed");
            response.put("results", results);
            response.put("count", results.size());
            
            log.info("Bulk merge completed: {} results", results.size());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error during bulk merge", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to perform bulk merge");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    @PostMapping("/bulk-merge-with-body")
    public ResponseEntity<Map<String, Object>> bulkMergeWithBody(@RequestBody List<DataEntity> entities) {
        try {
            log.info("Performing bulk merge for {} entities", entities.size());
            
            List<MergeResult> results = ruleEngineService.executeEntityMerging(entities);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Bulk merge completed");
            response.put("results", results);
            response.put("count", results.size());
            
            log.info("Bulk merge completed: {} results", results.size());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error during bulk merge", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to perform bulk merge");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    @PostMapping("/merge-entities")
    public ResponseEntity<Map<String, Object>> mergeEntitiesByQuery(
            @RequestParam(name = "entityId1") String entityId1, 
            @RequestParam(name = "entityId2") String entityId2) {
        try {
            log.info("Merging entities: {} and {}", entityId1, entityId2);
            
            // For demo purposes, create entities from IDs
            // In a real implementation, you would fetch these from the database
            DataEntity entity1 = DataEntity.builder()
                .entityId(entityId1)
                .entityType("PERSON")
                .sourceSystem("CRM")
                .confidenceScore(0.95)
                .status("ACTIVE")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .attributes(new HashMap<>() {{
                    put("firstName", "John");
                    put("lastName", "Smith");
                    put("email", "john.smith@email.com");
                }})
                .build();
            
            DataEntity entity2 = DataEntity.builder()
                .entityId(entityId2)
                .entityType("PERSON")
                .sourceSystem("ERP")
                .confidenceScore(0.90)
                .status("ACTIVE")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .attributes(new HashMap<>() {{
                    put("firstName", "John");
                    put("lastName", "Smith");
                    put("email", "john.smith@email.com");
                }})
                .build();
            
            MergeResult result = ruleEngineService.mergeEntities(entity1, entity2);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Entity merge completed");
            response.put("result", result);
            
            log.info("Entity merge completed: {}", result.getStatus());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error during entity merge", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to merge entities");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    @PostMapping("/merge")
    public ResponseEntity<Map<String, Object>> mergeEntities(@RequestBody Map<String, String> request) {
        try {
            String entity1Id = request.get("entity1Id");
            String entity2Id = request.get("entity2Id");
            
            log.info("Merging entities: {} and {}", entity1Id, entity2Id);
            
            // For demo purposes, create entities from IDs
            // In a real implementation, you would fetch these from the database
            DataEntity entity1 = DataEntity.builder()
                .entityId(entity1Id)
                .entityType("PERSON")
                .sourceSystem("CRM")
                .confidenceScore(0.95)
                .status("ACTIVE")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .attributes(new HashMap<>() {{
                    put("firstName", "John");
                    put("lastName", "Smith");
                    put("email", "john.smith@email.com");
                }})
                .build();
            
            DataEntity entity2 = DataEntity.builder()
                .entityId(entity2Id)
                .entityType("PERSON")
                .sourceSystem("ERP")
                .confidenceScore(0.90)
                .status("ACTIVE")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .attributes(new HashMap<>() {{
                    put("firstName", "John");
                    put("lastName", "Smith");
                    put("email", "john.smith@email.com");
                }})
                .build();
            
            MergeResult result = ruleEngineService.mergeEntities(entity1, entity2);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Entity merge completed");
            response.put("result", result);
            
            log.info("Entity merge completed: {}", result.getStatus());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error during entity merge", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to merge entities");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "Entity Merge Service");
        response.put("timestamp", LocalDateTime.now());
        return ResponseEntity.ok(response);
    }
} 