package com.speakit.speakit.util;

import java.util.ArrayList;
import java.util.List;

// 비밀번호 정책 정의 클래스
public class PasswordPolicy {
    public static final int MIN_LENGTH = 6;


    // 비밀번호가 정책을 만족하는지 검증
    public static List<String> validate(String password) {
        List<String> errors = new ArrayList<>();
        if (password == null || password.length() < MIN_LENGTH) {
            errors.add(MIN_LENGTH + "자 이상의 비밀번호를 사용해 주세요.");
        }
        return errors;
    }
}
