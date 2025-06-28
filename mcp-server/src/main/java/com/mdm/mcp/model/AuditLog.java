package com.mdm.mcp.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "audit_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "operation_type", nullable = false)
    private String operationType; // MATCH_FOUND, MERGE_INITIATED, MERGE_COMPLETED, AGENT_ACTIVITY
    
    @Column(name = "agent_id")
    private String agentId;
    
    @Column(name = "agent_type")
    private String agentType; // MATCHING_AGENT, MERGE_AGENT, SURVIVORSHIP_AGENT
    
    @Column(name = "entity_ids")
    private String entityIds; // Comma-separated list of entity IDs involved
    
    @Column(name = "confidence_score")
    private Double confidenceScore;
    
    @Column(name = "decision_reason", columnDefinition = "TEXT")
    private String decisionReason;
    
    @Column(name = "status")
    private String status; // SUCCESS, FAILED, PENDING, APPROVED, REJECTED
    
    @Column(name = "execution_time_ms")
    private Long executionTimeMs;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata; // JSON string for additional data
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
} 