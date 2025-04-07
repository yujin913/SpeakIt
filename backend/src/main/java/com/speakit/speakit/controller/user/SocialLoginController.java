package com.speakit.speakit.controller.user;

import com.speakit.speakit.model.user.User;
import com.speakit.speakit.repository.user.UserRepository;
import com.speakit.speakit.security.JwtTokenProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/login/oauth2/callback")
public class SocialLoginController {

    private static final Logger logger = LoggerFactory.getLogger(SocialLoginController.class);

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserRepository userRepository;

    // 구글 OAuth2 클라이언트 정보 (application.properties에서 설정)
    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String googleClientSecret;

    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String googleRedirectUri;

    @GetMapping("/google")
    public void googleCallback(@RequestParam("code") String code, HttpServletResponse response) throws IOException {
        logger.debug("Received Google OAuth2 callback with code: {}", code);
        RestTemplate restTemplate = new RestTemplate();

        // 1. 인가 코드로 토큰 교환 (동일)
        String tokenUrl = "https://oauth2.googleapis.com/token";
        HttpHeaders tokenHeaders = new HttpHeaders();
        tokenHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> tokenParams = new LinkedMultiValueMap<>();
        tokenParams.add("code", code);
        tokenParams.add("client_id", googleClientId);
        tokenParams.add("client_secret", googleClientSecret);
        tokenParams.add("redirect_uri", googleRedirectUri);
        tokenParams.add("grant_type", "authorization_code");

        HttpEntity<MultiValueMap<String, String>> tokenRequest = new HttpEntity<>(tokenParams, tokenHeaders);
        ResponseEntity<Map> tokenResponse = restTemplate.postForEntity(tokenUrl, tokenRequest, Map.class);

        if (!tokenResponse.getStatusCode().is2xxSuccessful() || tokenResponse.getBody() == null) {
            logger.error("Failed to retrieve access token from Google");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Failed to retrieve access token from Google");
            return;
        }

        Map<String, Object> tokenBody = tokenResponse.getBody();
        String googleAccessToken = (String) tokenBody.get("access_token");

        logger.debug("Retrieved Google access token");

        // 2. 구글 사용자 정보 조회 (동일)
        String userInfoUrl = "https://www.googleapis.com/oauth2/v3/userinfo";
        HttpHeaders userInfoHeaders = new HttpHeaders();
        userInfoHeaders.setBearerAuth(googleAccessToken);
        HttpEntity<String> userInfoRequest = new HttpEntity<>(userInfoHeaders);

        ResponseEntity<Map> userInfoResponse = restTemplate.exchange(userInfoUrl, HttpMethod.GET, userInfoRequest, Map.class);
        if (!userInfoResponse.getStatusCode().is2xxSuccessful() || userInfoResponse.getBody() == null) {
            logger.error("Failed to retrieve user info from Google");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Failed to retrieve user info from Google");
            return;
        }

        Map<String, Object> userInfo = userInfoResponse.getBody();
        String email = (String) userInfo.get("email");
        String name = (String) userInfo.get("name");
        String googleId = (String) userInfo.get("sub");

        logger.debug("Google user info - email: {}, name: {}, googleId: {}", email, name, googleId);

        // 3. 사용자 정보 DB 처리 (동일)
        User user = userRepository.findByEmail(email);
        if (user == null) {
            logger.debug("No existing user found for email {}, creating new user", email);

            user = User.builder()
                    .email(email)
                    .username(name)
                    .provider("google")
                    .providerId(googleId)
                    .password("SOCIAL_LOGIN")
                    .createdAt(LocalDateTime.now())
                    .role("ROLE_USER")
                    .build();
            userRepository.save(user);
            logger.debug("New user created: {}", user);
        } else {
            logger.debug("Existing user found: {}", user);

            user.setProvider("google");
            user.setProviderId(googleId);
            userRepository.save(user);
            logger.debug("User updated with provider info");
        }

        // 4. JWT 토큰 생성 (동일)
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                user.getEmail(),
                null,
                List.of(new SimpleGrantedAuthority(user.getRole()))
        );
        String jwtAccessToken = jwtTokenProvider.generateAccessToken(authToken);
        String jwtRefreshToken = jwtTokenProvider.generateRefreshToken(authToken);

        logger.debug("JWT tokens generated for user: {}", user.getEmail());

        // HttpOnly 쿠키에 JWT 토큰 저장
        Cookie accessCookie = new Cookie("accessToken", jwtAccessToken);
        accessCookie.setHttpOnly(true);
        accessCookie.setPath("/");
        accessCookie.setMaxAge(3600);
        accessCookie.setSecure(false); // 개발 환경: false, 운영에서는 true
        Cookie refreshCookie = new Cookie("refreshToken", jwtRefreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(86400);
        refreshCookie.setSecure(false);

        response.addCookie(accessCookie);
        response.addCookie(refreshCookie);

        logger.debug("JWT tokens stored in HttpOnly cookies");

        logger.debug("Redirecting to main page");
        // 5. 메인 페이지로 리다이렉트
        response.sendRedirect("http://localhost:3000");
    }

}

