package com.mdm.mcp.config;

import com.mdm.agent.core.AgentOrchestrator;
import com.mdm.agent.core.AgentRegistry;
import com.mdm.agent.core.RestMCPClient;
import com.mdm.agent.mdm.impl.IntelligentMatchingAgent;
import com.mdm.agent.mdm.impl.IntelligentMergeAgent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AgentConfig {

    @Value("${server.port:8080}")
    private int serverPort;

    @Bean
    public RestMCPClient mcpClient() {
        String baseUrl = "http://localhost:" + serverPort;
        return new RestMCPClient(baseUrl);
    }

    @Bean
    public AgentRegistry agentRegistry() {
        return new AgentRegistry();
    }

    @Bean
    public IntelligentMatchingAgent intelligentMatchingAgent(RestMCPClient mcpClient) {
        return new IntelligentMatchingAgent(mcpClient);
    }

    @Bean
    public IntelligentMergeAgent intelligentMergeAgent(RestMCPClient mcpClient) {
        return new IntelligentMergeAgent(mcpClient);
    }

    @Bean
    public AgentOrchestrator agentOrchestrator(AgentRegistry agentRegistry) {
        return new AgentOrchestrator(agentRegistry);
    }
} 