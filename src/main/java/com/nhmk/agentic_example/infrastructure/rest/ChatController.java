package com.nhmk.agentic_example.infrastructure.rest;

import com.nhmk.agentic_example.application.chatbot.ChatService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.UUID;

@RestController
@RequestMapping("/api/chat")
public class ChatController {
    private final ChatService chatService;
    private final ChatResponseMapper responseMapper;

    public ChatController(ChatService chatService,
                          ChatResponseMapper responseMapper) {
        this.chatService = chatService;
        this.responseMapper = responseMapper;
    }


    @PostMapping
    public ChatResponse chat(@RequestBody ChatRequest request) {
        String sessionId = ensureSessionId(request.sessionId());

        String response = chatService.chat(
                request.prompt(),
                sessionId,
                request.detailLevel(),
                request.intent(),
                request.tone());

        return responseMapper.map(response);
    }

    @GetMapping("/rag")
    public ChatResponse ask(@RequestParam("question") String question) {
        String response = chatService.chatWithRag(question);
        return responseMapper.map(response);
    }

    /**
     * Stream endpoint with full context support (session, instruction building, conversation history).
     * Returns Server-Sent Events (SSE) for real-time streaming response.
     * Client should use EventSource or fetch with text/event-stream accept header.
     */
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatStreamPost(@RequestBody ChatRequest request) {
        String sessionId = ensureSessionId(request.sessionId());

        return chatService.chatStream(
                request.prompt(),
                sessionId,
                request.detailLevel(),
                request.intent(),
                request.tone());
    }

    // Generate or reuse session id so Spring AI chat memory advisor can link turns.
    private String ensureSessionId(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return UUID.randomUUID().toString();
        }
        return sessionId;
    }
}
