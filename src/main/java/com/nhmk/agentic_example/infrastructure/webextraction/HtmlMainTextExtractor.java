package com.nhmk.agentic_example.infrastructure.webextraction;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 * Reusable heuristics for extracting main readable text from an HTML document.
 */
public final class HtmlMainTextExtractor {
    private HtmlMainTextExtractor() {
    }

    /**
     * Attempts to extract the most relevant text content from the provided document.
     * Strategy: article → main → all paragraphs (aggregated) → meta descriptions.
     */
    public static String extractMainText(Document doc) {
        if (doc == null) {
            return "";
        }

        // Try article tag first
        Elements articleEl = doc.select("article");
        String text = null;
        if (!articleEl.isEmpty()) {
            text = articleEl.text();
        }
        // Try main tag
        if (isBlank(text)) {
            Elements main = doc.select("main");
            if (!main.isEmpty()) {
                text = main.text();
            }
        }
        // Aggregate all substantial paragraphs (not just first one)
        if (isBlank(text)) {
            Elements ps = doc.select("p");
            StringBuilder sb = new StringBuilder();
            for (var p : ps) {
                String pt = p.text();
                if (!isBlank(pt) && pt.length() > 30) {
                    sb.append(pt).append(" ");
                }
            }
            text = sb.toString().trim();
        }
        // Fallback to meta description
        if (isBlank(text)) {
            String desc = doc.select("meta[name=description]").attr("content");
            if (isBlank(desc)) {
                desc = doc.select("meta[property=og:description]").attr("content");
            }
            text = desc == null ? "" : desc;
        }
        return text == null ? "" : text.trim();
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
