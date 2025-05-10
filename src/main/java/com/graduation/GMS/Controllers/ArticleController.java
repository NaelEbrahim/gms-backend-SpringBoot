package com.graduation.GMS.Controllers;

import com.graduation.GMS.DTO.Request.ArticleRequest;
import com.graduation.GMS.Services.ArticleService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/article")
public class ArticleController {
    @Autowired
    private ArticleService articleService;

    // Endpoint to create a new Article
    @PostMapping("/create")
    public ResponseEntity<?> createArticle(@Valid @RequestBody ArticleRequest request) {
        return articleService.createArticle(request);
    }

    // Endpoint to update an existing Article
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateArticle(@PathVariable Integer id, @Valid @RequestBody ArticleRequest request) {
        return articleService.updateArticle(id, request);
    }

    // Endpoint to delete an Article
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteArticle(@PathVariable Integer id) {
        return articleService.deleteArticle(id);
    }

    // Endpoint to get details of a specific Article by ID
    @GetMapping("/show/{id}")
    public ResponseEntity<?> getArticleById(@PathVariable Integer id) {
        return articleService.getArticleById(id);
    }

    // Endpoint to get all Articles
    @GetMapping("/show/all")
    public ResponseEntity<?> getAllArticles() {
        return articleService.getAllArticles();
    }

    // Endpoint to get Health Articles
    @GetMapping("/show/all/health")
    public ResponseEntity<?> getAllHealthArticles() {
        return articleService.getAllHealthArticles();
    }

    // Endpoint to get Sport Articles
    @GetMapping("/show/all/sport")
    public ResponseEntity<?> getAllSportArticles() {
        return articleService.getAllSportArticles();
    }
}
