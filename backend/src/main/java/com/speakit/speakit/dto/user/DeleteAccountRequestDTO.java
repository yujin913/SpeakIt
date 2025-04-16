package com.speakit.speakit.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

// 회원탈퇴 요청 DTO
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeleteAccountRequestDTO {
    @NotBlank(message = "현재 비밀번호를 입력해 주세요.")
    private String currentPassword;
}
