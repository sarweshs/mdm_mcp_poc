package com.mdm.shared.core;

import com.mdm.shared.api.MCPClient;

public abstract class BotAgent {
    protected MCPClient mcpClient;

    public BotAgent(MCPClient mcpClient) {
        this.mcpClient = mcpClient;
    }

    public abstract String execute(String input);
} 