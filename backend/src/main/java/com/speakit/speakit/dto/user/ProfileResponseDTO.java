package com.speakit.speakit.dto.user;

import lombok.*;

// 회원정보 응답 DTO
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileResponseDTO {
    private String username;
    private String email;
    private String password;
    private String registrationDate;    // 가입일을 "yyyy-MM-dd" 형태의 문자열로 반환
}
