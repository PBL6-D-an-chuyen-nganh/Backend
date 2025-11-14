package com.pbl.backend.service;

import com.pbl.backend.model.Article;
import com.pbl.backend.repository.ArticleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ArticleService {
    @Autowired
    private ArticleRepository articleRepository;

    public Page<Article> getArticles(Pageable pageable) {
        return articleRepository.findAll(pageable);
    }

    public Article getArticleById(Long id) {
        return articleRepository.findById(id).orElse(null);
    }

    public Page<Article> searchArticles(String keyword, Pageable pageable) {
        return articleRepository.findByTitleContainingIgnoreCase(keyword, pageable);
    }
    public List<Article> getAllArticles() {
        return articleRepository.findAll();
    }

}
