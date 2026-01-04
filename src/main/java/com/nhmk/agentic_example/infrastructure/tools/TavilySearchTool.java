package com.nhmk.agentic_example.infrastructure.tools;

import com.nhmk.agentic_example.domain.search.SearchQuery;
import com.nhmk.agentic_example.infrastructure.tavily.TavilySearchAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/**
 * Tavily Search Tool - optimized search for AI agents.
 * Tavily provides pre-processed, LLM-ready search results.
 */
@Component
@Slf4j
public class TavilySearchTool {

    private final TavilySearchAdapter adapter;

    public TavilySearchTool(TavilySearchAdapter adapter) {
        this.adapter = adapter;
    }

    @Tool(description = "Tìm kiếm thông tin mới nhất trên internet bằng Tavily (tối ưu cho AI)")
    public String search(@ToolParam(description = "Từ khóa tìm kiếm") String query) {
        var q = new SearchQuery(query, null, null, null, 5);
        // Short-circuit: return cached result if available to avoid repeated API calls and logs
        var cached = adapter.cachedSearch(q);
        if (cached != null) {
            var resCached = cached;
            if (resCached.snippets() == null || resCached.snippets().isEmpty()) {
                return "Không tìm thấy kết quả phù hợp.";
            }
            StringBuilder sbCached = new StringBuilder("Kết quả tìm kiếm từ Tavily (cache):\n");
            for (String s : resCached.snippets()) {
                sbCached.append(s).append("\n\n");
            }
            return sbCached.toString().trim();
        }

        log.info("TavilySearchTool search: {}", query);
        var res = adapter.search(q);
        if (res.snippets() == null || res.snippets().isEmpty()) {
            return "Không tìm thấy kết quả phù hợp.";
        }
        StringBuilder sb = new StringBuilder("Kết quả tìm kiếm từ Tavily:\n");
        for (String s : res.snippets()) {
            sb.append(s).append("\n\n");
        }
        return sb.toString().trim();
    }
}
