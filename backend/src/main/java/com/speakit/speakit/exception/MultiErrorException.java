package com.speakit.speakit.exception;

import lombok.Getter;

import java.util.List;

// 사용자 정의 예외 클래스
@Getter
public class MultiErrorException extends RuntimeException {
    private final List<String> errorMessages;

    public MultiErrorException(List<String> errorMessages) {
        super(String.join(", ", errorMessages));
        this.errorMessages = errorMessages;
    }

}
