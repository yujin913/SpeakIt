package com.speakit.speakit.security.oauth2;

import com.speakit.speakit.security.jwt.JwtTokenProvider;
import com.speakit.speakit.util.CookieUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import java.io.IOException;

import static com.speakit.speakit.util.Constants.MAIN_PAGE_URL;

// OAuth2AuthenticationSuccessHandler는 소셜 로그인 인증이 성공했을 때 호출되는 핸들러
@Component
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    // OAuth2 상태 정보를 담은 쿠키를 삭제하기 위해 사용
    private final HttpCookieOAuth2AuthorizationRequestRepository authorizationRequestRepository =
            new HttpCookieOAuth2AuthorizationRequestRepository();

    // JWT 토큰을 생성한 후 HttpOnly 쿠키에 저장하고, 클라이언트를 메인 페이지로 리다이렉트합니다.
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {


        // JWT 토큰 생성 (access token과 refresh token)
        String accessToken = jwtTokenProvider.generateAccessToken(authentication);
        String refreshToken = jwtTokenProvider.generateRefreshToken(authentication);


        // OAuth2 로그인 과정 중 사용했던 상태 쿠키 삭제
        authorizationRequestRepository.deleteAuthorizationRequestCookies(request, response);

        // CookieUtils.setAuthCookies()를 사용하여 HttpOnly 쿠키에 JWT 토큰을 저장
        CookieUtils.setAuthCookies(response,
                accessToken, refreshToken,
                jwtTokenProvider.getAccessTokenExpiration(), jwtTokenProvider.getRefreshTokenExpiration());

        // 클라이언트를 메인 페이지로 리다이렉트 (쿠키는 자동 전송됨)
        response.sendRedirect(MAIN_PAGE_URL);
    }
}
