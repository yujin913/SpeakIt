package com.speakit.speakit.service.user;

import com.speakit.speakit.dto.user.SignInResponseDTO;

// 소셜 로그인 관련 서비스 인터페이스
public interface SocialUserService {

    SignInResponseDTO processGoogleSocialLogin(String code);
    void disconnectGoogleSocialAccount(String email);

    SignInResponseDTO processNaverSocialLogin(String code);
    void disconnectNaverSocialAccount(String email);
    
    SignInResponseDTO processKakaoSocialLogin(String code);
    void disconnectKakaoSocialAccount(String email);

    
    void disconnectSocialAccountByToken(String token);
}
