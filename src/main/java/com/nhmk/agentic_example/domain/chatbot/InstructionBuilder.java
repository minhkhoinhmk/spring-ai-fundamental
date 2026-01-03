package com.nhmk.agentic_example.domain.chatbot;

/**
 * Strategy interface for building instructions sent to the AI.
 */
public interface InstructionBuilder {
    /**
     * Build a prompt instruction based on the provided context.
     */
    String build(InstructionContext ctx);
}
