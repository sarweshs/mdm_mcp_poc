package com.mdm.mcp.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "merge_rules")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MergeRule {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "rule_name", unique = true, nullable = false)
    private String ruleName;
    
    @Column(name = "rule_type", nullable = false)
    private String ruleType; // MATCH, MERGE, SURVIVORSHIP, RELATIONSHIP
    
    @Column(name = "entity_type", nullable = false)
    private String entityType;
    
    @Column(name = "priority")
    private Integer priority;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "rule_condition", columnDefinition = "TEXT")
    private String ruleCondition; // Drools condition
    
    @Column(name = "rule_action", columnDefinition = "TEXT")
    private String ruleAction; // Drools action
    
    @Column(name = "match_criteria", columnDefinition = "TEXT")
    private String matchCriteria; // JSON string for match criteria
    
    @Column(name = "survivorship_rules", columnDefinition = "TEXT")
    private String survivorshipRules; // JSON string for survivorship rules
    
    @Column(name = "company_id", nullable = true)
    private String companyId; // null = global rule, not null = company-specific rule
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
} 