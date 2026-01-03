package com.nhmk.agentic_example.application.extract;

import com.nhmk.agentic_example.domain.extract.DomainFetchResult;

public interface PageExtractionPort {
    DomainFetchResult extractForSummary(String url);
}
