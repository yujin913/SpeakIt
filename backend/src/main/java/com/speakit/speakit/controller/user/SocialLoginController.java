package com.speakit.speakit.controller.user;

import com.speakit.speakit.dto.user.SignInResponseDTO;
import com.speakit.speakit.security.JwtTokenProvider;
import com.speakit.speakit.service.user.SocialUserService;
import com.speakit.speakit.util.CookieUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Optional;

@RestController
public class SocialLoginController {

    private final SocialUserService socialUserService;
    private final JwtTokenProvider jwtTokenProvider;

    public SocialLoginController(SocialUserService socialUserService, JwtTokenProvider jwtTokenProvider) {
        this.socialUserService = socialUserService;
        this.jwtTokenProvider = jwtTokenProvider;
    }


    /**
     * 구글 소셜 로그인 콜백 엔드포인트
     * 인가 코드를 받아 processGoogleSocialLogin()을 호출하고, JWT 토큰을 HttpOnly 쿠키에 저장한 후 메인 페이지로 리다이렉트합니다.
     */
    @GetMapping("/login/oauth2/callback/google")
    public void googleCallback(@RequestParam("code") String code, HttpServletResponse response) throws IOException {
        // SocialUserService가 구글 인가 코드를 처리하여, 내부 JWT 토큰 정보를 포함한 SignInResponseDTO를 반환합니다.
        SignInResponseDTO signInResponseDTO = socialUserService.processGoogleSocialLogin(code);

        // JwtTokenProvider에서 Duration 타입으로 만료 시간을 가져옵니다.
        // 이후 CookieUtils.setAuthCookies 메서드에 직접 Duration을 전달합니다.
        CookieUtils.setAuthCookies(response,
                signInResponseDTO.getAccessToken(),
                signInResponseDTO.getRefreshToken(),
                jwtTokenProvider.getAccessTokenExpiration(),
                jwtTokenProvider.getRefreshTokenExpiration());

        // 메인 페이지로 리다이렉트합니다.
        response.sendRedirect("http://localhost:3000");
    }

    /**
     * 네이버 소셜 로그인 콜백 엔드포인트
     * 인가 코드를 받아 SocialUserService.processNaverSocialLogin()을 호출한 후,
     * JwtTokenProvider에서 만료 시간을 가져와 HttpOnly 쿠키에 JWT 토큰을 저장하고, 메인 페이지로 리다이렉트합니다.
     */
    @GetMapping("/login/oauth2/callback/naver")
    public void naverCallback(@RequestParam("code") String code, HttpServletResponse response) throws IOException {
        SignInResponseDTO signInResponseDTO = socialUserService.processNaverSocialLogin(code);

        // JwtTokenProvider에서 만료 시간을 Duration 타입으로 반환하므로, 이를 그대로 전달합니다.
        CookieUtils.setAuthCookies(response,
                signInResponseDTO.getAccessToken(),
                signInResponseDTO.getRefreshToken(),
                jwtTokenProvider.getAccessTokenExpiration(),
                jwtTokenProvider.getRefreshTokenExpiration());

        response.sendRedirect("http://localhost:3000");
    }


    @PostMapping("/disconnect/social")
    public ResponseEntity<String> disconnectSocialAccount(HttpServletRequest request, HttpServletResponse response) {
        Optional<Cookie> tokenCookieOpt = CookieUtils.getCookie(request, "accessToken");
        if (tokenCookieOpt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        String token = tokenCookieOpt.get().getValue();
        try {
            socialUserService.disconnectSocialAccountByToken(token);
            CookieUtils.clearCookies(response, "JSESSIONID", "accessToken", "refreshToken");
            return new ResponseEntity<>("소셜 연동 해제 및 계정 삭제가 완료되었습니다.", HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
