package com.speakit.speakit.exception;

import java.util.List;

// 사용자 정의 예외 클래스
public class MultiErrorException extends RuntimeException {
    private final List<String> errorMessages;

    public MultiErrorException(List<String> errorMessages) {
        super(String.join(", ", errorMessages));
        this.errorMessages = errorMessages;
    }

    public List<String> getErrorMessages() {
        return errorMessages;
    }
}
