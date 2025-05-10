package com.graduation.GMS.Repositories;

import com.graduation.GMS.Models.Article;
import com.graduation.GMS.Models.Enums.Wiki;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Integer> {
    List<Article> findByWikiType(Wiki wikiType);

    Optional<Article> findByTitle(String title);
}
