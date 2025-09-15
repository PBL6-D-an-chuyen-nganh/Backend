package com.pbl.backend.controller;

import com.pbl.backend.model.Article;
import com.pbl.backend.service.ArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/articles/")
public class ArticleController {

    @Autowired
    private ArticleService articleService;

    @GetMapping
    public ResponseEntity<Page<Article>> getArticles(
            @RequestParam(defaultValue = "0") int page,      // số trang (bắt đầu từ 0)
            @RequestParam(defaultValue = "10") int size,     // số phần tử mỗi trang
            @RequestParam(defaultValue = "createdAt") String sortBy, // cột để sort
            @RequestParam(defaultValue = "desc") String sortDir      // asc hoặc desc
    ) {
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Article> articles = articleService.getArticles(pageable);

        return ResponseEntity.ok(articles);
    }
}
