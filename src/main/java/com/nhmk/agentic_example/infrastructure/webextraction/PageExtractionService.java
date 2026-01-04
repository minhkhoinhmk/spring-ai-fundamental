package com.nhmk.agentic_example.infrastructure.webextraction;

import com.nhmk.agentic_example.domain.extract.DomainFetchResult;
import org.jsoup.Jsoup;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PageExtractionService {
    private final PageFetcher fetcher;
    private final ReadabilityExtractor extractor;
    private final int maxChars;

    public PageExtractionService(PageFetcher fetcher, ReadabilityExtractor extractor, int maxChars) {
        this.fetcher = fetcher;
        this.extractor = extractor;
        this.maxChars = maxChars;
    }

    public DomainFetchResult extractForSummary(String url) {
        try {
            FetchResult fetched = fetcher.fetch(url);
            if (fetched == null || !fetched.isSuccess()) {
                String reason = fetched == null ? "fetch-empty" : fetched.getError();
                return new DomainFetchResult(url, "", reason == null ? "fetch-empty" : reason);
            }

            String contentType = fetched.getContentType().toLowerCase();

            // If HTML, parse with readability extractor
            if (contentType.contains("text/html") || contentType.contains("application/xhtml+xml")) {
                String html = new String(fetched.getBody().orElse(new byte[0]));
                log.info("Fetched HTML for {} ({} bytes)", url, html.length());

                String article = extractor.extract(html, url);
                if (article != null && !article.isBlank()) {
                    String normalized = normalizeAndTruncate(article);
                    log.info("Readability extractor produced {} chars for {}", normalized.length(), url);
                    return new DomainFetchResult(url, normalized, "readability");
                }

                try {
                    String text = HtmlMainTextExtractor.extractMainText(org.jsoup.Jsoup.parse(html));
                    String normalized = normalizeAndTruncate(text);
                    log.info("Fallback DOM heuristics produced {} chars for {}", normalized.length(), url);
                    return new DomainFetchResult(url, normalized, "jsoup-fallback");
                } catch (Exception e) {
                    log.error("Fallback extraction failed for {}", url, e);
                    return new DomainFetchResult(url, "", "fallback-error");
                }
            }

            // Non-HTML (PDF, DOCX, etc.) -> use DocumentSplitter (Tika)
            try (java.io.InputStream in = new java.io.ByteArrayInputStream(fetched.getBody().orElse(new byte[0]))) {
                var docSplitter = new com.nhmk.agentic_example.infrastructure.pdf.DocumentSplitter();
                java.util.List<String> chunks = docSplitter.split(in);
                String combined = String.join("\n---\n", chunks);
                String normalized = normalizeAndTruncate(combined);
                log.info("DocumentSplitter produced {} chars for {}", normalized.length(), url);
                return new DomainFetchResult(url, normalized, "document");
            } catch (Exception e) {
                log.error("Document extraction failed for {}", url, e);
                return new DomainFetchResult(url, "", "document-error");
            }

        } catch (Exception e) {
            log.error("extractForSummary error for {}", url, e);
            return new DomainFetchResult(url, "", "error");
        }
    }

    private String normalizeAndTruncate(String text) {
        if (text == null) text = "";
        String n = text.replaceAll("\\s+", " ").trim();
        if (n.length() > maxChars) return n.substring(0, maxChars).trim() + "...";
        return n;
    }
}
