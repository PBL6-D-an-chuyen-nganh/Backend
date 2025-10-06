package com.pbl.backend.repository;

import com.pbl.backend.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Integer> {
    Post findByPostID(Integer postId);
}