package com.adria.bank.account.config;

import com.adria.bank.account.mcp.AccountTools;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class McpServerConfig {

    @Bean
    public ToolCallbackProvider accountToolCallbacks(AccountTools tools) {
        return MethodToolCallbackProvider.builder().toolObjects(tools).build();
    }
}
