package com.mdm.agent.skills;

import com.mdm.agent.core.BaseBotAgent;
import com.mdm.shared.api.MCPClient;

public class LifeSciencesBot extends BaseBotAgent {
    public LifeSciencesBot(MCPClient mcpClient) {
        super(mcpClient);
    }

    @Override
    public String execute(String input) {
        String context = mcpClient.fetchContext("drug_interactions");
        return "Checked interactions for: " + input + ". Context: " + context;
    }
} 