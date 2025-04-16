package com.speakit.speakit.exception;

// 소셜 로그인 사용자 정보 수정 에러
public class SocialLoginUpdateException extends RuntimeException {
    public SocialLoginUpdateException(String message) {
        super(message);
    }
}
