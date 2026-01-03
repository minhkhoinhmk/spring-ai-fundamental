package com.nhmk.agentic_example.infrastructure.webextraction;

/**
 * Extract main article text from HTML. Returns empty string on failure.
 */
public interface ReadabilityExtractor {
    String extract(String html, String url);
}
