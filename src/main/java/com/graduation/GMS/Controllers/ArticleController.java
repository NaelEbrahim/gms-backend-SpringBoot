package com.graduation.GMS.Controllers;

import com.graduation.GMS.DTO.Request.ArticleRequest;
import com.graduation.GMS.Models.Enums.Wiki;
import com.graduation.GMS.Services.ArticleService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


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
    public ResponseEntity<?> getAllArticles(@RequestParam String wiki) {
        if (wiki.equalsIgnoreCase("Health")) {
            return articleService.getAllArticlesByWikiType(Wiki.Health);        }
        else if (wiki.equalsIgnoreCase("Sport")) {
            return articleService.getAllArticlesByWikiType(Wiki.Sport);        }
        else if (wiki.equalsIgnoreCase("Food")) {
            return articleService.getAllArticlesByWikiType(Wiki.Food);        }
        else if (wiki.equalsIgnoreCase("Fitness")) {
            return articleService.getAllArticlesByWikiType(Wiki.Fitness);        }
        else if (wiki.equalsIgnoreCase("Supplements")) {
            return articleService.getAllArticlesByWikiType(Wiki.Supplements);        }
        else {
            return articleService.getAllArticles();
        }
    }

}
