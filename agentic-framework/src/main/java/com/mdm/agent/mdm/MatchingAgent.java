package com.mdm.agent.mdm;

import java.util.List;
import java.util.Map;

public interface MatchingAgent {
    List<Map<String, Object>> findMatches(List<Map<String, Object>> entities);
    double calculateConfidence(Map<String, Object> entity1, Map<String, Object> entity2);
    String getMatchingCriteria();
} 