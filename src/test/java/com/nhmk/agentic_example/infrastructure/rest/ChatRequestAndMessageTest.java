package com.nhmk.agentic_example.infrastructure.rest;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ChatRequestAndMessageTest {

    @Test
    void chatRequestContainsSessionAndPrompt() {
        ChatRequest r = new ChatRequest("s123", "hello", null);
        assertThat(r.sessionId()).isEqualTo("s123");
        assertThat(r.prompt()).isEqualTo("hello");
        assertThat(r.detailLevel()).isNull();
    }
}
