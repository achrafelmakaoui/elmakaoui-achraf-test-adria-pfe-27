package com.adria.bank.chatbot.web;

import com.adria.bank.chatbot.agent.BankAgent;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/** REST client entry point of the chatbot. */
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final BankAgent agent;

    public ChatController(BankAgent agent) {
        this.agent = agent;
    }

    @PostMapping
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        if (request.message() == null || request.message().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        String conversationId = (request.conversationId() == null || request.conversationId().isBlank())
                ? UUID.randomUUID().toString()
                : request.conversationId();
        return ResponseEntity.ok(new ChatResponse(conversationId, agent.chat(request.message(), conversationId)));
    }
}
