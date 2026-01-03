package com.nhmk.agentic_example.application.chatbot;

import reactor.core.publisher.Flux;

public interface ChatService {
    String chat(String userPrompt, String conversationId, String detailLevel, String intent, String tone);

    String chatWithRag(String question);

    Flux<String> chatStream(String userPrompt, String conversationId, String detailLevel, String intent, String tone);
}
