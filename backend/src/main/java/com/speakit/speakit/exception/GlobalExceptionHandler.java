package com.speakit.speakit.exception;

import com.speakit.speakit.dto.error.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // MultiErrorException 처리: 여러 오류 메시지를 반환
    @ExceptionHandler(MultiErrorException.class)
    public ResponseEntity<ErrorResponse> handleMultiErrorException(MultiErrorException e) {
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    // RuntimeException 처리
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException e) {
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    // 입력 검증 오류 (DTO에 @Valid로 발생)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException e) {
        String errorMessage = e.getBindingResult().getAllErrors().stream()
                .map(error -> error.getDefaultMessage())
                .collect(java.util.stream.Collectors.joining(", "));
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), errorMessage);
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(BadCredentialsException e) {
        String customMessage = "입력하신 이메일 또는 비밀번호가 올바르지 않습니다. 다시 시도해 주세요.";
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.UNAUTHORIZED.value(), customMessage);
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    // 소셜 로그인 사용자에 대해 프로필 수정을 시도할 경우 발생하는 예외 처리
    @ExceptionHandler(SocialLoginUpdateException.class)
    public ResponseEntity<ErrorResponse> handleSocialLoginException(SocialLoginUpdateException e) {
        String customMessage = "소셜 로그인 사용자는 회원정보 수정이 불가합니다.";
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.UNAUTHORIZED.value(), customMessage);
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }
}
