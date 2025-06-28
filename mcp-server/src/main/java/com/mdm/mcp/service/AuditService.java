package com.mdm.mcp.service;

import com.mdm.mcp.agent.Agent;
import com.mdm.mcp.model.AuditLog;
import com.mdm.mcp.model.DataEntity;
import com.mdm.mcp.model.MatchCandidate;
import com.mdm.mcp.model.MergeResult;
import com.mdm.mcp.repository.AuditLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AuditService {
    
    @Autowired
    private AuditLogRepository auditLogRepository;
    
    /**
     * Log agent activity
     */
    public void logAgentActivity(Agent agent, String operationType, String entityIds, 
                                Double confidenceScore, String decisionReason, 
                                String status, Long executionTimeMs, Map<String, Object> metadata) {
        try {
            AuditLog auditLog = AuditLog.builder()
                .operationType(operationType)
                .agentId(agent.getAgentId())
                .agentType(agent.getAgentType())
                .entityIds(entityIds)
                .confidenceScore(confidenceScore)
                .decisionReason(decisionReason)
                .status(status)
                .executionTimeMs(executionTimeMs)
                .createdAt(LocalDateTime.now())
                .metadata(metadata != null ? metadata.toString() : null)
                .build();
            
            auditLogRepository.save(auditLog);
            log.info("Audit log created: {} - {} - {}", agent.getAgentId(), operationType, status);
            
        } catch (Exception e) {
            log.error("Failed to create audit log: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Log match found by agent
     */
    public void logMatchFound(Agent agent, MatchCandidate matchCandidate, Long executionTimeMs) {
        String entityIds = matchCandidate.getEntity1().getEntityId() + "," + matchCandidate.getEntity2().getEntityId();
        
        logAgentActivity(
            agent,
            "MATCH_FOUND",
            entityIds,
            matchCandidate.getConfidenceScore(),
            matchCandidate.getMatchReason(),
            "SUCCESS",
            executionTimeMs,
            Map.of("ruleName", matchCandidate.getRuleName())
        );
    }
    
    /**
     * Log merge initiated by agent
     */
    public void logMergeInitiated(Agent agent, MatchCandidate matchCandidate) {
        String entityIds = matchCandidate.getEntity1().getEntityId() + "," + matchCandidate.getEntity2().getEntityId();
        
        logAgentActivity(
            agent,
            "MERGE_INITIATED",
            entityIds,
            matchCandidate.getConfidenceScore(),
            "Merge initiated by " + agent.getAgentId(),
            "PENDING",
            null,
            Map.of("ruleName", matchCandidate.getRuleName())
        );
    }
    
    /**
     * Log merge completed by agent
     */
    public void logMergeCompleted(Agent agent, MergeResult mergeResult, Long executionTimeMs) {
        String entityIds = String.join(",", mergeResult.getSourceEntityIds());
        
        logAgentActivity(
            agent,
            "MERGE_COMPLETED",
            entityIds,
            mergeResult.getConfidenceScore(),
            mergeResult.getMessage(),
            mergeResult.getStatus(),
            executionTimeMs,
            Map.of(
                "mergeId", mergeResult.getMergeId(),
                "mergedEntityId", mergeResult.getMergedEntityId(),
                "appliedRuleName", mergeResult.getAppliedRuleName()
            )
        );
    }
    
    /**
     * Log bulk operation
     */
    public void logBulkOperation(Agent agent, String operationType, int totalItems, int successfulItems, 
                                Long executionTimeMs, String details) {
        logAgentActivity(
            agent,
            operationType,
            "BULK_OPERATION",
            null,
            String.format("Bulk operation: %d/%d successful - %s", successfulItems, totalItems, details),
            successfulItems == totalItems ? "SUCCESS" : "PARTIAL_SUCCESS",
            executionTimeMs,
            Map.of("totalItems", totalItems, "successfulItems", successfulItems)
        );
    }
    
    /**
     * Log entity creation
     */
    public void logEntityCreated(DataEntity entity, String source) {
        AuditLog auditLog = AuditLog.builder()
            .operationType("ENTITY_CREATED")
            .agentId("SYSTEM")
            .agentType("SYSTEM")
            .entityIds(entity.getEntityId())
            .confidenceScore(entity.getConfidenceScore())
            .decisionReason("Entity created from " + source)
            .status("SUCCESS")
            .executionTimeMs(null)
            .createdAt(LocalDateTime.now())
            .metadata(Map.of("sourceSystem", entity.getSourceSystem(), "entityType", entity.getEntityType()).toString())
            .build();
        
        auditLogRepository.save(auditLog);
        log.info("Entity creation logged: {}", entity.getEntityId());
    }
    
    /**
     * Log entity update
     */
    public void logEntityUpdated(DataEntity entity, String reason) {
        AuditLog auditLog = AuditLog.builder()
            .operationType("ENTITY_UPDATED")
            .agentId("SYSTEM")
            .agentType("SYSTEM")
            .entityIds(entity.getEntityId())
            .confidenceScore(entity.getConfidenceScore())
            .decisionReason(reason)
            .status("SUCCESS")
            .executionTimeMs(null)
            .createdAt(LocalDateTime.now())
            .metadata(Map.of("sourceSystem", entity.getSourceSystem(), "entityType", entity.getEntityType()).toString())
            .build();
        
        auditLogRepository.save(auditLog);
        log.info("Entity update logged: {}", entity.getEntityId());
    }
    
    /**
     * Get audit logs for an entity
     */
    public List<AuditLog> getAuditLogsForEntity(String entityId) {
        return auditLogRepository.findByEntityIdsContainingOrderByCreatedAtDesc(entityId);
    }
    
    /**
     * Get audit logs for an agent
     */
    public List<AuditLog> getAuditLogsForAgent(String agentId) {
        return auditLogRepository.findByAgentIdOrderByCreatedAtDesc(agentId);
    }
    
    /**
     * Get audit logs by operation type
     */
    public List<AuditLog> getAuditLogsByOperationType(String operationType) {
        return auditLogRepository.findByOperationTypeOrderByCreatedAtDesc(operationType);
    }
    
    /**
     * Get recent audit logs
     */
    public List<AuditLog> getRecentAuditLogs(int limit) {
        return auditLogRepository.findTopNByOrderByCreatedAtDesc(limit);
    }
    
    /**
     * Get audit statistics
     */
    public Map<String, Object> getAuditStatistics() {
        long totalLogs = auditLogRepository.count();
        long matchLogs = auditLogRepository.countByOperationType("MATCH_FOUND");
        long mergeLogs = auditLogRepository.countByOperationType("MERGE_COMPLETED");
        long successLogs = auditLogRepository.countByStatus("SUCCESS");
        
        return Map.of(
            "totalLogs", totalLogs,
            "matchLogs", matchLogs,
            "mergeLogs", mergeLogs,
            "successLogs", successLogs,
            "successRate", totalLogs > 0 ? (double) successLogs / totalLogs : 0.0
        );
    }
} 