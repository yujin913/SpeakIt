package com.speakit.speakit.model.user;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

// 사용자 정보 엔티티
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    // 소셜 로그인 관련 정보 (연동 해제 시 null 가능)
    private String provider;
    private String providerId;

    private LocalDateTime createdAt;
}
