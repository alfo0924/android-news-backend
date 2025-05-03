package com.androidnews.service;

import com.androidnews.crawler.NewsCrawler;
import com.androidnews.model.News;
import com.androidnews.repository.NewsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CrawlerService {

    private final List<NewsCrawler> crawlers;
    private final NewsRepository newsRepository;

    @Transactional
    public void crawlAllSources() {
        log.info("Starting news crawling process");

        for (NewsCrawler crawler : crawlers) {
            try {
                log.info("Crawling from source: {}", crawler.getSourceName());
                List<News> newsList = crawler.crawlNews();

                for (News news : newsList) {
                    Optional<News> existingNews = newsRepository.findBySourceUrl(news.getSourceUrl());
                    if (existingNews.isEmpty()) {
                        newsRepository.save(news);
                        log.info("Saved new article: {}", news.getTitle());
                    } else {
                        log.debug("Article already exists: {}", news.getTitle());
                    }
                }
            } catch (Exception e) {
                log.error("Error crawling from {}: {}", crawler.getSourceName(), e.getMessage(), e);
            }
        }

        log.info("Completed news crawling process");
    }
}
