package com.androidnews.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewsDTO {
    private Long id;
    private String title;
    private String content;
    private String summary;
    private String imageUrl;
    private String imageCredit;
    private LocalDateTime publishDate;
    private String sourceUrl;
    private String source;
    private String sourceLogoUrl;
    private String category;
    private String categorySlug;
    private Set<String> tags = new HashSet<>();
    private String author;
    private String authorBio;
    private String authorAvatar;
}
