package com.nhmk.agentic_example.infrastructure.config;

import com.nhmk.agentic_example.infrastructure.webextraction.HttpPageFetcher;
import com.nhmk.agentic_example.infrastructure.webextraction.PageFetcher;
import com.nhmk.agentic_example.infrastructure.webextraction.BoilerpipeReadabilityExtractor;
import com.nhmk.agentic_example.infrastructure.webextraction.PageExtractionService;
import com.nhmk.agentic_example.application.extract.PageExtractionFacade;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PageFetcherConfig {

    @Value("${app.extract.fetch-timeout-ms:10000}")
    private int fetchTimeoutMs;

    @Value("${app.extract.user-agent:agentic-example/1.0}")
    private String fetchUserAgent;

    @Value("${app.extract.max-download-bytes:5242880}")
    private int maxDownloadBytes;

    @Value("${app.extract.max-retries:2}")
    private int maxRetries;

    @Value("${app.extract.max-chars:2000}")
    private int maxExtractChars;

    @Bean
    public PageFetcher pageFetcher() {
        return new HttpPageFetcher(fetchTimeoutMs, fetchUserAgent, maxDownloadBytes, maxRetries);
    }

    @Bean
    public BoilerpipeReadabilityExtractor boilerpipeReadabilityExtractor() {
        return new BoilerpipeReadabilityExtractor();
    }

    @Bean
    public PageExtractionService pageExtractionService(PageFetcher fetcher, BoilerpipeReadabilityExtractor extractor) {
        return new PageExtractionService(fetcher, extractor, maxExtractChars);
    }

    @Bean
    public PageExtractionFacade pageExtractionFacade(PageExtractionService service) {
        return new PageExtractionFacade(service);
    }
}
