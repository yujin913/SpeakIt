package com.speakit.speakit.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignUpRequestDTO {
    @NotBlank(message = "이름을 입력해 주세요.")
    private String username;

    @NotBlank(message = "이메일을 입력해 주세요.")
    @Email(message = "유효한 이메일 주소를 입력해 주세요.")
    private String email;

    @NotBlank(message = "비밀번호를 입력해 주세요.")
    private String password;
}
