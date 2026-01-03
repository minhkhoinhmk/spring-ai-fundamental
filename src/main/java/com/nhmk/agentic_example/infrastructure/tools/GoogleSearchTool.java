package com.nhmk.agentic_example.infrastructure.tools;

import com.nhmk.agentic_example.application.extract.PageExtractionPort;
import com.nhmk.agentic_example.domain.search.SearchQuery;
import com.nhmk.agentic_example.infrastructure.googlesearch.GoogleSearchAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class GoogleSearchTool {
    private static final Logger logger = LoggerFactory.getLogger(GoogleSearchTool.class);

    private final GoogleSearchAdapter adapter;

    public GoogleSearchTool(@Value("${google.search.api-key}") String apiKey,
                            @Value("${google.search.cx}") String cx,
                            PageExtractionPort pageExtractionPort,
                            GoogleSearchAdapter adapter) {
        this.adapter = adapter;
    }

    @Tool(description = "Tìm kiếm thông tin mới nhất trên Google")
    public String search(@ToolParam(description = "Từ khóa tìm kiếm") String query) {
        logger.info("GoogleSearchTool search: {}", query);
        var q = new SearchQuery(query, null, null, null, 5);
        var res = adapter.search(q);
        if (res.snippets() == null || res.snippets().isEmpty()) return "Không tìm thấy kết quả phù hợp trên Google.";
        StringBuilder sb = new StringBuilder("Tóm tắt từ Google Search:\n");
        for (String s : res.snippets()) sb.append(s).append("\n");
        return sb.toString().trim();
    }
}
