package com.pbl.backend.dto.response;

import com.pbl.backend.model.Post;
import com.pbl.backend.model.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class PostDTO {
    Integer postID;
    String title;
    String content;
    private LocalDateTime createdAt;
    private AuthorDTO author;

    public static PostDTO fromEntity(Post post) {
        if (post == null) return null;

        AuthorDTO authorDTO = null;
        User author = post.getAuthor();
        if (author != null) {
            authorDTO = new AuthorDTO(author.getUserId(), author.getName());
        }
        return new PostDTO(
                post.getPostID(),
                post.getTitle(),
                post.getContent(),
                post.getCreatedAt(),
                authorDTO
        );
    }

}
