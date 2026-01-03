package com.nhmk.agentic_example.infrastructure.webextraction;

import com.nhmk.agentic_example.domain.extract.DomainFetchResult;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PageExtractionService {
    private static final Logger logger = LoggerFactory.getLogger(PageExtractionService.class);
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
            String html = fetcher.fetchHtml(url);
            if (html == null || html.isBlank()) {
                return new DomainFetchResult(url, "", "fetch-empty");
            }

            // Log fetched HTML summary and full content (full content at DEBUG)
            logger.info("Fetched HTML for {} ({} bytes)", url, html.length());
            logger.debug("Fetched HTML content for {}:\n{}", url, html);

            String article = extractor.extract(html, url);
            if (article != null && !article.isBlank()) {
                String normalized = normalizeAndTruncate(article);
                logger.info("Readability extractor produced {} chars for {}", normalized.length(), url);
                logger.debug("Extracted text (readability) for {}:\n{}", url, normalized);
                return new DomainFetchResult(url, normalized, "readability");
            }

            try {
                String text = HtmlMainTextExtractor.extractMainText(Jsoup.parse(html));
                String normalized = normalizeAndTruncate(text);
                logger.info("Fallback DOM heuristics produced {} chars for {}", normalized.length(), url);
                logger.debug("Extracted text (fallback) for {}:\n{}", url, normalized);
                return new DomainFetchResult(url, normalized, "jsoup-fallback");
            } catch (Exception e) {
                logger.warn("Fallback extraction failed for {}", url, e);
                return new DomainFetchResult(url, "", "fallback-error");
            }
        } catch (Exception e) {
            logger.warn("extractForSummary error for {}", url, e);
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
