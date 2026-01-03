package com.nhmk.agentic_example.domain.chatbot;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultInstructionBuilderTest {

    @Test
    void loadsExplainLongTemplateAndAppendsTone() {
        InstructionBuilder b = new DefaultInstructionBuilder();
        InstructionContext ctx = new InstructionContext("long", "explain", "formal", null, false, null);
        String out = b.build(ctx);
        assertThat(out).contains("Trả lời chi tiết");
        assertThat(out).contains("Tone: formal");
    }

    @Test
    void fallsBackToDefaultWhenIntentUnknown() {
        InstructionBuilder b = new DefaultInstructionBuilder();
        InstructionContext ctx = new InstructionContext("normal", "unknown-intent", null, null, false, null);
        String out = b.build(ctx);
        // default_normal.st contains 'Trả lời rõ ràng'
        assertThat(out).contains("Trả lời rõ ràng");
    }

    @Test
    void includesSourcesAndMaxLengthHints() {
        InstructionBuilder b = new DefaultInstructionBuilder();
        InstructionContext ctx = new InstructionContext("brief", "explain", null, null, true, 50);
        String out = b.build(ctx);
        assertThat(out).contains("nguồn").as("should request sources when includeSources=true");
        assertThat(out).contains("Giới hạn độ dài");
    }
}
