package com.androidnews.util;

import com.androidnews.model.Category;
import com.androidnews.model.News;
import com.androidnews.model.Source;
import com.androidnews.repository.CategoryRepository;
import com.androidnews.repository.NewsRepository;
import com.androidnews.repository.SourceRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final SourceRepository sourceRepository;
    private final NewsRepository newsRepository;

    public DataInitializer(CategoryRepository categoryRepository, SourceRepository sourceRepository, NewsRepository newsRepository) {
        this.categoryRepository = categoryRepository;
        this.sourceRepository = sourceRepository;
        this.newsRepository = newsRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // 初始化分類
        initCategories();

        // 初始化來源
        initSources();

        // 抓取新聞
        crawlGoogleNews();
    }

    private void initCategories() {
        if (categoryRepository.count() == 0) {
            List<Category> categories = Arrays.asList(
                    new Category(null, "手機", "phones", "Android手機相關新聞", null),
                    new Category(null, "平板", "tablets", "Android平板相關新聞", null),
                    new Category(null, "穿戴裝置", "wearables", "Android穿戴裝置相關新聞", null),
                    new Category(null, "應用程式", "apps", "Android應用程式相關新聞", null),
                    new Category(null, "作業系統", "os", "Android作業系統相關新聞", null),
                    new Category(null, "開發", "development", "Android開發相關新聞", null),
                    new Category(null, "Google", "google", "Google相關新聞", null),
                    new Category(null, "硬體", "hardware", "Android硬體相關新聞", null)
            );
            categoryRepository.saveAll(categories);
        }
    }

    private void initSources() {
        if (sourceRepository.count() == 0) {
            List<Source> sources = Arrays.asList(
                    new Source(null, "Google News", "https://news.google.com", "https://www.google.com/favicon.ico", "Google News聚合最新Android新聞", null),
                    new Source(null, "Android Authority", "https://www.androidauthority.com", "https://www.androidauthority.com/favicon.ico", "Android Authority是最大的Android相關新聞網站", null),
                    new Source(null, "Android Police", "https://www.androidpolice.com", "https://www.androidpolice.com/favicon.ico", "Android Police提供最新的Android新聞和評論", null)
            );
            sourceRepository.saveAll(sources);
        }
    }

    private void crawlGoogleNews() {
        try {
            if (newsRepository.count() == 0) {
                // 從Google News抓取Android相關新聞
                Source source = sourceRepository.findByName("Google News").orElseThrow();

                // 使用Google News RSS訂閱源
                Document doc = Jsoup.connect("https://news.google.com/rss/search?q=android&hl=zh-TW&gl=TW&ceid=TW:zh-Hant").get();
                Elements items = doc.select("item");

                for (int i = 0; i < Math.min(20, items.size()); i++) {
                    Element item = items.get(i);

                    String title = item.select("title").text();
                    String link = item.select("link").text();
                    String pubDate = item.select("pubDate").text();
                    String description = item.select("description").text();
                    String sourceName = item.select("source").text();

                    // 根據標題和描述判斷分類
                    Category category = determineCategory(title + " " + description);

                    News news = new News();
                    news.setTitle(title);
                    news.setSummary(description.length() > 200 ? description.substring(0, 200) + "..." : description);
                    news.setContent("<p>" + description + "</p><p>閱讀更多: <a href='" + link + "' target='_blank'>" + link + "</a></p>");
                    news.setImageUrl("https://via.placeholder.com/600x400?text=Android+News"); // 預設圖片
                    news.setPublishDate(LocalDateTime.now().minusDays((long) (Math.random() * 3)));
                    news.setSourceUrl(link);
                    news.setSource(source);
                    news.setCategory(category);
                    news.setTags(extractTags(title + " " + description));
                    news.setAuthor(sourceName.isEmpty() ? "Google News" : sourceName);

                    newsRepository.save(news);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Category determineCategory(String content) {
        content = content.toLowerCase();

        if (content.contains("手機") || content.contains("smartphone") || content.contains("pixel") || content.contains("galaxy")) {
            return categoryRepository.findBySlug("phones").orElseThrow();
        } else if (content.contains("平板") || content.contains("tablet") || content.contains("ipad")) {
            return categoryRepository.findBySlug("tablets").orElseThrow();
        } else if (content.contains("穿戴") || content.contains("watch") || content.contains("wearable")) {
            return categoryRepository.findBySlug("wearables").orElseThrow();
        } else if (content.contains("app") || content.contains("應用")) {
            return categoryRepository.findBySlug("apps").orElseThrow();
        } else if (content.contains("android 13") || content.contains("android 14") || content.contains("系統") || content.contains("更新")) {
            return categoryRepository.findBySlug("os").orElseThrow();
        } else if (content.contains("開發") || content.contains("develop") || content.contains("程式")) {
            return categoryRepository.findBySlug("development").orElseThrow();
        } else if (content.contains("google")) {
            return categoryRepository.findBySlug("google").orElseThrow();
        } else {
            return categoryRepository.findBySlug("phones").orElseThrow(); // 預設分類
        }
    }

    private HashSet<String> extractTags(String content) {
        HashSet<String> tags = new HashSet<>();
        content = content.toLowerCase();

        if (content.contains("android 13")) tags.add("Android 13");
        if (content.contains("android 14")) tags.add("Android 14");
        if (content.contains("pixel")) tags.add("Pixel");
        if (content.contains("samsung") || content.contains("三星")) tags.add("Samsung");
        if (content.contains("galaxy")) tags.add("Galaxy");
        if (content.contains("google")) tags.add("Google");
        if (content.contains("app") || content.contains("應用")) tags.add("App");
        if (content.contains("更新") || content.contains("update")) tags.add("更新");

        // 確保至少有一個標籤
        if (tags.isEmpty()) {
            tags.add("Android");
        }

        return tags;
    }
}
