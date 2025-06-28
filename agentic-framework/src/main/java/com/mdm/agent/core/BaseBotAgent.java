package com.mdm.agent.core;

import com.mdm.shared.core.BotAgent;

public abstract class BaseBotAgent extends BotAgent {
    public BaseBotAgent(com.mdm.shared.api.MCPClient mcpClient) {
        super(mcpClient);
    }
} 