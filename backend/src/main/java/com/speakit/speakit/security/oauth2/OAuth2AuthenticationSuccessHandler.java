package com.speakit.speakit.security.oauth2;

import com.speakit.speakit.security.JwtTokenProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import java.io.IOException;

/**
 * OAuth2AuthenticationSuccessHandler는 소셜 로그인 인증이 성공했을 때 호출됩니다.
 * JWT 토큰을 생성한 후 HttpOnly 쿠키에 저장하고, 클라이언트를 메인 페이지로 리다이렉트합니다.
 */
@Slf4j
@Component
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private static final Logger logger = LoggerFactory.getLogger(OAuth2AuthenticationSuccessHandler.class);

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    // OAuth2 상태 정보를 담은 쿠키를 삭제하기 위해 사용
    private final HttpCookieOAuth2AuthorizationRequestRepository authorizationRequestRepository =
            new HttpCookieOAuth2AuthorizationRequestRepository();

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        logger.debug("OAuth2 authentication success");

        // 1. JWT 토큰 생성 (access token과 refresh token)
        String accessToken = jwtTokenProvider.generateAccessToken(authentication);
        String refreshToken = jwtTokenProvider.generateRefreshToken(authentication);

        logger.debug("Generated JWT tokens");

        // 2. OAuth2 로그인 과정 중 사용했던 상태 쿠키 삭제
        authorizationRequestRepository.deleteAuthorizationRequestCookies(request, response);

        logger.debug("Deleted OAuth2 state cookies");

        // 3. HttpOnly 쿠키에 JWT 토큰 저장
        Cookie accessCookie = new Cookie("accessToken", accessToken);
        accessCookie.setHttpOnly(true);
        accessCookie.setPath("/");
        // 예: 3600초(1시간) 유효 (실제 유효시간에 맞춰 조정)
        accessCookie.setMaxAge(3600);
        // 개발환경에서는 false, 운영(HTTPS)에서는 true로 설정
        accessCookie.setSecure(false);

        Cookie refreshCookie = new Cookie("refreshToken", refreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setPath("/");
        // 예: 86400초(24시간) 유효 (실제 유효시간에 맞춰 조정)
        refreshCookie.setMaxAge(86400);
        refreshCookie.setSecure(false);

        response.addCookie(accessCookie);
        response.addCookie(refreshCookie);

        logger.debug("JWT tokens stored in HttpOnly cookies");

        // 4. 클라이언트를 메인 페이지로 리다이렉트 (쿠키는 자동 전송됨)
        response.sendRedirect("http://localhost:3000");
    }
}
