package com.nhmk.agentic_example.infrastructure.webextraction;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JsoupPageFetcher implements PageFetcher {
    private final int timeoutMs;
    private final String userAgent;

    public JsoupPageFetcher(int timeoutMs, String userAgent) {
        this.timeoutMs = timeoutMs;
        this.userAgent = userAgent;
    }

    @Override
    public FetchResult fetch(String url) {
        if (url == null || url.isBlank()) return new FetchResult("empty-url");
        try {
            Document doc = Jsoup.connect(url)
                    .userAgent(userAgent)
                    .timeout(timeoutMs)
                    .followRedirects(true)
                    .ignoreContentType(true)
                    .get();
            String html = doc.html();
            return new FetchResult(200, "text/html; charset=utf-8", html.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        } catch (Exception e) {
            log.error("JsoupPageFetcher.fetch failed for {}", url, e);
            return new FetchResult("fetch-error");
        }
    }
}
