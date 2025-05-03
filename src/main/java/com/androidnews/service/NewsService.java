package com.androidnews.service;

import com.androidnews.dto.NewsDTO;
import com.androidnews.model.Category;
import com.androidnews.model.News;
import com.androidnews.repository.CategoryRepository;
import com.androidnews.repository.NewsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NewsService {

    private final NewsRepository newsRepository;
    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public Page<NewsDTO> getAllNews(Pageable pageable) {
        return newsRepository.findAllByOrderByPublishDateDesc(pageable)
                .map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public Page<NewsDTO> getNewsByCategory(String categorySlug, Pageable pageable) {
        Optional<Category> categoryOpt = categoryRepository.findBySlug(categorySlug);
        if (categoryOpt.isEmpty()) {
            throw new RuntimeException("Category not found: " + categorySlug);
        }

        return newsRepository.findByCategoryOrderByPublishDateDesc(categoryOpt.get(), pageable)
                .map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public NewsDTO getNewsById(Long id) {
        News news = newsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("News not found with id: " + id));

        NewsDTO newsDTO = convertToDTO(news);

        // Get related news
        List<NewsDTO> relatedNews = newsRepository.findRelatedNews(id)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        // Set related news (would need to add this field to NewsDTO)
        // newsDTO.setRelatedNews(relatedNews);

        return newsDTO;
    }

    @Transactional(readOnly = true)
    public Page<NewsDTO> searchNews(String query, Pageable pageable) {
        return newsRepository.search(query, pageable)
                .map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public Page<NewsDTO> getNewsByTag(String tag, Pageable pageable) {
        return newsRepository.findByTag(tag, pageable)
                .map(this::convertToDTO);
    }

    private NewsDTO convertToDTO(News news) {
        NewsDTO dto = new NewsDTO();
        dto.setId(news.getId());
        dto.setTitle(news.getTitle());
        dto.setContent(news.getContent());
        dto.setSummary(news.getSummary());
        dto.setImageUrl(news.getImageUrl());
        dto.setImageCredit(news.getImageCredit());
        dto.setPublishDate(news.getPublishDate());
        dto.setSourceUrl(news.getSourceUrl());
        dto.setSource(news.getSource().getName());
        dto.setSourceLogoUrl(news.getSource().getLogoUrl());
        dto.setCategory(news.getCategory().getName());
        dto.setCategorySlug(news.getCategory().getSlug());
        dto.setTags(news.getTags());
        dto.setAuthor(news.getAuthor());
        dto.setAuthorBio(news.getAuthorBio());
        dto.setAuthorAvatar(news.getAuthorAvatar());
        return dto;
    }
}
