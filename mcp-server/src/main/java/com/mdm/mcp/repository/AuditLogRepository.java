package com.mdm.mcp.repository;

import com.mdm.mcp.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    
    /**
     * Find audit logs by agent ID, ordered by creation date descending
     */
    List<AuditLog> findByAgentIdOrderByCreatedAtDesc(String agentId);
    
    /**
     * Find audit logs by operation type, ordered by creation date descending
     */
    List<AuditLog> findByOperationTypeOrderByCreatedAtDesc(String operationType);
    
    /**
     * Find audit logs by status, ordered by creation date descending
     */
    List<AuditLog> findByStatusOrderByCreatedAtDesc(String status);
    
    /**
     * Find audit logs containing specific entity IDs, ordered by creation date descending
     */
    @Query("SELECT a FROM AuditLog a WHERE a.entityIds LIKE %:entityId% ORDER BY a.createdAt DESC")
    List<AuditLog> findByEntityIdsContainingOrderByCreatedAtDesc(@Param("entityId") String entityId);
    
    /**
     * Find recent audit logs with limit
     */
    @Query("SELECT a FROM AuditLog a ORDER BY a.createdAt DESC")
    List<AuditLog> findTopNByOrderByCreatedAtDesc(int limit);
    
    /**
     * Count audit logs by operation type
     */
    long countByOperationType(String operationType);
    
    /**
     * Count audit logs by status
     */
    long countByStatus(String status);
    
    /**
     * Count audit logs by agent ID
     */
    long countByAgentId(String agentId);
    
    /**
     * Find audit logs by date range
     */
    @Query("SELECT a FROM AuditLog a WHERE a.createdAt BETWEEN :startDate AND :endDate ORDER BY a.createdAt DESC")
    List<AuditLog> findByCreatedAtBetweenOrderByCreatedAtDesc(
        @Param("startDate") java.time.LocalDateTime startDate, 
        @Param("endDate") java.time.LocalDateTime endDate
    );
    
    /**
     * Find audit logs with confidence score above threshold
     */
    @Query("SELECT a FROM AuditLog a WHERE a.confidenceScore >= :threshold ORDER BY a.createdAt DESC")
    List<AuditLog> findByConfidenceScoreGreaterThanEqualOrderByCreatedAtDesc(@Param("threshold") Double threshold);
    
    /**
     * Find audit logs by agent type
     */
    List<AuditLog> findByAgentTypeOrderByCreatedAtDesc(String agentType);
} 