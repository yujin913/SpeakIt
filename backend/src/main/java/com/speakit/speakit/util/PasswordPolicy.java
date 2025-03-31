package com.speakit.speakit.util;

import java.util.ArrayList;
import java.util.List;

public class PasswordPolicy {
    public static final int MIN_LENGTH = 6;

    /**
     * 비밀번호가 정책을 만족하는지 검증합니다.
     * 만족하지 않는 경우 오류 메시지를 반환합니다.
     *
     * @param password 검증할 비밀번호
     * @return 오류 메시지 리스트 (정책을 만족하면 빈 리스트)
     */

    public static List<String> validate(String password) {
        List<String> errors = new ArrayList<>();
        if (password == null || password.length() < MIN_LENGTH) {
            errors.add(MIN_LENGTH + "자 이상의 비밀번호를 사용해 주세요.");
        }
        return errors;
    }
}
