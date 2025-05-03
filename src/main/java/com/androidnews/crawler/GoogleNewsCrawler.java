package com.androidnews.crawler;

import com.androidnews.model.Category;
import com.androidnews.model.News;
import com.androidnews.model.Source;
import com.androidnews.repository.CategoryRepository;
import com.androidnews.repository.SourceRepository;
import com.androidnews.util.HtmlParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class GoogleNewsCrawler implements NewsCrawler {

    private static final String SOURCE_NAME = "Google News";
    private static final String BASE_URL = "https://news.google.com/rss/search?q=android&hl=en-US&gl=US&ceid=US:en";

    private final SourceRepository sourceRepository;
    private final CategoryRepository categoryRepository;
    private final HtmlParser htmlParser;

    @Override
    public List<News> crawlNews() {
        List<News> newsList = new ArrayList<>();

        try {
            Source source = getOrCreateSource();
            Map<String, Category> categories = getCategories();

            Document doc = Jsoup.connect(BASE_URL).get();
            Elements items = doc.select("item");

            for (Element item : items) {
                try {
                    String title = item.select("title").text();
                    String link = item.select("link").text();
                    String pubDate = item.select("pubDate").text();
                    String description = item.select("description").text();

                    // Parse the article to get full content
                    Document articleDoc = Jsoup.connect(link).get();
                    String content = htmlParser.extractContent(articleDoc);
                    String imageUrl = htmlParser.extractMainImage(articleDoc);

                    // Determine category based on content analysis
                    Category category = determineCategory(title + " " + description, categories);

                    // Extract tags
                    Set<String> tags = extractTags(title + " " + description);

                    News news = new News();
                    news.setTitle(title);
                    news.setContent(content);
                    news.setSummary(description.length() > 500 ? description.substring(0, 500) : description);
                    news.setImageUrl(imageUrl);
                    news.setPublishDate(LocalDateTime.now()); // Should parse pubDate properly
                    news.setSourceUrl(link);
                    news.setSource(source);
                    news.setCategory(category);
                    news.setTags(tags);

                    newsList.add(news);
                } catch (Exception e) {
                    log.error("Error processing news item: {}", e.getMessage(), e);
                }
            }
        } catch (IOException e) {
            log.error("Error crawling Google News: {}", e.getMessage(), e);
        }

        return newsList;
    }

    @Override
    public String getSourceName() {
        return SOURCE_NAME;
    }

    private Source getOrCreateSource() {
        return sourceRepository.findByName(SOURCE_NAME)
                .orElseGet(() -> {
                    Source newSource = new Source();
                    newSource.setName(SOURCE_NAME);
                    newSource.setUrl("https://news.google.com");
                    newSource.setLogoUrl("https://www.google.com/favicon.ico");
                    newSource.setDescription("Google News aggregates headlines from news sources worldwide");
                    return sourceRepository.save(newSource);
                });
    }

    private Map<String, Category> getCategories() {
        Map<String, Category> categoryMap = new HashMap<>();
        categoryRepository.findAll().forEach(category ->
                categoryMap.put(category.getName().toLowerCase(), category)
        );
        return categoryMap;
    }

    private Category determineCategory(String content, Map<String, Category> categories) {
        // Simple category detection based on keyword matching
        content = content.toLowerCase();

        if (content.contains("phone") || content.contains("smartphone") || content.contains("pixel") || content.contains("galaxy")) {
            return categories.getOrDefault("phones", getDefaultCategory(categories));
        } else if (content.contains("tablet") || content.contains("ipad")) {
            return categories.getOrDefault("tablets", getDefaultCategory(categories));
        } else if (content.contains("wear") || content.contains("watch") || content.contains("wearable")) {
            return categories.getOrDefault("wearables", getDefaultCategory(categories));
        } else if (content.contains("app") || content.contains("application")) {
            return categories.getOrDefault("apps", getDefaultCategory(categories));
        } else if (content.contains("android 13") || content.contains("android 14") || content.contains("os") || content.contains("update")) {
            return categories.getOrDefault("os", getDefaultCategory(categories));
        } else if (content.contains("develop") || content.contains("code") || content.contains("programming")) {
            return categories.getOrDefault("development", getDefaultCategory(categories));
        } else if (content.contains("google")) {
            return categories.getOrDefault("google", getDefaultCategory(categories));
        } else {
            return getDefaultCategory(categories);
        }
    }

    private Category getDefaultCategory(Map<String, Category> categories) {
        return categories.values().iterator().next(); // Get first available category
    }

    private Set<String> extractTags(String content) {
        Set<String> tags = new HashSet<>();
        content = content.toLowerCase();

        // Extract common Android-related tags
        if (content.contains("android 13")) tags.add("Android 13");
        if (content.contains("android 14")) tags.add("Android 14");
        if (content.contains("pixel")) tags.add("Pixel");
        if (content.contains("samsung")) tags.add("Samsung");
        if (content.contains("galaxy")) tags.add("Galaxy");
        if (content.contains("google")) tags.add("Google");
        if (content.contains("app")) tags.add("App");
        if (content.contains("update")) tags.add("Update");

        return tags;
    }
}
