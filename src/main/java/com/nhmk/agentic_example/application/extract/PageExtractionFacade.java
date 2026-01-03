package com.nhmk.agentic_example.application.extract;

import com.nhmk.agentic_example.domain.extract.DomainFetchResult;
import com.nhmk.agentic_example.infrastructure.webextraction.PageExtractionService;

public class PageExtractionFacade implements PageExtractionPort {
    private final PageExtractionService service;

    public PageExtractionFacade(PageExtractionService service) {
        this.service = service;
    }

    @Override
    public DomainFetchResult extractForSummary(String url) {
        return service.extractForSummary(url);
    }
}
