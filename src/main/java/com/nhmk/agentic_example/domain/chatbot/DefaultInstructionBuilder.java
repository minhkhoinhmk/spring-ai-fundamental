package com.nhmk.agentic_example.domain.chatbot;

import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Instruction builder that resolves prompt templates from classpath resources under /prompts/*.st.
 * Responsibility: choose the right template based on intent/detail level and append optional tone/length/source hints.
 * <p>
 * Domain logic: encapsulates the policy for building AI instructions based on user context.
 * This keeps the controller thin and allows template-based flexibility.
 */
@Component
public class DefaultInstructionBuilder implements InstructionBuilder {

    // Templates are loaded on demand from classpath following convention: prompts/{intent}_{detail}.st
    // Example: prompts/explain_long.st, prompts/summarize_brief.st
    public DefaultInstructionBuilder() {
        // No initialization needed; templates are resolved dynamically from resources
    }

    @Override
    public String build(InstructionContext ctx) {
        // Normalize inputs to lowercase for consistent template matching
        String intent = normalize(ctx.intent(), "default");
        String detail = normalize(ctx.detailLevel(), "normal");

        // Load template using fallback chain:
        // 1. Try exact match: {intent}_{detail}.st
        // 2. Fall back to {intent}_normal.st
        // 3. Fall back to {intent}_brief.st
        // 4. Fall back to default_{detail}.st
        // 5. Fall back to default_normal.st
        // 6. Fall back to default_brief.st
        // 7. Use hardcoded fallback if all else fails
        String template = tryLoadTemplate(intent, detail);
        if (template == null) template = tryLoadTemplate(intent, "normal");
        if (template == null) template = tryLoadTemplate(intent, "brief");
        if (template == null) template = tryLoadTemplate("default", detail);
        if (template == null) template = tryLoadTemplate("default", "normal");
        if (template == null) template = tryLoadTemplate("default", "brief");
        if (template == null) template = "Trả lời rõ ràng.";

        StringBuilder out = new StringBuilder(template);

        // Post-process: append optional directives based on context
        // Tone directive: guides the model's writing style (e.g., "formal", "casual")
        if (ctx.tone() != null && !ctx.tone().isBlank()) {
            out.append(" Tone: ").append(ctx.tone()).append(".");
        }

        // Max length hint: guides response brevity (useful for token/word limits)
        if (Objects.nonNull(ctx.maxLength())) {
            out.append(" (Giới hạn độ dài: ~").append(ctx.maxLength()).append(" tokens.)");
        }

        // Include sources directive: requests citation/references when available
        if (ctx.includeSources()) {
            out.append(" Nếu có nguồn, hãy ghi rõ nguồn tham khảo hoặc nêu rõ khi không có nguồn trực tiếp.");
        }

        return out.toString();
    }

    /**
     * Normalize string values: trim whitespace and convert to lowercase.
     * Returns fallback if input is null.
     */
    private String normalize(String value, String fallback) {
        if (value == null) return fallback;
        return value.trim().toLowerCase();
    }

    /**
     * Attempt to load a template file from classpath under prompts/{intent}_{detail}.st
     * Returns null if the resource doesn't exist or cannot be read.
     * This allows the fallback chain to continue gracefully.
     */
    private String tryLoadTemplate(String intent, String detail) {
        String resourcePath = String.format("prompts/%s_%s.st", intent, detail);
        try (var is = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath)) {
            if (is == null) return null; // Resource not found

            // Read all lines and return as trimmed string
            try (var br = new java.io.BufferedReader(new java.io.InputStreamReader(is))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line).append('\n');
                }
                return sb.toString().trim();
            }
        } catch (Exception e) {
            // Swallow exception and return null to allow fallback chain to continue
            // This handles IO errors, missing files, etc. gracefully
            return null;
        }
    }
}
