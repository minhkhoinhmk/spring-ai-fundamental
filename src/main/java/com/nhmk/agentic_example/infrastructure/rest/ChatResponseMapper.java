package com.nhmk.agentic_example.infrastructure.rest;

import org.springframework.stereotype.Component;

@Component
public class ChatResponseMapper {

    public ChatResponse map(String response) {
        if (response == null || response.isBlank()) {
            return new ChatResponse("text", response, null);
        }

        if (response.startsWith("Tóm tắt từ Google Search:") ||
            response.startsWith("Tin tức mới nhất") ||
            response.contains("Trích đoạn:")) {
            return new ChatResponse("formatted", null, splitLines(response));
        }

        if (response.contains("\n")) {
            return new ChatResponse("multiline", null, splitLines(response));
        }

        return new ChatResponse("text", response, null);
    }

    private String[] splitLines(String response) {
        return response.split("\\n");
    }
}
