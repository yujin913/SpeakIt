package com.speakit.speakit.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

// 로그인 요청 시 필요한 데이터(이메일, 비밀번호)를 캡슐화
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignInRequestDTO {
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email address")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;
}
