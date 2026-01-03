package com.nhmk.agentic_example.domain.search;

import java.util.List;

public final class SearchResult {
    private final String query;
    private final List<String> snippets;

    public SearchResult(String query, List<String> snippets) {
        this.query = query;
        this.snippets = snippets;
    }

    public String query() { return query; }
    public List<String> snippets() { return snippets; }
}
