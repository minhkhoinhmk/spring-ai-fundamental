package com.nhmk.agentic_example.infrastructure.tools;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
@Slf4j
public class NewsApiTool {
    private final WebClient webClient;
    private final String apiKey;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public NewsApiTool(@Value("${newsapi.api-key}") String apiKey) {
        this.apiKey = apiKey;
        this.webClient = WebClient.builder().baseUrl("https://newsapi.org/v2").build();
    }

    @Tool(description = "Lấy tin tức mới nhất từ NewsAPI cho bất kỳ quốc gia nào. country là mã quốc gia (ví dụ: vn, us, gb, jp, kr, fr, de, ...)")
    public String getLatestNews(
            @ToolParam(description = "Mã quốc gia (ví dụ: vn, us, gb, jp, kr, fr, de, ...)") String country,
            @ToolParam(description = "Từ khóa tìm kiếm (có thể để trống để lấy tin mới nhất)") String keyword) {
        log.info("NewsApiTool getLatestNews: country={}, keyword={}", country, keyword);
        try {
            String url = "/top-headlines?country=" + (country == null || country.isBlank() ? "us" : country)
                    + "&pageSize=3&apiKey=" + apiKey;
            if (country != null && country.equalsIgnoreCase("vn")) {
                url += "&language=vi";
            }
            if (keyword != null && !keyword.isBlank()) {
                url += "&q=" + keyword;
            }
            String result = webClient.get().uri(url).retrieve().bodyToMono(String.class).block();
            log.info("NewsAPI raw response: {}", result);
            JsonNode root = MAPPER.readTree(result);
            JsonNode articles = root.path("articles");
            if (articles.isArray() && articles.size() > 0) {
                StringBuilder summary = new StringBuilder();
                for (int i = 0; i < articles.size(); i++) {
                    JsonNode article = articles.get(i);
                    String title = article.path("title").asText("");
                    String description = article.path("description").asText("");
                    String urlToNews = article.path("url").asText("");
                    summary.append(String.format("- %s: %s (Nguồn: %s)\n", title, description, urlToNews));
                }
                return "Tin tức mới nhất từ NewsAPI:\n" + summary.toString().trim();
            } else {
                return "Không tìm thấy tin tức phù hợp.";
            }
        } catch (Exception e) {
            log.error("NewsApiTool error", e);
            return "Không thể lấy tin tức từ NewsAPI: " + e.getMessage();
        }
    }
}
