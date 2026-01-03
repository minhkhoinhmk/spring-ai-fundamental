package com.nhmk.agentic_example.infrastructure.rest;

public record ChatResponse(String type, String text, String[] lines) { }
