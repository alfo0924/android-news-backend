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
public class TechNewsCrawler implements NewsCrawler {

    private static final String SOURCE_NAME = "Android Authority";
    private static final String BASE_URL = "https://www.androidauthority.com/news/";

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
            Elements articles = doc.select("article");

            for (Element article : articles) {
                try {
                    Element titleElement = article.selectFirst("h3 a");
                    if (titleElement == null) continue;

                    String title = titleElement.text();
                    String link = titleElement.attr("abs:href");

                    // Get article details
                    Document articleDoc = Jsoup.connect(link).get();
                    String content = htmlParser.extractContent(articleDoc);
                    String summary = articleDoc.select("meta[name=description]").attr("content");
                    String imageUrl = articleDoc.select("meta[property=og:image]").attr("content");
                    String author = articleDoc.select(".aa_author_name").text();

                    // Determine category based on URL or content
                    Category category = determineCategory(link, title + " " + summary, categories);

                    // Extract tags
                    Set<String> tags = extractTags(title + " " + summary + " " + link);

                    News news = new News();
                    news.setTitle(title);
                    news.setContent(content);
                    news.setSummary(summary.length() > 500 ? summary.substring(0, 500) : summary);
                    news.setImageUrl(imageUrl);
                    news.setPublishDate(LocalDateTime.now()); // Should extract actual date
                    news.setSourceUrl(link);
                    news.setSource(source);
                    news.setCategory(category);
                    news.setTags(tags);
                    news.setAuthor(author);

                    newsList.add(news);
                } catch (Exception e) {
                    log.error("Error processing article: {}", e.getMessage(), e);
                }
            }
        } catch (IOException e) {
            log.error("Error crawling Android Authority: {}", e.getMessage(), e);
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
                    newSource.setUrl("https://www.androidauthority.com");
                    newSource.setLogoUrl("https://www.androidauthority.com/favicon.ico");
                    newSource.setDescription("Android Authority is the largest publication dedicated to Android OS and the tech ecosystem around it");
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

    private Category determineCategory(String url, String content, Map<String, Category> categories) {
        // Try to determine category from URL first
        url = url.toLowerCase();
        if (url.contains("/phones/")) {
            return categories.getOrDefault("phones", getDefaultCategory(categories));
        } else if (url.contains("/tablet/") || url.contains("/tablets/")) {
            return categories.getOrDefault("tablets", getDefaultCategory(categories));
        } else if (url.contains("/wearables/")) {
            return categories.getOrDefault("wearables", getDefaultCategory(categories));
        } else if (url.contains("/apps/")) {
            return categories.getOrDefault("apps", getDefaultCategory(categories));
        } else if (url.contains("/android-development/")) {
            return categories.getOrDefault("development", getDefaultCategory(categories));
        } else if (url.contains("/google/")) {
            return categories.getOrDefault("google", getDefaultCategory(categories));
        }

        // Fall back to content analysis
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
