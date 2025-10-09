package com.pbl.backend.service;

import com.pbl.backend.dto.PostDTO;
import com.pbl.backend.model.Post;
import com.pbl.backend.model.User;
import com.pbl.backend.repository.PostRepository;
import com.pbl.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PostService {
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private UserRepository userRepository;

    public Post createPost(Post post) {
        if (post.getAuthor() != null && post.getAuthor().getUserId() != null) {
            User author = userRepository.findById(post.getAuthor().getUserId())
                    .orElseThrow(() -> new RuntimeException("Author not found"));
            post.setAuthor(author);
        } else {
            throw new RuntimeException("Author ID is required");
        }
        post.setCreatedAt(LocalDateTime.now());
        return postRepository.save(post);
    }

    @Transactional(readOnly = true)
    public List<PostDTO> getAllPosts() {
        List<Post> posts = postRepository.findAll();
        return posts.stream()
                .map(PostDTO::fromEntity)
                .toList();
    }
}
