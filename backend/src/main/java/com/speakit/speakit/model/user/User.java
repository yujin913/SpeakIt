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

    @Column(nullable = false)
    private String role;

    private LocalDateTime createdAt;

    // JWT Refresh Token 저장 (필요 시 갱신 및 만료 체크)
    @Column(name = "refresh_token")
    private String refreshToken;

    // 소셜 로그인 관련 정보 (연동 해제 시 null 가능)
    private String provider;
    private String providerId;

    // 소셜 로그인 액세스 토큰 (구글 연동 해제 등 기능에 사용)
    @Column(name = "social_access_token")
    private String socialAccessToken;

}
