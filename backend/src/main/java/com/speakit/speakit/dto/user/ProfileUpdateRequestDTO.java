package com.speakit.speakit.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

// 회원정보 수정 요청 DTO
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileUpdateRequestDTO {
    private String username;

    @NotBlank(message = "현재 비밀번호를 입력해 주세요.")
    private String currentPassword;

    private String newPassword;
}
