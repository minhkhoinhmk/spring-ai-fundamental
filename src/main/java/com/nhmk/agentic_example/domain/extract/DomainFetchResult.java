package com.nhmk.agentic_example.domain.extract;

public class DomainFetchResult {
    private final String url;
    private final String extractedText;
    private final String source;

    public DomainFetchResult(String url, String extractedText, String source) {
        this.url = url;
        this.extractedText = extractedText;
        this.source = source;
    }

    public String getUrl() { return url; }
    public String getExtractedText() { return extractedText; }
    public String getSource() { return source; }
}
