package com.nhmk.agentic_example.infrastructure.webextraction;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsoupPageFetcher implements PageFetcher {
    private static final Logger logger = LoggerFactory.getLogger(JsoupPageFetcher.class);
    private final int timeoutMs;
    private final String userAgent;

    public JsoupPageFetcher(int timeoutMs, String userAgent) {
        this.timeoutMs = timeoutMs;
        this.userAgent = userAgent;
    }

    @Override
    public String fetchHtml(String url) {
        if (url == null || url.isBlank()) return "";
        try {
            Document doc = Jsoup.connect(url)
                    .userAgent(userAgent)
                    .timeout(timeoutMs)
                    .followRedirects(true)
                    .get();
            return doc.html();
        } catch (Exception e) {
            logger.warn("JsoupPageFetcher.fetchHtml failed for {}", url, e);
            return "";
        }
    }
}
