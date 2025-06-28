package com.mdm.mcp.repository;

import com.mdm.mcp.model.MergeRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MergeRuleRepository extends JpaRepository<MergeRule, Long> {
    
    Optional<MergeRule> findByRuleName(String ruleName);
    
    List<MergeRule> findByRuleType(String ruleType);
    
    List<MergeRule> findByEntityType(String entityType);
    
    List<MergeRule> findByRuleTypeAndEntityType(String ruleType, String entityType);
    
    List<MergeRule> findByIsActiveTrue();
    
    List<MergeRule> findByIsActiveTrueOrderByPriorityAsc();
    
    List<MergeRule> findByEntityTypeAndIsActiveTrueOrderByPriorityAsc(String entityType);
} 