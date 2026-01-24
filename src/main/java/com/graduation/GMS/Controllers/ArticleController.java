package com.graduation.GMS.Controllers;

import com.graduation.GMS.DTO.Request.ArticleRequest;
import com.graduation.GMS.Models.Enums.Wiki;
import com.graduation.GMS.Services.ArticleService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@RestController
@RequestMapping("/api/article")
@AllArgsConstructor
public class ArticleController {
    //test

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
    public ResponseEntity<?> getAllArticles(
            @RequestParam(required = false) String wiki,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);

        if (wiki != null) {
            if (wiki.equalsIgnoreCase("All")) {
                return articleService.searchArticles(null, keyword, pageable);
            } else if (wiki.equalsIgnoreCase("Sport")) {
                return articleService.searchArticles(Wiki.Sport, keyword, pageable);
            } else if (wiki.equalsIgnoreCase("Food")) {
                return articleService.searchArticles(Wiki.Food, keyword, pageable);
            } else if (wiki.equalsIgnoreCase("Fitness")) {
                return articleService.searchArticles(Wiki.Fitness, keyword, pageable);
            } else if (wiki.equalsIgnoreCase("Supplements")) {
                return articleService.searchArticles(Wiki.Supplements, keyword, pageable);
            } else if (wiki.equalsIgnoreCase("Health")) {
                return articleService.searchArticles(Wiki.Health, keyword, pageable);
            }
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", "wikiType required"));
    }

}
