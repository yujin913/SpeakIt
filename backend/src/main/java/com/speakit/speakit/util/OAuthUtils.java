package com.speakit.speakit.util;

import com.speakit.speakit.dto.user.SignInResponseDTO;
import com.speakit.speakit.model.user.User;
import com.speakit.speakit.repository.user.UserRepository;
import com.speakit.speakit.security.jwt.JwtTokenProvider;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static com.speakit.speakit.util.Constants.SIGN_IN_URL;

// 소셜 로그인 과정에서 반복되는 로직(토큰 교환, 사용자 정보 조회, 사용자 정보 처리, JWT 토큰 생성)을 공통으로 수행하는 유틸리티 클래스
public class OAuthUtils {

    // 인가 코드를 소셜 서비스 토큰 엔드포인트에 전송하여 access token을 받기
    public static String exchangeCodeForAccessToken(String tokenUrl, String clientId, String clientSecret, String redirectUri, String code) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders tokenHeaders = new HttpHeaders();
        tokenHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> tokenParams = new LinkedMultiValueMap<>();
        tokenParams.add("code", code);
        tokenParams.add("client_id", clientId);
        tokenParams.add("client_secret", clientSecret);
        tokenParams.add("redirect_uri", redirectUri);
        tokenParams.add("grant_type", "authorization_code");

        HttpEntity<MultiValueMap<String, String>> tokenRequest = new HttpEntity<>(tokenParams, tokenHeaders);
        ResponseEntity<Map> tokenResponse = restTemplate.postForEntity(tokenUrl, tokenRequest, Map.class);
        if (!tokenResponse.getStatusCode().is2xxSuccessful() || tokenResponse.getBody() == null) {
            throw new RuntimeException("Failed to retrieve access token from provider");
        }
        Map<String, Object> tokenBody = tokenResponse.getBody();
        return (String) tokenBody.get("access_token");
    }


    // 지정된 사용자 정보 엔드포인트에서 access token을 사용해 사용자 정보를 조회
    public static Map<String, Object> retrieveUserInfo(String userInfoUrl, String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<String> requestEntity = new HttpEntity<>(headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Map> response = restTemplate.exchange(userInfoUrl, HttpMethod.GET, requestEntity, Map.class);
        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException("Failed to retrieve user info from provider");
        }
        return response.getBody();
    }


    // 소셜 로그인 사용자 정보를 DB에 생성하거나 업데이트
    public static User processSocialUser(String email,
                                         String username,
                                         String provider,
                                         String providerId,
                                         String socialAccessToken,
                                         UserRepository userRepository) {

        User user = userRepository.findByEmail(email);
        if (user == null) {
            user = User.builder()
                    .email(email)
                    .username(username)
                    .provider(provider)
                    .providerId(providerId)
                    .password("SOCIAL_LOGIN")
                    .createdAt(LocalDateTime.now())
                    .role("ROLE_USER")
                    .socialAccessToken(socialAccessToken)
                    .build();
            userRepository.save(user);

        } else {
            user.setProvider(provider);
            user.setProviderId(providerId);
            user.setSocialAccessToken(socialAccessToken);
            user.setUsername(username);
            userRepository.save(user);
        }

        return user;
    }


    // 내부 서비스용 JWT Access Token과 Refresh Token을 생성하여 SignInResponseDTO로 반환
    public static SignInResponseDTO generateJwtTokensForUser(User user, JwtTokenProvider jwtTokenProvider) {
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(user.getEmail(), null, List.of(new SimpleGrantedAuthority(user.getRole())));

        String jwtAccessToken = jwtTokenProvider.generateAccessToken(authToken);
        String jwtRefreshToken = jwtTokenProvider.generateRefreshToken(authToken);

        return SignInResponseDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .createdAt(user.getCreatedAt())
                .accessToken(jwtAccessToken)
                .refreshToken(jwtRefreshToken)
                .build();
    }


    // 지정한 이메일의 사용자를 조회하고, 해당 사용자의 provider가 expectedProvider와 일치하면 전달받은 revokeUrl로 연동 해제 API를 호출하고, 최종적으로 사용자 레코드를 삭제
    public static void disconnectSocialProvider(String email,
                                                String expectedProvider,
                                                UserRepository userRepository,
                                                String revokeUrl) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new RuntimeException("사용자 정보를 찾을 수 없습니다.");
        }

        if (user.getProvider() == null || user.getProvider().isBlank() ||
                !expectedProvider.equalsIgnoreCase(user.getProvider())) {
            throw new RuntimeException("지원되지 않는 소셜 로그인입니다.");
        }

        String socialAccessToken = user.getSocialAccessToken();
        if (socialAccessToken != null && !socialAccessToken.isBlank()) {
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.getForEntity(revokeUrl, String.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException(expectedProvider + " 연동 해제에 실패하였습니다.");
            }
        }

        userRepository.delete(user);
    }


    // 요청 파라미터 error가 존재하면, 사용자가 소셜 로그인 동의를 취소한 것으로 보고 로그인 페이지로 리다이렉트
    public static boolean checkCancelled(String error, HttpServletResponse response) throws IOException {
        if (error != null) {
            response.sendRedirect(SIGN_IN_URL);
            return true;
        }
        return false;
    }
}
