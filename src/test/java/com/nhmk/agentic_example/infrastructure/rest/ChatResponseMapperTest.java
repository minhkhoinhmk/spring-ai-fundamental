package com.nhmk.agentic_example.infrastructure.rest;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ChatResponseMapperTest {

    private final ChatResponseMapper mapper = new ChatResponseMapper();

    @Test
    void returnsTextForBlankResponse() {
        ChatResponse response = mapper.map("");
        assertThat(response.type()).isEqualTo("text");
        assertThat(response.text()).isEmpty();
        assertThat(response.lines()).isNull();
    }

    @Test
    void returnsFormattedForToolResponses() {
        ChatResponse response = mapper.map("Tóm tắt từ Google Search:\n- line1\n- line2");
        assertThat(response.type()).isEqualTo("formatted");
        assertThat(response.lines()).containsExactly("Tóm tắt từ Google Search:", "- line1", "- line2");
    }

    @Test
    void returnsMultilineWhenContainsNewlines() {
        ChatResponse response = mapper.map("line1\nline2");
        assertThat(response.type()).isEqualTo("multiline");
        assertThat(response.lines()).containsExactly("line1", "line2");
    }

    @Test
    void returnsTextWhenSingleLine() {
        ChatResponse response = mapper.map("single line");
        assertThat(response.type()).isEqualTo("text");
        assertThat(response.text()).isEqualTo("single line");
    }
}
