package com.nhmk.agentic_example.infrastructure.webextraction;

/**
 * FetchResult-aware PageFetcher. Implementations should return a FetchResult
 * containing status, content-type and body (or an error) so callers can
 * decide how to parse the response (HTML vs PDF vs other).
 */
public interface PageFetcher {
    FetchResult fetch(String url);
}
