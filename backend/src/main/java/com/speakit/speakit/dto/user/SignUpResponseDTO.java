package com.speakit.speakit.dto.user;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignUpResponseDTO {
    private Long id;
    private String username;
    private String email;
    private LocalDateTime createdAt;
}
