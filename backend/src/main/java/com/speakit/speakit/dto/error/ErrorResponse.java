package com.speakit.speakit.dto.error;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

// 에러 정보 응답 DTO
@Getter
@Setter
@AllArgsConstructor
public class ErrorResponse {
    private int status;
    private String message;
}
