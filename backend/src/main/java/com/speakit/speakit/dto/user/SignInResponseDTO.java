package com.speakit.speakit.dto.user;

import lombok.*;
import java.time.LocalDateTime;

// 로그인 성공 응답 DTO
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignInResponseDTO {
    private Long id;
    private String username;
    private String email;
    private LocalDateTime createdAt;
    private String accessToken;
    private String refreshToken;
}
