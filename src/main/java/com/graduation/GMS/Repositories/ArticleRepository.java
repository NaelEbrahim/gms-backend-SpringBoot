package com.graduation.GMS.Repositories;

import com.graduation.GMS.Models.Article;
import com.graduation.GMS.Models.Enums.Wiki;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Integer> {
    Optional<Article> findByTitle(String title);

    @Query("""
            SELECT a FROM Article a
            WHERE (:wikiType IS NULL OR a.wikiType = :wikiType)
            """)
    Page<Article> findByWikiType(
            @Param("wikiType") Wiki wikiType,
            Pageable pageable
    );

}
