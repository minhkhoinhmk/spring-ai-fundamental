package com.nhmk.agentic_example.infrastructure.webextraction;

import java.util.Optional;

/**
 * Result of fetching a URL. Contains HTTP status, content type and optional
 * body bytes (may be empty on error). Implementations should fill `error`
 * on failure.
 */
public class FetchResult {
    private final int statusCode;
    private final String contentType;
    private final byte[] body;
    private final String error;

    public FetchResult(int statusCode, String contentType, byte[] body) {
        this.statusCode = statusCode;
        this.contentType = contentType == null ? "" : contentType;
        this.body = body;
        this.error = null;
    }

    public FetchResult(String error) {
        this.statusCode = 0;
        this.contentType = "";
        this.body = new byte[0];
        this.error = error;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getContentType() {
        return contentType;
    }

    public Optional<byte[]> getBody() {
        return Optional.ofNullable(body == null ? null : body.length == 0 ? null : body);
    }

    public boolean isSuccess() {
        return error == null && statusCode >= 200 && statusCode < 300 && body != null && body.length > 0;
    }

    public String getError() {
        return error;
    }
}
