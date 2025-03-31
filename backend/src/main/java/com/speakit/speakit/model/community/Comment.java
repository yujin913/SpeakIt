package com.speakit.speakit.model.community;

import com.speakit.speakit.model.user.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;

// 댓글 엔티티
@Entity
@Table(name = "comments")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(length = 2000)
    private String content;

    private LocalDateTime createdAt;

    // 기본 생성자, getter, setter 생략
}
