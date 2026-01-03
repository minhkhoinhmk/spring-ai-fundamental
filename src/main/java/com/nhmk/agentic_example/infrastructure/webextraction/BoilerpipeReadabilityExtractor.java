package com.nhmk.agentic_example.infrastructure.webextraction;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BoilerpipeReadabilityExtractor implements ReadabilityExtractor {
    private static final Logger logger = LoggerFactory.getLogger(BoilerpipeReadabilityExtractor.class);

    @Override
    public String extract(String html, String url) {
        if (html == null || html.isBlank()) return "";
        try {
            Document doc = Jsoup.parse(html);
            return HtmlMainTextExtractor.extractMainText(doc);
        } catch (Exception e) {
            logger.warn("Readability extraction failed for {}", url, e);
            return "";
        }
    }
}
