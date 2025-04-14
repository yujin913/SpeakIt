package com.speakit.speakit.service.user;

import com.speakit.speakit.dto.user.SignInResponseDTO;

public interface SocialUserService {

    SignInResponseDTO processGoogleSocialLogin(String code);
    void disconnectGoogleSocialAccount(String email);

    SignInResponseDTO processNaverSocialLogin(String code);
    void disconnectNaverSocialAccount(String email);

    // 카카오 전용 메서드 추가
    SignInResponseDTO processKakaoSocialLogin(String code);
    void disconnectKakaoSocialAccount(String email);

    /**
     * JWT 토큰을 받아 소셜 로그인 사용자인 경우에 해당 공급자(google, naver 등)를 확인하고,
     * 적절한 연동 해제 메서드를 호출한 후 계정을 삭제합니다.
     *
     * @param token JWT 토큰 (HttpOnly 쿠키로부터 전달)
     */
    void disconnectSocialAccountByToken(String token);
}
