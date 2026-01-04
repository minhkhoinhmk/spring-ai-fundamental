package com.nhmk.agentic_example.infrastructure.webextraction;

import com.nhmk.agentic_example.domain.extract.DomainFetchResult;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PageExtractionServiceTest {

    private static final String SAMPLE_HTML = "<html><body><article>Primary content for testing extraction.</article></body></html>";

    @Test
    void usesReadabilityExtractorWhenAvailable() {
        PageFetcher fetcher = url -> new FetchResult(200, "text/html; charset=utf-8", SAMPLE_HTML.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        ReadabilityExtractor extractor = (html, url) -> "Readability result";
        PageExtractionService service = new PageExtractionService(fetcher, extractor, 300);

        DomainFetchResult result = service.extractForSummary("http://example.com");

        assertThat(result.getExtractedText()).isEqualTo("Readability result");
        assertThat(result.getSource()).isEqualTo("readability");
    }

    @Test
    void fallsBackToDomHeuristicsWhenExtractorEmpty() {
        PageFetcher fetcher = url -> new FetchResult(200, "text/html; charset=utf-8", SAMPLE_HTML.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        ReadabilityExtractor extractor = (html, url) -> "";
        PageExtractionService service = new PageExtractionService(fetcher, extractor, 300);

        DomainFetchResult result = service.extractForSummary("http://example.com");

        assertThat(result.getExtractedText()).contains("Primary content for testing extraction.");
        assertThat(result.getSource()).isEqualTo("jsoup-fallback");
    }

    @Test
    void truncatesLongContent() {
        String longContent = "<html><body><article>" + "x".repeat(500) + "</article></body></html>";
        PageFetcher fetcher = url -> new FetchResult(200, "text/html; charset=utf-8", longContent.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        ReadabilityExtractor extractor = (html, url) -> "";
        PageExtractionService service = new PageExtractionService(fetcher, extractor, 100);

        DomainFetchResult result = service.extractForSummary("http://example.com");

        assertThat(result.getExtractedText()).hasSizeLessThanOrEqualTo(103); // includes ellipsis
        assertThat(result.getExtractedText()).endsWith("...");
    }
}
