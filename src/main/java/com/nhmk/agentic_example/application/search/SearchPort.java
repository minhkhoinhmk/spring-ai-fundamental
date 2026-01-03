package com.nhmk.agentic_example.application.search;

import com.nhmk.agentic_example.domain.search.SearchQuery;
import com.nhmk.agentic_example.domain.search.SearchResult;

public interface SearchPort {
    SearchResult search(SearchQuery query);
}
