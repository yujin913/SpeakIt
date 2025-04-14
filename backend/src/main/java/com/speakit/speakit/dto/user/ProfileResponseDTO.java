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
    private String registrationDate;
    private String provider;
}
