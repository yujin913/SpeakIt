package com.speakit.speakit.service.user;

import com.speakit.speakit.dto.user.SignInResponseDTO;
import com.speakit.speakit.model.user.User;
import com.speakit.speakit.repository.user.UserRepository;
import com.speakit.speakit.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class SocialUserServiceImpl implements SocialUserService {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final String googleClientId;
    private final String googleClientSecret;
    private final String googleRedirectUri;

    public SocialUserServiceImpl(JwtTokenProvider jwtTokenProvider,
                                 UserRepository userRepository,
                                 @Value("${spring.security.oauth2.client.registration.google.client-id}") String googleClientId,
                                 @Value("${spring.security.oauth2.client.registration.google.client-secret}") String googleClientSecret,
                                 @Value("${spring.security.oauth2.client.registration.google.redirect-uri}") String googleRedirectUri) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
        this.googleClientId = googleClientId;
        this.googleClientSecret = googleClientSecret;
        this.googleRedirectUri = googleRedirectUri;
    }


    @Override
    public SignInResponseDTO processGoogleSocialLogin(String code) {
        RestTemplate restTemplate = new RestTemplate();

        // 1. 인가 코드를 이용하여 구글 액세스 토큰 교환
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
            throw new RuntimeException("Failed to retrieve access token from Google");
        }
        Map<String, Object> tokenBody = tokenResponse.getBody();
        String googleAccessToken = (String) tokenBody.get("access_token");

        // 2. 구글 사용자 정보 조회
        String userInfoUrl = "https://www.googleapis.com/oauth2/v3/userinfo";
        HttpHeaders userInfoHeaders = new HttpHeaders();
        userInfoHeaders.setBearerAuth(googleAccessToken);
        HttpEntity<String> userInfoRequest = new HttpEntity<>(userInfoHeaders);
        ResponseEntity<Map> userInfoResponse = restTemplate.exchange(userInfoUrl, HttpMethod.GET, userInfoRequest, Map.class);
        if (!userInfoResponse.getStatusCode().is2xxSuccessful() || userInfoResponse.getBody() == null) {
            throw new RuntimeException("Failed to retrieve user info from Google");
        }
        Map<String, Object> userInfo = userInfoResponse.getBody();
        String email = (String) userInfo.get("email");
        String name = (String) userInfo.get("name");
        String googleId = (String) userInfo.get("sub");

        // 3. 사용자 정보 DB 처리: 새 사용자는 생성, 기존 사용자는 업데이트 (socialAccessToken 저장)
        User user = userRepository.findByEmail(email);
        if (user == null) {
            user = User.builder()
                    .email(email)
                    .username(name)
                    .provider("google")
                    .providerId(googleId)
                    .password("SOCIAL_LOGIN")  // 소셜 로그인 사용자는 실질적 비밀번호 사용 안 함
                    .createdAt(LocalDateTime.now())
                    .role("ROLE_USER")
                    .socialAccessToken(googleAccessToken)  // 발급받은 소셜 액세스 토큰 저장
                    .build();
            userRepository.save(user);
        } else {
            user.setProvider("google");
            user.setProviderId(googleId);
            user.setSocialAccessToken(googleAccessToken);  // 기존 사용자는 토큰 업데이트
            userRepository.save(user);
        }

        // 4. 내부 서비스용 JWT 토큰 생성
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                user.getEmail(), null, List.of(new SimpleGrantedAuthority(user.getRole()))
        );
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

    @Override
    public void disconnectGoogleSocialAccount(String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new RuntimeException("사용자 정보를 찾을 수 없습니다.");
        }
        if (user.getProvider() != null && !user.getProvider().isBlank() &&
                "google".equalsIgnoreCase(user.getProvider())) {
            String socialAccessToken = user.getSocialAccessToken();
            if (socialAccessToken != null && !socialAccessToken.isBlank()) {
                String revokeUrl = "https://accounts.google.com/o/oauth2/revoke?token=" + socialAccessToken;
                RestTemplate restTemplate = new RestTemplate();
                try {
                    restTemplate.getForEntity(revokeUrl, String.class);
                } catch (Exception e) {
                    throw new RuntimeException("구글 연동 해제에 실패하였습니다.");
                }
            }
        }
        userRepository.delete(user);
    }
}
