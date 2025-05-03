package com.androidnews.util;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

@Component
public class HtmlParser {

    public String extractContent(Document doc) {
        // Remove unwanted elements
        doc.select("script, style, iframe, .advertisement, .ads, .related-posts, .comments").remove();

        // Try to find the main content
        Element content = findMainContent(doc);

        if (content != null) {
            // Clean up the content
            content.select("div.social-share, div.author-bio, div.newsletter-signup").remove();
            return content.html();
        }

        // Fallback to body content
        return doc.body().html();
    }

    public String extractMainImage(Document doc) {
        // Try to find the main image using common meta tags
        String ogImage = doc.select("meta[property=og:image]").attr("content");
        if (!ogImage.isEmpty()) {
            return ogImage;
        }

        // Try to find the first large image in the article
        Elements images = doc.select("article img, .post-content img, .entry-content img");
        if (!images.isEmpty()) {
            return images.first().attr("abs:src");
        }

        // Fallback to any image
        images = doc.select("img");
        if (!images.isEmpty()) {
            for (Element img : images) {
                String src = img.attr("abs:src");
                if (!src.isEmpty() && !src.contains("logo") && !src.contains("icon")) {
                    return src;
                }
            }
        }

        return "";
    }

    private Element findMainContent(Document doc) {
        // Try common article content selectors
        Element content = doc.selectFirst("article .content, .post-content, .entry-content, .article-content, main");
        if (content != null) {
            return content;
        }

        // Try to find the largest div with paragraphs
        Elements divs = doc.select("div");
        Element largestDiv = null;
        int maxParagraphs = 0;

        for (Element div : divs) {
            int paragraphCount = div.select("p").size();
            if (paragraphCount > maxParagraphs) {
                maxParagraphs = paragraphCount;
                largestDiv = div;
            }
        }

        return largestDiv;
    }
}
