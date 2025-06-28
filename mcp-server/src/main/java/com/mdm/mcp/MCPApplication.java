package com.mdm.mcp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class MCPApplication {
    public static void main(String[] args) {
        SpringApplication.run(MCPApplication.class, args);
    }
}