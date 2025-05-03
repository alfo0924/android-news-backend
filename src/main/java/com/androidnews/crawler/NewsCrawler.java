package com.androidnews.crawler;

import com.androidnews.model.News;

import java.util.List;

public interface NewsCrawler {

    List<News> crawlNews();

    String getSourceName();
}
