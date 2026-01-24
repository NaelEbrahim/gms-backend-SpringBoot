package com.graduation.GMS.Services;

import com.graduation.GMS.DTO.Request.ArticleRequest;
import com.graduation.GMS.DTO.Response.ArticleResponse;
import com.graduation.GMS.DTO.Response.UserResponse;
import com.graduation.GMS.Models.Article;
import com.graduation.GMS.Models.Enums.Roles;
import com.graduation.GMS.Models.Enums.Wiki;
import com.graduation.GMS.Models.Notification;
import com.graduation.GMS.Models.User;
import com.graduation.GMS.Repositories.ArticleRepository;
import com.graduation.GMS.Handlers.HandleCurrentUserSession;
import com.graduation.GMS.Repositories.NotificationRepository;
import com.graduation.GMS.Repositories.UserRepository;
import com.graduation.GMS.Services.GeneralServices.NotificationService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.graduation.GMS.DTO.Response.UserResponse.mapToUserResponse;

@Service
@AllArgsConstructor
public class ArticleService {

    private ArticleRepository articleRepository;

    private NotificationService notificationService;

    private NotificationRepository notificationRepository;

    private UserRepository userRepository;

    @Transactional
    @PreAuthorize("hasAnyAuthority('Admin')")
    public ResponseEntity<?> createArticle(ArticleRequest request) {

        // Check if article title already exists
        Optional<Article> existingArticle = articleRepository.findByTitle(request.getTitle());
        if (existingArticle.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "Article title already exists"));
        }

        Article article = new Article();
        article.setCreatedAt(LocalDateTime.now());
        article.setTitle(request.getTitle());
        article.setContent(request.getContent());
        article.setWikiType(request.getWikiType());
        article.setAdmin(HandleCurrentUserSession.getCurrentUser());
        article.setLastModifiedAt(LocalDateTime.now());
        articleRepository.save(article);

        // Create and send notification
        Notification notification = new Notification();
        notification.setTitle("A new article has been published: " + article.getTitle());
        notification.setContent("article title : " + article.getTitle());
        notification.setCreatedAt(LocalDateTime.now());
        // Persist notification first
        notification = notificationRepository.save(notification); // Save and get managed instance
        List<User> usersWithUserRole = userRepository.findAllByRoleName(Roles.User);


        notificationService.sendNotificationToUsers(
                usersWithUserRole,
                notification
        );
        // Return the response with the saved article details
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Article created successfully"));
    }

    @Transactional
    @PreAuthorize("hasAnyAuthority('Admin')")
    public ResponseEntity<?> updateArticle(Integer id, ArticleRequest request) {
        Optional<Article> optionalArticle = articleRepository.findById(id);
        if (optionalArticle.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Article not found"));
        }

        Article existingArticle = optionalArticle.get();

        if (!existingArticle.getTitle().equals(request.getTitle()) && !request.getTitle().isEmpty()) {
            existingArticle.setTitle(request.getTitle());
        }
        if (!existingArticle.getContent().equals(request.getTitle()) && !request.getContent().isEmpty()) {
            existingArticle.setContent(request.getContent());
        }
        if (request.getWikiType() != null && !existingArticle.getWikiType().equals(request.getWikiType())) {
            existingArticle.setWikiType(request.getWikiType());
        }
        existingArticle.setLastModifiedAt(LocalDateTime.now());
        articleRepository.save(existingArticle);

        return ResponseEntity.status(HttpStatus.OK)
                .body(Map.of("message", "Article updated successfully"));
    }

    @PreAuthorize("hasAnyAuthority('Admin')")
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
                articleEntity.getWikiType(),
                articleEntity.getCreatedAt(),
                articleEntity.getLastModifiedAt()
        );

        return ResponseEntity.status(HttpStatus.OK)
                .body(responseDto);
    }


    public ResponseEntity<?> searchArticles(Wiki wikiParam, String keyword, Pageable pageable) {

        Page<Article> articlesPage = articleRepository.searchArticlesByWikiAndKeyword(wikiParam, keyword, pageable);

        if (articlesPage.isEmpty()) {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(Map.of("message", "No Articles found"));
        }

        List<ArticleResponse> articleResponses = articlesPage.stream()
                .map(article -> new ArticleResponse(
                        article.getId(),
                        mapToUserResponse(article.getAdmin()),
                        article.getTitle(),
                        article.getContent(),
                        article.getWikiType(),
                        article.getCreatedAt(),
                        article.getLastModifiedAt()
                )).toList();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("count", articlesPage.getTotalElements());
        response.put("totalPages", articlesPage.getTotalPages());
        response.put("currentPage", articlesPage.getNumber());
        response.put("articles", articleResponses);


        return ResponseEntity.ok(response);
    }


}
