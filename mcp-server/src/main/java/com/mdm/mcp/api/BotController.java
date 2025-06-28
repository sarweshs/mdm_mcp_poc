package com.mdm.mcp.api;

import com.mdm.shared.api.MCPClient;
import com.mdm.shared.core.BotAgent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/bot")
public class BotController {
    
    @Autowired
    private MCPClient mcpClient;
    
    @PostMapping("/execute")
    public Map<String, String> executeBot(@RequestBody Map<String, String> request) {
        String botType = request.get("botType");
        String input = request.get("input");
        
        BotAgent bot = createBot(botType);
        String result = bot.execute(input);
        
        return Map.of(
            "botType", botType,
            "input", input,
            "result", result
        );
    }
    
    private BotAgent createBot(String botType) {
        if ("LifeSciencesBot".equals(botType)) {
            return new LifeSciencesBot(mcpClient);
        } else {
            throw new IllegalArgumentException("Unknown bot type: " + botType);
        }
    }
    
    // Simple implementation of LifeSciencesBot
    private static class LifeSciencesBot extends BotAgent {
        public LifeSciencesBot(MCPClient mcpClient) {
            super(mcpClient);
        }

        @Override
        public String execute(String input) {
            String context = mcpClient.fetchContext("drug_interactions");
            return "Checked interactions for: " + input + ". Context: " + context;
        }
    }
} 