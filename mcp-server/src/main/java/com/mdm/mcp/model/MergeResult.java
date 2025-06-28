package com.mdm.mcp.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MergeResult {
    
    private String mergeId;
    private String status; // SUCCESS, FAILED, PARTIAL
    private String message;
    private LocalDateTime timestamp;
    
    // Input entities
    private List<String> sourceEntityIds;
    
    // Output entities
    private String mergedEntityId;
    private List<String> duplicateEntityIds;
    
    // Rule execution details
    private String appliedRuleName;
    private Double confidenceScore;
    private Map<String, Object> ruleExecutionDetails;
    
    // Survivorship results
    private Map<String, String> survivorshipDecisions;
    
    // Relationships created/modified
    private List<String> relationshipChanges;
} 