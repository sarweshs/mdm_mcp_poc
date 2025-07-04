package com.mdm.mcp.api;

import com.mdm.mcp.model.Rule;
import com.mdm.mcp.service.RuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rules")
@CrossOrigin(originPatterns = "*")
public class RuleController {
    @Autowired
    private RuleService ruleService;

    @PostMapping
    public void addRule(@RequestBody Rule rule) {
        ruleService.saveRule(rule);
    }

    @GetMapping
    public List<Rule> getAllRules() {
        return ruleService.getAllRules();
    }

    @GetMapping("/{domain}")
    public List<Rule> getRulesByDomain(@PathVariable String domain) {
        return ruleService.getRules(domain);
    }
}