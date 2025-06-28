package com.mdm.mcp.service;

import com.mdm.mcp.model.Rule;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RuleService {
    
    private final Map<String, List<Rule>> rulesByDomain = new ConcurrentHashMap<>();
    
    public void saveRule(Rule rule) {
        rulesByDomain.computeIfAbsent(rule.getDomain(), k -> new ArrayList<>()).add(rule);
    }
    
    public List<Rule> getRules(String domain) {
        return rulesByDomain.getOrDefault(domain, new ArrayList<>());
    }
} 