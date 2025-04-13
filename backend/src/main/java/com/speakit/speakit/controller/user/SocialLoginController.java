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

@RestController
public class SocialLoginController {

    private final SocialUserService socialUserService;
    private final JwtTokenProvider jwtTokenProvider;

    public SocialLoginController(SocialUserService socialUserService, JwtTokenProvider jwtTokenProvider) {
        this.socialUserService = socialUserService;
        this.jwtTokenProvider = jwtTokenProvider;
    }


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


    @PostMapping("/disconnect/google")
    public ResponseEntity<String> disconnectGoogleAccount(HttpServletRequest request, HttpServletResponse response) {
        String token = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("accessToken".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }
        if (token == null || !jwtTokenProvider.validateToken(token)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        // JWT 토큰에서 이메일 추출
        String email = jwtTokenProvider.getUsernameFromJWT(token);
        socialUserService.disconnectGoogleSocialAccount(email);
        CookieUtils.clearCookies(response, "JSESSIONID", "accessToken", "refreshToken");
        return new ResponseEntity<>("구글 연동 해제 및 계정 삭제가 완료되었습니다.", HttpStatus.OK);
    }
}
