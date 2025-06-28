package com.mdm.agent.core;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class RestMCPClient implements MCPClient {
    
    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final HttpHeaders headers;
    
    public RestMCPClient(String baseUrl) {
        this.restTemplate = new RestTemplate();
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
        this.headers = new HttpHeaders();
        this.headers.setContentType(MediaType.APPLICATION_JSON);
    }
    
    @Override
    public List<Map<String, Object>> findMatchCandidates(List<Map<String, Object>> entities) {
        try {
            Map<String, Object> request = new HashMap<>();
            request.put("entities", entities);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl + "api/rules/match", entity, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> matches = (List<Map<String, Object>>) response.getBody().get("matches");
                log.debug("Found {} match candidates via MCP server", matches != null ? matches.size() : 0);
                return matches != null ? matches : List.of();
            }
        } catch (Exception e) {
            log.error("Error calling MCP server for match candidates: {}", e.getMessage(), e);
        }
        return List.of();
    }
    
    @Override
    public double calculateConfidence(Map<String, Object> entity1, Map<String, Object> entity2) {
        try {
            Map<String, Object> request = new HashMap<>();
            request.put("entity1", entity1);
            request.put("entity2", entity2);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl + "api/rules/confidence", entity, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Object confidence = response.getBody().get("confidence");
                if (confidence instanceof Number) {
                    return ((Number) confidence).doubleValue();
                }
            }
        } catch (Exception e) {
            log.error("Error calling MCP server for confidence calculation: {}", e.getMessage(), e);
        }
        return 0.0;
    }
    
    @Override
    public Map<String, Object> mergeEntities(Map<String, Object> entity1, Map<String, Object> entity2) {
        try {
            Map<String, Object> request = new HashMap<>();
            request.put("entity1", entity1);
            request.put("entity2", entity2);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl + "api/rules/merge", entity, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> mergedEntity = (Map<String, Object>) response.getBody().get("mergedEntity");
                log.debug("Merged entities via MCP server");
                return mergedEntity != null ? mergedEntity : new HashMap<>();
            }
        } catch (Exception e) {
            log.error("Error calling MCP server for entity merge: {}", e.getMessage(), e);
        }
        return new HashMap<>();
    }
    
    @Override
    public Map<String, Object> applySurvivorshipRules(Map<String, Object> entity1, Map<String, Object> entity2) {
        try {
            Map<String, Object> request = new HashMap<>();
            request.put("entity1", entity1);
            request.put("entity2", entity2);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl + "api/rules/survivorship", entity, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> attributes = (Map<String, Object>) response.getBody().get("attributes");
                log.debug("Applied survivorship rules via MCP server");
                return attributes != null ? attributes : new HashMap<>();
            }
        } catch (Exception e) {
            log.error("Error calling MCP server for survivorship rules: {}", e.getMessage(), e);
        }
        return new HashMap<>();
    }
    
    @Override
    public Map<String, Object> getRules(String ruleType) {
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(
                baseUrl + "api/rules/" + ruleType, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                log.debug("Retrieved rules of type: {} from MCP server", ruleType);
                return response.getBody();
            }
        } catch (Exception e) {
            log.error("Error calling MCP server for rules: {}", e.getMessage(), e);
        }
        return new HashMap<>();
    }
    
    @Override
    public Map<String, Object> executeRule(String ruleName, Map<String, Object> facts) {
        try {
            Map<String, Object> request = new HashMap<>();
            request.put("ruleName", ruleName);
            request.put("facts", facts);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl + "api/rules/execute", entity, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                log.debug("Executed rule: {} via MCP server", ruleName);
                return response.getBody();
            }
        } catch (Exception e) {
            log.error("Error calling MCP server for rule execution: {}", e.getMessage(), e);
        }
        return new HashMap<>();
    }
    
    @Override
    public boolean isHealthy() {
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(
                baseUrl + "api/health", Map.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.warn("MCP server health check failed: {}", e.getMessage());
            return false;
        }
    }
} 