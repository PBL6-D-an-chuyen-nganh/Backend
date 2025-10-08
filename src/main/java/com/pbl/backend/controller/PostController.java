package com.pbl.backend.controller;

import com.pbl.backend.dto.PostDTO;
import com.pbl.backend.model.Post;
import com.pbl.backend.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts")

public class PostController {
    @Autowired
    private PostService postService;

    @PostMapping
    public PostDTO createPost(@RequestBody Post post) {
        Post savedPost = postService.createPost(post);
        return PostDTO.fromEntity(savedPost);
    }


    @GetMapping
    public List<PostDTO> getAllPosts() {
        return postService.getAllPosts();
    }
}
