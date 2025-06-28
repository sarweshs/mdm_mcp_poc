package com.mdm.mcp.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "entities")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DataEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "entity_id", unique = true, nullable = false)
    private String entityId;
    
    @Column(name = "entity_type", nullable = false)
    private String entityType;
    
    @Column(name = "source_system")
    private String sourceSystem;
    
    @Column(name = "confidence_score")
    private Double confidenceScore;
    
    @Column(name = "status")
    private String status; // ACTIVE, INACTIVE, MERGED, DUPLICATE
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @ElementCollection
    @CollectionTable(name = "entity_attributes", 
        joinColumns = @JoinColumn(name = "entity_id"))
    @MapKeyColumn(name = "attribute_name")
    @Column(name = "attribute_value")
    private Map<String, String> attributes = new HashMap<>();
    
    @ElementCollection
    @CollectionTable(name = "entity_relationships", 
        joinColumns = @JoinColumn(name = "entity_id"))
    @MapKeyColumn(name = "relationship_type")
    @Column(name = "related_entity_id")
    private Map<String, String> relationships = new HashMap<>();
    
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