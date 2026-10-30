package com.adria.bank.chatbot.agent;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.stereotype.Component;

@Component
public class BankAgent {

    private static final String SYSTEM_PROMPT = """
            You are the assistant of a bank. You help users consult accounts, check balances,
            list and record transactions, and make transfers between accounts.

            Rules:
            - ALWAYS use the available tools to get or change bank data. Never invent an account, a balance or a transaction.
            - Accounts and transactions are identified by UUIDs. If the user did not give a required value
              (account id, amount, currency, merchant category, country), ask for it before calling a tool.
            - Before a payment or a transfer, state what you are going to do. After it, report the result.
            - If a tool returns an error (limit exceeded, blocked account, insufficient balance, currency mismatch,
              service unavailable), explain it simply to the user and do not retry blindly.
            - Answer in the language of the user, briefly.
            """;

    private final ChatClient chatClient;

    public BankAgent(ChatModel chatModel, ChatMemory chatMemory, ToolCallbackProvider toolCallbackProvider) {
        this.chatClient = ChatClient.builder(chatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .defaultTools(toolCallbackProvider)
                .build();
    }

    public String chat(String message, String conversationId) {
        return chatClient.prompt()
                .user(message)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .content();
    }
}
