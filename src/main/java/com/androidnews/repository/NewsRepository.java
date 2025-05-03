package com.androidnews.repository;

import com.androidnews.model.Category;
import com.androidnews.model.News;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface NewsRepository extends JpaRepository<News, Long> {

    Page<News> findAllByOrderByPublishDateDesc(Pageable pageable);

    Page<News> findByCategoryOrderByPublishDateDesc(Category category, Pageable pageable);

    Optional<News> findBySourceUrl(String sourceUrl);

    @Query("SELECT n FROM News n WHERE n.title LIKE %:query% OR n.content LIKE %:query% OR n.summary LIKE %:query%")
    Page<News> search(@Param("query") String query, Pageable pageable);

    @Query("SELECT n FROM News n JOIN n.tags t WHERE t = :tag")
    Page<News> findByTag(@Param("tag") String tag, Pageable pageable);

    List<News> findByPublishDateAfter(LocalDateTime date);

    @Query(value = "SELECT n.* FROM news n JOIN news_tags t ON n.id = t.news_id " +
            "WHERE t.tag IN (SELECT t2.tag FROM news_tags t2 WHERE t2.news_id = :newsId) " +
            "AND n.id != :newsId GROUP BY n.id ORDER BY COUNT(t.tag) DESC LIMIT 5", nativeQuery = true)
    List<News> findRelatedNews(@Param("newsId") Long newsId);
}
