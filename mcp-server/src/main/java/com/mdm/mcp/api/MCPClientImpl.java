package com.mdm.mcp.api;

import com.mdm.shared.api.MCPClient;
import org.springframework.stereotype.Component;

@Component
public class MCPClientImpl implements MCPClient {
    
    @Override
    public String fetchContext(String domain) {
        // This would typically make HTTP calls to the MCP server
        // For now, return a mock response
        return "Mock context for domain: " + domain;
    }
    
    @Override
    public void sendRequest(String request) {
        // This would typically send requests to the MCP server
        System.out.println("Sending request: " + request);
    }
} 