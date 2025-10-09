package com.pbl.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "posts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer postID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "authorID")
    private User author;

    private String title;

    @Column(length = 5000)
    private String content;

    private LocalDateTime createdAt;
}
