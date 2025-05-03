package com.androidnews.scheduler;

import com.androidnews.service.CrawlerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CrawlerScheduler {

    private final CrawlerService crawlerService;

    // Run every hour
    @Scheduled(cron = "0 0 * * * ?")
    public void scheduledCrawling() {
        log.info("Starting scheduled news crawling");
        crawlerService.crawlAllSources();
    }
}
