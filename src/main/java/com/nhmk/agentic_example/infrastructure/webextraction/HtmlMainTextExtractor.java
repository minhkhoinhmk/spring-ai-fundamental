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
     * Strategy: article → main → first long paragraph → meta descriptions.
     */
    public static String extractMainText(Document doc) {
        if (doc == null) {
            return "";
        }

        Elements articleEl = doc.select("article");
        String text = null;
        if (!articleEl.isEmpty()) {
            text = articleEl.text();
        }
        if (isBlank(text)) {
            Elements main = doc.select("main");
            if (!main.isEmpty()) {
                text = main.text();
            }
        }
        if (isBlank(text)) {
            Elements ps = doc.select("p");
            for (var p : ps) {
                String pt = p.text();
                if (!isBlank(pt) && pt.length() > 40) {
                    text = pt;
                    break;
                }
            }
        }
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
