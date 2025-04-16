package com.speakit.speakit.controller.user;

import com.speakit.speakit.dto.user.SignInResponseDTO;
import com.speakit.speakit.security.jwt.JwtTokenProvider;
import com.speakit.speakit.service.user.SocialUserService;
import com.speakit.speakit.util.CookieUtils;
import com.speakit.speakit.util.OAuthUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Optional;

import static com.speakit.speakit.util.Constants.MAIN_PAGE_URL;

@RestController
public class SocialLoginController {

    private final SocialUserService socialUserService;
    private final JwtTokenProvider jwtTokenProvider;

    public SocialLoginController(SocialUserService socialUserService, JwtTokenProvider jwtTokenProvider) {
        this.socialUserService = socialUserService;
        this.jwtTokenProvider = jwtTokenProvider;
    }


    @GetMapping("/login/oauth2/callback/google")
    public void googleCallback(@RequestParam(value = "code", required = false) String code,
                               @RequestParam(value = "error", required = false) String error,
                               HttpServletResponse response) throws IOException {

        // 사용자가 동의 취소 등의 이유로 error 파라미터가 전달된 경우 로그인 페이지로 리다이렉트
        if (OAuthUtils.checkCancelled(error, response)) return;

        // SocialUserService 메서드로 구글 인가 코드를 처리하여, 내부 JWT 토큰 정보를 포함한 SignInResponseDTO 반환
        SignInResponseDTO signInResponseDTO = socialUserService.processGoogleSocialLogin(code);

        // JwtTokenProvider에서 Duration 타입으로 만료 시간을 가져오고, CookieUtils.setAuthCookies 메서드에 직접 Duration을 전달합니다.
        CookieUtils.setAuthCookies(response,
                signInResponseDTO.getAccessToken(), signInResponseDTO.getRefreshToken(),
                jwtTokenProvider.getAccessTokenExpiration(), jwtTokenProvider.getRefreshTokenExpiration());

        // 메인 페이지로 리다이렉트합니다.
        response.sendRedirect(MAIN_PAGE_URL);
    }


    @GetMapping("/login/oauth2/callback/naver")
    public void naverCallback(@RequestParam(value = "code", required = false) String code,
                              @RequestParam(value = "error", required = false) String error,
                              HttpServletResponse response) throws IOException {

        if (OAuthUtils.checkCancelled(error, response)) return;

        SignInResponseDTO signInResponseDTO = socialUserService.processNaverSocialLogin(code);
        CookieUtils.setAuthCookies(response,
                signInResponseDTO.getAccessToken(), signInResponseDTO.getRefreshToken(),
                jwtTokenProvider.getAccessTokenExpiration(), jwtTokenProvider.getRefreshTokenExpiration());
        response.sendRedirect(MAIN_PAGE_URL);
    }


    @GetMapping("/login/oauth2/callback/kakao")
    public void kakaoCallback(@RequestParam(value = "code", required = false) String code,
                              @RequestParam(value = "error", required = false) String error,
                              HttpServletResponse response) throws IOException {

        if (OAuthUtils.checkCancelled(error, response)) return;

        SignInResponseDTO signInResponseDTO = socialUserService.processKakaoSocialLogin(code);
        CookieUtils.setAuthCookies(response,
                signInResponseDTO.getAccessToken(), signInResponseDTO.getRefreshToken(),
                jwtTokenProvider.getAccessTokenExpiration(), jwtTokenProvider.getRefreshTokenExpiration());
        response.sendRedirect(MAIN_PAGE_URL);
    }


    @PostMapping("/disconnect/social")
    public ResponseEntity<String> disconnectSocialAccount(HttpServletRequest request, HttpServletResponse response) {
        // "accessToken" 쿠키를 찾기 위해 CookieUtils.getCookie 메서드를 호출
        Optional<Cookie> tokenCookieOpt = CookieUtils.getCookie(request, "accessToken");

        // 쿠키가 존재하지 않으면 인증되지 않은 상태로 간주하여 401 UNAUTHORIZED 응답 반환
        if (tokenCookieOpt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        // "accessToken" 쿠키에서 JWT 토큰 값을 추출
        String token = tokenCookieOpt.get().getValue();
        try {
            // socialUserService를 통해, 토큰을 기반으로 소셜 계정 연동 해제 및 사용자 삭제 처리
            socialUserService.disconnectSocialAccountByToken(token);

            // 연동 해제 후, JSESSIONID, accessToken, refreshToken 쿠키를 모두 삭제하여 클라이언트 인증 정보를 제거
            CookieUtils.clearCookies(response, "JSESSIONID", "accessToken", "refreshToken");

            // 성공적으로 처리되었음을 나타내며, 성공 메시지와 함께 HTTP OK 응답을 반환
            return new ResponseEntity<>("소셜 연동 해제 및 계정 삭제가 완료되었습니다.", HttpStatus.OK);
        } catch (RuntimeException e) {

            // 실패한 경우 RuntimeException의 메시지를 포함하여 BAD REQUEST 응답을 반환
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
