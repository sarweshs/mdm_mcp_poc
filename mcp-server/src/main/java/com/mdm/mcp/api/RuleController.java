package com.mdm.mcp.api;

import com.mdm.mcp.service.DroolsRuleEngineService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rules")
@Slf4j
public class RuleController {

    @Autowired
    private DroolsRuleEngineService droolsRuleEngineService;

    /**
     * Find match candidates for entities using Drools rules
     */
    @PostMapping("/match")
    public ResponseEntity<Map<String, Object>> findMatchCandidates(@RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> entities = (List<Map<String, Object>>) request.get("entities");
            
            log.info("Finding match candidates for {} entities via MCP server", entities.size());
            List<Map<String, Object>> matches = droolsRuleEngineService.findMatchCandidates(entities);
            
            Map<String, Object> response = new HashMap<>();
            response.put("matches", matches);
            response.put("totalEntities", entities.size());
            response.put("matchCount", matches.size());
            
            log.info("Found {} match candidates via MCP server", matches.size());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error finding match candidates: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Calculate confidence score between two entities using Drools rules
     */
    @PostMapping("/confidence")
    public ResponseEntity<Map<String, Object>> calculateConfidence(@RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> entity1 = (Map<String, Object>) request.get("entity1");
            @SuppressWarnings("unchecked")
            Map<String, Object> entity2 = (Map<String, Object>) request.get("entity2");
            
            log.info("Calculating confidence between entities: {} and {}", 
                entity1.get("entityId"), entity2.get("entityId"));
            
            double confidence = droolsRuleEngineService.calculateConfidence(entity1, entity2);
            
            Map<String, Object> response = new HashMap<>();
            response.put("confidence", confidence);
            response.put("entity1Id", entity1.get("entityId"));
            response.put("entity2Id", entity2.get("entityId"));
            
            log.info("Confidence calculated: {} between {} and {}", confidence, 
                entity1.get("entityId"), entity2.get("entityId"));
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error calculating confidence: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Merge two entities using Drools rules
     */
    @PostMapping("/merge")
    public ResponseEntity<Map<String, Object>> mergeEntities(@RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> entity1 = (Map<String, Object>) request.get("entity1");
            @SuppressWarnings("unchecked")
            Map<String, Object> entity2 = (Map<String, Object>) request.get("entity2");
            
            log.info("Merging entities: {} and {} via MCP server", 
                entity1.get("entityId"), entity2.get("entityId"));
            
            Map<String, Object> mergedEntity = droolsRuleEngineService.mergeEntities(entity1, entity2);
            
            Map<String, Object> response = new HashMap<>();
            response.put("mergedEntity", mergedEntity);
            response.put("sourceEntity1", entity1);
            response.put("sourceEntity2", entity2);
            response.put("status", "MERGED");
            
            log.info("Entities merged successfully via MCP server");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error merging entities: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Apply survivorship rules to determine which attributes to keep
     */
    @PostMapping("/survivorship")
    public ResponseEntity<Map<String, Object>> applySurvivorshipRules(@RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> entity1 = (Map<String, Object>) request.get("entity1");
            @SuppressWarnings("unchecked")
            Map<String, Object> entity2 = (Map<String, Object>) request.get("entity2");
            
            log.info("Applying survivorship rules for entities: {} and {}", 
                entity1.get("entityId"), entity2.get("entityId"));
            
            Map<String, Object> attributes = droolsRuleEngineService.applySurvivorshipRules(entity1, entity2);
            
            Map<String, Object> response = new HashMap<>();
            response.put("attributes", attributes);
            response.put("entity1Id", entity1.get("entityId"));
            response.put("entity2Id", entity2.get("entityId"));
            
            log.info("Survivorship rules applied successfully via MCP server");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error applying survivorship rules: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Get rules by type
     */
    @GetMapping("/{ruleType}")
    public ResponseEntity<Map<String, Object>> getRules(@PathVariable String ruleType) {
        try {
            log.info("Retrieving rules of type: {}", ruleType);
            
            Map<String, Object> rules = droolsRuleEngineService.getRules(ruleType);
            
            Map<String, Object> response = new HashMap<>();
            response.put("ruleType", ruleType);
            response.put("rules", rules);
            response.put("count", rules.size());
            
            log.info("Retrieved {} rules of type: {}", rules.size(), ruleType);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error retrieving rules: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Execute a specific rule
     */
    @PostMapping("/execute")
    public ResponseEntity<Map<String, Object>> executeRule(@RequestBody Map<String, Object> request) {
        try {
            String ruleName = (String) request.get("ruleName");
            @SuppressWarnings("unchecked")
            Map<String, Object> facts = (Map<String, Object>) request.get("facts");
            
            log.info("Executing rule: {} with {} facts", ruleName, facts.size());
            
            Map<String, Object> result = droolsRuleEngineService.executeRule(ruleName, facts);
            
            Map<String, Object> response = new HashMap<>();
            response.put("ruleName", ruleName);
            response.put("result", result);
            response.put("status", "EXECUTED");
            
            log.info("Rule {} executed successfully", ruleName);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error executing rule: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "MCP Rule Engine");
        response.put("droolsAvailable", droolsRuleEngineService.isDroolsAvailable());
        return ResponseEntity.ok(response);
    }
}