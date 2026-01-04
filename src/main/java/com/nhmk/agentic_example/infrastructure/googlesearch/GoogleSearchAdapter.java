package com.nhmk.agentic_example.infrastructure.googlesearch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhmk.agentic_example.application.extract.PageExtractionPort;
import com.nhmk.agentic_example.application.search.SearchPort;
import com.nhmk.agentic_example.domain.extract.DomainFetchResult;
import com.nhmk.agentic_example.domain.search.SearchQuery;
import com.nhmk.agentic_example.domain.search.SearchResult;
import com.nhmk.agentic_example.infrastructure.cache.SimpleCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component
@Slf4j
public class GoogleSearchAdapter implements SearchPort {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private final WebClient webClient;
    private final String apiKey;
    private final String cx;
    private final PageExtractionPort pageExtractionPort;
    private final SimpleCache<com.nhmk.agentic_example.domain.search.SearchResult> cache = new SimpleCache<>(60 * 60 * 1000); // 1h cache

    public GoogleSearchAdapter(@Value("${google.search.api-key}") String apiKey,
                               @Value("${google.search.cx}") String cx,
                               PageExtractionPort pageExtractionPort) {
        this.apiKey = apiKey;
        this.cx = cx;
        this.webClient = WebClient.builder().baseUrl("https://www.googleapis.com/customsearch/v1").build();
        this.pageExtractionPort = pageExtractionPort;
    }

    public SearchResult search(SearchQuery q) {
        String cacheKey = buildCacheKey(q);
        SearchResult cached = cache.get(cacheKey);
        if (cached != null) return cached;

        try {
            String res = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .queryParam("key", apiKey)
                            .queryParam("cx", cx)
                            .queryParam("q", q.q())
                            .queryParam("num", q.num())
                            .queryParam("gl", q.country() == null ? "" : q.country())
                            .queryParam("lr", q.language() == null ? "" : q.language())
                            .queryParam("dateRestrict", q.dateRestrict() == null ? "" : q.dateRestrict())
                            .build())
                    .retrieve().bodyToMono(String.class).block();

            JsonNode root = MAPPER.readTree(res);
            JsonNode items = root.path("items");
            List<ResultItem> list = new ArrayList<>();
            if (items.isArray()) {
                for (JsonNode item : items) {
                    String title = item.path("title").asText("");
                    String snippet = item.path("snippet").asText("");
                    String link = item.path("link").asText("");
                    String pageExcerpt = "";
                    try {
                        DomainFetchResult fr = pageExtractionPort.extractForSummary(link);
                        if (fr != null) pageExcerpt = fr.getExtractedText();
                    } catch (Exception ex) {
                        log.info("Failed to extract page {}: {}", link, ex.toString());
                    }
                    list.add(new ResultItem(title, snippet, link, pageExcerpt));
                }
            }

            // rerank by simple heuristic: count occurrences of query tokens in excerpt + snippet/title bonus
            String[] tokens = q.q().toLowerCase().split("\\s+");
            for (ResultItem it : list) {
                int score = 0;
                String combined = (it.title + " " + it.snippet + " " + it.pageExcerpt).toLowerCase();
                for (String t : tokens)
                    if (!t.isBlank()) {
                        int idx = 0;
                        while ((idx = combined.indexOf(t, idx)) != -1) {
                            score++;
                            idx += t.length();
                        }
                    }
                if (it.title.toLowerCase().contains(q.q().toLowerCase())) score += 5;
                it.score = score;
            }

            list.sort(Comparator.comparingInt((ResultItem r) -> r.score).reversed());

            // prepare top snippets
            List<String> snippets = new ArrayList<>();
            int count = Math.min(3, list.size());
            for (int i = 0; i < count; i++) {
                ResultItem it = list.get(i);
                if (it.pageExcerpt != null && !it.pageExcerpt.isBlank()) {
                    snippets.add(String.format("- %s: %s (Nguồn: %s)\n  Trích đoạn: %s", it.title, it.snippet, it.link, it.pageExcerpt));
                } else {
                    snippets.add(String.format("- %s: %s (Nguồn: %s)", it.title, it.snippet, it.link));
                }
            }

            SearchResult out = new SearchResult(q.q(), snippets);
            cache.put(cacheKey, out);
            return out;
        } catch (Exception e) {
            log.error("Google search error", e);
            return new SearchResult(q.q(), List.of("Không thể tìm kiếm Google: " + e.getMessage()));
        }
    }

    private String buildCacheKey(SearchQuery q) {
        return q.q() + "|" + q.country() + "|" + q.language() + "|" + q.dateRestrict() + "|" + q.num();
    }

    private static class ResultItem {
        final String title;
        final String snippet;
        final String link;
        final String pageExcerpt;
        int score;

        ResultItem(String title, String snippet, String link, String pageExcerpt) {
            this.title = title;
            this.snippet = snippet;
            this.link = link;
            this.pageExcerpt = pageExcerpt;
            this.score = 0;
        }
    }
}
