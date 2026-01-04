package com.nhmk.agentic_example.infrastructure.webextraction;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BoilerpipeReadabilityExtractor implements ReadabilityExtractor {

    @Override
    public String extract(String html, String url) {
        if (html == null || html.isBlank()) return "";
        try {
            Document doc = Jsoup.parse(html);
            return HtmlMainTextExtractor.extractMainText(doc);
        } catch (Exception e) {
            log.error("Readability extraction failed for {}", url, e);
            return "";
        }
    }
}
