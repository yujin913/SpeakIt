package com.speakit.speakit.dto.user;

import lombok.*;
import java.time.LocalDateTime;

// 로그인 성공 후 사용자 정보를 반환
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
}
