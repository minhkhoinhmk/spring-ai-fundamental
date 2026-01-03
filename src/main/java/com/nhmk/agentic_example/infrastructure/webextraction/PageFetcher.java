package com.nhmk.agentic_example.infrastructure.webextraction;

/**
 * Fetch raw HTML for the given URL. Returns empty string on failure.
 */
public interface PageFetcher {
    String fetchHtml(String url);
}
