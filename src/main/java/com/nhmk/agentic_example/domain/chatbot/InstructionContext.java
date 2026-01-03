package com.nhmk.agentic_example.domain.chatbot;

/**
 * Value object that carries parameters used to build an instruction for the AI.
 * Kept intentionally small and extensible for future fields.
 */
public record InstructionContext(
        String detailLevel, // e.g. "brief", "normal", "long"
        String intent,     // e.g. "explain", "summarize", "generate_tests"
        String tone,       // e.g. "formal", "casual"
        String format,     // e.g. "bullet", "numbered", "essay"
        boolean includeSources,
        Integer maxLength  // optional token/word limit
) {
}
