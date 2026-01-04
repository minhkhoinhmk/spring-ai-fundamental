package com.nhmk.agentic_example.infrastructure.tavily;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhmk.agentic_example.application.search.SearchPort;
import com.nhmk.agentic_example.domain.search.SearchQuery;
import com.nhmk.agentic_example.domain.search.SearchResult;
import com.nhmk.agentic_example.infrastructure.cache.SimpleCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tavily Search API adapter - optimized for AI/RAG use cases.
 * Tavily automatically extracts and ranks content for LLM consumption.
 */
@Component
@Slf4j
public class TavilySearchAdapter implements SearchPort {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private final WebClient webClient;
    private final String apiKey;
    private final SimpleCache<SearchResult> cache = new SimpleCache<>(60 * 60 * 1000); // 1h cache

    public TavilySearchAdapter(@Value("${tavily.api-key}") String apiKey) {
        this.apiKey = apiKey;
        this.webClient = WebClient.builder()
                .baseUrl("https://api.tavily.com")
                .build();
    }

    @Override
    public SearchResult search(SearchQuery q) {
        String cacheKey = buildCacheKey(q);
        SearchResult cached = cache.get(cacheKey);
        if (cached != null) return cached;

        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("api_key", apiKey);
            requestBody.put("query", q.q());
            requestBody.put("max_results", q.num() > 0 ? q.num() : 5);
            requestBody.put("include_answer", true);
            // Request raw extracted content from Tavily so we can provide detailed snippets
            requestBody.put("include_raw_content", true);
            requestBody.put("include_images", false);

            String response = webClient.post()
                    .uri("/search")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode root = MAPPER.readTree(response);
            List<String> snippets = new ArrayList<>();

            // Add Tavily's AI-generated answer if available
            String answer = root.path("answer").asText("");
            if (!answer.isBlank()) {
                snippets.add("Tóm tắt AI từ Tavily:\n" + answer);
            }

            // Add search results
            JsonNode results = root.path("results");
            if (results.isArray()) {
                for (JsonNode result : results) {
                    String title = result.path("title").asText("");
                    // Tavily may return raw_content (preferred) or content; prefer raw_content for detail
                    String raw = result.path("raw_content").asText("");
                    String content = (raw == null || raw.isBlank()) ? result.path("content").asText("") : raw;
                    String url = result.path("url").asText("");

                    if (!content.isBlank()) {
                        snippets.add(String.format("- %s\n  Nguồn: %s\n  Nội dung: %s", title, url, content));
                    } else {
                        snippets.add(String.format("- %s (Nguồn: %s)", title, url));
                    }
                }
            }

            SearchResult out = new SearchResult(q.q(), snippets);
            cache.put(cacheKey, out);
            return out;
        } catch (Exception e) {
            log.error("Tavily search error", e);
            return new SearchResult(q.q(), List.of("Không thể tìm kiếm Tavily: " + e.getMessage()));
        }
    }

    /**
     * Return cached SearchResult for the given query, or null if not cached.
     */
    public SearchResult cachedSearch(SearchQuery q) {
        return cache.get(buildCacheKey(q));
    }

    private String buildCacheKey(SearchQuery q) {
        return "tavily:" + q.q() + "|" + q.num();
    }

    // extractFromUrls removed — not used elsewhere. If needed later, re-add a dedicated extract tool.
}
