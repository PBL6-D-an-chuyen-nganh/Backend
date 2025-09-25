package com.pbl.backend.service;

import com.pbl.backend.model.Article;
import com.pbl.backend.repository.ArticleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.stereotype.Service;

@Service
public class ArticleService {
    @Autowired
    private ArticleRepository articleRepository;

    public Page<Article> getArticles(Pageable pageable) {
        return articleRepository.findAll(pageable);
    }

    public Page<Article> getArticlesByCategory(Integer categoryID, Pageable pageable) {
        return articleRepository.findByCategoryID(categoryID, pageable);
    }
    public Article getArticleById(Long id) {
        return articleRepository.findById(id).orElse(null);
    }

}
