package com.mdm.shared.api;

public interface MCPClient {
    String fetchContext(String domain);
    void sendRequest(String request);
} 