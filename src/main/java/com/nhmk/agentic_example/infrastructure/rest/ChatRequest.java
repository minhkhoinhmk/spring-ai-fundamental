package com.nhmk.agentic_example.infrastructure.rest;

/**
 * DTO for chat requests coming from HTTP clients.
 * sessionId is optional; when provided, conversation history will be persisted under this id.
 * detailLevel can be "brief", "normal", or "long" (optional).
 * intent and tone are optional extensions used to build more specific AI instructions.
 */
public record ChatRequest(String sessionId,
						  String prompt,
						  String detailLevel,
						  String intent,
						  String tone) {

	// Backwards-compatible convenience constructor for existing callers.
	public ChatRequest(String sessionId, String prompt, String detailLevel) {
		this(sessionId, prompt, detailLevel, null, null);
	}

}
