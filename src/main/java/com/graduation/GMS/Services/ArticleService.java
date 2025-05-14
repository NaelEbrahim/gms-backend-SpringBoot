package com.graduation.GMS.Services;

import com.graduation.GMS.DTO.Request.ArticleRequest;
import com.graduation.GMS.DTO.Response.ArticleResponse;
import com.graduation.GMS.DTO.Response.UserResponse;
import com.graduation.GMS.Models.Article;
import com.graduation.GMS.Models.Enums.Wiki;
import com.graduation.GMS.Models.User;
import com.graduation.GMS.Repositories.ArticleRepository;
import com.graduation.GMS.Repositories.UserRepository;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.graduation.GMS.DTO.Response.UserResponse.mapToUserResponse;

@Service
@AllArgsConstructor
public class ArticleService {

    private ArticleRepository articleRepository;

    private UserRepository userRepository;

    @Transactional
    public ResponseEntity<?> createArticle(@Valid ArticleRequest request) {

        // Check if article title already exists
        Optional<Article> existingArticle = articleRepository.findByTitle(request.getTitle());
        if (existingArticle.isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Article title already exists"));
        }

        Optional<User> admin =userRepository.findById(request.getAdminId());
        Article article = new Article();
        article.setCreatedAt(LocalDateTime.now());
        article.setTitle(request.getTitle());
        article.setContent(request.getContent());
        article.setCategory(request.getCategory());
        article.setWikiType(request.getWikiType());
        article.setAdmin(admin.get());
        article.setLastModifiedAt(LocalDateTime.now());
        articleRepository.save(article);
        // Return the response with the saved article details
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Article created successfully"));
    }
    @Transactional
    public ResponseEntity<?> updateArticle(Integer id, @Valid ArticleRequest request) {
        Optional<Article> optionalArticle = articleRepository.findById(id);
        if (optionalArticle.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Article not found"));
        }

        Article existingArticle = optionalArticle.get();

        Optional<User> admin =userRepository.findById(request.getAdminId());
        if (admin.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Admin Not Found"));
        }

        if (!existingArticle.getAdmin().getId().equals(request.getAdminId())) {
            existingArticle.setAdmin(admin.get());
        }

        if (!existingArticle.getTitle().equals(request.getTitle())&&!request.getTitle().isEmpty()) {
            existingArticle.setTitle(request.getTitle());
        }
        if (!existingArticle.getContent().equals(request.getTitle())&&!request.getContent().isEmpty()) {
            existingArticle.setContent(request.getContent());
        }
        if (!existingArticle.getCategory().equals(request.getCategory())&&!request.getCategory().isEmpty()) {
            existingArticle.setCategory(request.getCategory());
        }
        if (!existingArticle.getWikiType().equals(request.getWikiType())&& request.getWikiType() != null) {
            existingArticle.setWikiType(request.getWikiType());
        }
        existingArticle.setLastModifiedAt(LocalDateTime.now());
        articleRepository.save(existingArticle);

        return ResponseEntity.status(HttpStatus.OK)
                .body(Map.of("message", "Article updated successfully"));
    }

    public ResponseEntity<?> deleteArticle(Integer id) {
        if (!articleRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Article not found"));
        }

        articleRepository.deleteById(id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(Map.of("message", "Article deleted successfully"));
    }

    public ResponseEntity<?> getArticleById(Integer id) {
        Optional<Article> articleOptional = articleRepository.findById(id);
        if (articleOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Article Not found"));
        }

        Article articleEntity = articleOptional.get();
        UserResponse adminResponse = mapToUserResponse(articleEntity.getAdmin());

        ArticleResponse responseDto = new ArticleResponse(
                articleEntity.getId(),
                adminResponse,
                articleEntity.getTitle(),
                articleEntity.getContent(),
                articleEntity.getCategory(),
                articleEntity.getWikiType(),
                articleEntity.getCreatedAt(),
                articleEntity.getLastModifiedAt()
        );

        return ResponseEntity.status(HttpStatus.OK)
                .body(responseDto);
    }


    public ResponseEntity<?> getAllArticles() {
        List<Article> articles = articleRepository.findAll();

        if (articles.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "No Articles found"));
        }

        List<ArticleResponse> articleResponses = articles.stream()
                .map(article -> new ArticleResponse(
                        article.getId(),
                        mapToUserResponse(article.getAdmin()),
                        article.getTitle(),
                        article.getContent(),
                        article.getCategory(),
                        article.getWikiType(),
                        article.getCreatedAt(),
                        article.getLastModifiedAt()
                ))
                .toList();

        return ResponseEntity.status(HttpStatus.OK).body(articleResponses);
    }

    public ResponseEntity<?> getAllHealthArticles() {
        List<Article> articles = articleRepository.findByWikiType(Wiki.Health);

        if (articles.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "No Articles found"));
        }

        List<ArticleResponse> articleResponses = articles.stream()
                .map(c -> new ArticleResponse(
                        c.getId(),
                        mapToUserResponse(c.getAdmin()),
                        c.getTitle(),
                        c.getContent(),
                        c.getCategory(),
                        c.getWikiType(),
                        c.getCreatedAt(),
                        c.getLastModifiedAt()
                ))
                .toList();

        return ResponseEntity.status(HttpStatus.OK)
                .body(articleResponses);
    }

    public ResponseEntity<?> getAllSportArticles() {
        List<Article> articles = articleRepository.findByWikiType(Wiki.Sport);

        if (articles.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "No Articles found"));
        }

        List<ArticleResponse> articleResponses = articles.stream()
                .map(c -> new ArticleResponse(
                        c.getId(),
                        mapToUserResponse(c.getAdmin()),
                        c.getTitle(),
                        c.getContent(),
                        c.getCategory(),
                        c.getWikiType(),
                        c.getCreatedAt(),
                        c.getLastModifiedAt()
                ))
                .toList();

        return ResponseEntity.status(HttpStatus.OK)
                .body(articleResponses);
    }

}
