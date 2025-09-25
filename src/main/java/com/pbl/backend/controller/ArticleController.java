package com.pbl.backend.controller;

import com.pbl.backend.dto.PagedResponse;
import com.pbl.backend.model.Article;
import com.pbl.backend.service.ArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/articles")
public class ArticleController {

    @Autowired
    private ArticleService articleService;

    @GetMapping
    public ResponseEntity<PagedResponse<Article>> getArticles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Article> articles = articleService.getArticles(pageable);

        PagedResponse<Article> response = new PagedResponse<>(
                articles.getContent(),
                articles.getNumber(),
                articles.getSize(),
                articles.getTotalElements(),
                articles.getTotalPages(),
                articles.isLast()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/category/{categoryID}")
    public ResponseEntity<PagedResponse<Article>> getArticlesByCategory(
            @PathVariable Integer categoryID,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Article> articles = articleService.getArticlesByCategory(categoryID, pageable);

        PagedResponse<Article> response = new PagedResponse<>(
                articles.getContent(),
                articles.getNumber(),
                articles.getSize(),
                articles.getTotalElements(),
                articles.getTotalPages(),
                articles.isLast()
        );

        return ResponseEntity.ok(response);
    }
    @GetMapping("/{id}")
    public ResponseEntity<Article> getArticleById(@PathVariable Long id) {
        Article article = articleService.getArticleById(id);
        if (article == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(article);
    }

}
