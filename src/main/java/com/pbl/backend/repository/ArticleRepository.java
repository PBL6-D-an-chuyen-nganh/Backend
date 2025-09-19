package com.pbl.backend.repository;

import com.pbl.backend.model.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArticleRepository extends JpaRepository<Article, Long> {
    Page<Article> findByCategoryID(Integer categoryID, Pageable pageable);
}

