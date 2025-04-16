package com.speakit.speakit.service.user;

import com.speakit.speakit.dto.user.SignInResponseDTO;
import com.speakit.speakit.model.user.User;
import com.speakit.speakit.repository.user.UserRepository;
import com.speakit.speakit.security.jwt.JwtTokenProvider;
import com.speakit.speakit.util.OAuthUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

// SocialUserService 인터페이스에 정의된 소셜 로그인 관련 비즈니스 로직 구현
@Service
public class SocialUserServiceImpl implements SocialUserService {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    private final String googleClientId;
    private final String googleClientSecret;
    private final String googleRedirectUri;

    private final String naverClientId;
    private final String naverClientSecret;
    private final String naverRedirectUri;

    private final String kakaoClientId;
    private final String kakaoClientSecret;
    private final String kakaoRedirectUri;

    public SocialUserServiceImpl(JwtTokenProvider jwtTokenProvider,
                                 UserRepository userRepository,
                                 @Value("${spring.security.oauth2.client.registration.google.client-id}") String googleClientId,
                                 @Value("${spring.security.oauth2.client.registration.google.client-secret}") String googleClientSecret,
                                 @Value("${spring.security.oauth2.client.registration.google.redirect-uri}") String googleRedirectUri,
                                 @Value("${spring.security.oauth2.client.registration.naver.client-id}") String naverClientId,
                                 @Value("${spring.security.oauth2.client.registration.naver.client-secret}") String naverClientSecret,
                                 @Value("${spring.security.oauth2.client.registration.naver.redirect-uri}") String naverRedirectUri,
                                 @Value("${spring.security.oauth2.client.registration.kakao.client-id}") String kakaoClientId,
                                 @Value("${spring.security.oauth2.client.registration.kakao.client-secret}") String kakaoClientSecret,
                                 @Value("${spring.security.oauth2.client.registration.kakao.redirect-uri}") String kakaoRedirectUri) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
        this.googleClientId = googleClientId;
        this.googleClientSecret = googleClientSecret;
        this.googleRedirectUri = googleRedirectUri;
        this.naverClientId = naverClientId;
        this.naverClientSecret = naverClientSecret;
        this.naverRedirectUri = naverRedirectUri;
        this.kakaoClientId = kakaoClientId;
        this.kakaoClientSecret = kakaoClientSecret;
        this.kakaoRedirectUri = kakaoRedirectUri;
    }


    @Override
    public SignInResponseDTO processGoogleSocialLogin(String code) {

        // 1. 구글 토큰 엔드포인트에서 access token 교환
        String tokenUrl = "https://oauth2.googleapis.com/token";
        String googleAccessToken = OAuthUtils.exchangeCodeForAccessToken(tokenUrl, googleClientId, googleClientSecret, googleRedirectUri, code);

        // 2. 구글 사용자 정보 조회
        String userInfoUrl = "https://www.googleapis.com/oauth2/v3/userinfo";

        Map<String, Object> userInfo = OAuthUtils.retrieveUserInfo(userInfoUrl, googleAccessToken);
        String email = (String) userInfo.get("email");
        String name = (String) userInfo.get("name");
        String googleId = (String) userInfo.get("sub");

        // 3. 사용자 정보 DB 처리
        User user = OAuthUtils.processSocialUser(email, name, "google", googleId, googleAccessToken, userRepository);

        // 4. JWT 토큰 생성 및 반환
        return OAuthUtils.generateJwtTokensForUser(user, jwtTokenProvider);
    }

    
    @Override
    public void disconnectGoogleSocialAccount(String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new RuntimeException("사용자 정보를 찾을 수 없습니다.");
        }
        String revokeUrl = "https://accounts.google.com/o/oauth2/revoke?token=" + user.getSocialAccessToken();
        OAuthUtils.disconnectSocialProvider(email, "google", userRepository, revokeUrl);
    }


    @Override
    public SignInResponseDTO processNaverSocialLogin(String code) {
        RestTemplate restTemplate = new RestTemplate();

        // 1. 네이버 토큰 엔드포인트에서 access token 교환
        String tokenUrl = "https://nid.naver.com/oauth2.0/token";
        String naverAccessToken = OAuthUtils.exchangeCodeForAccessToken(
                tokenUrl,
                naverClientId,
                naverClientSecret,
                naverRedirectUri,
                code
        );

        // 2. 네이버 사용자 정보 조회 (네이버 응답은 "response" 키 안에 있음)
        String userInfoUrl = "https://openapi.naver.com/v1/nid/me";
        Map<String, Object> userInfoResponse = OAuthUtils.retrieveUserInfo(
                userInfoUrl,
                naverAccessToken
        );
        @SuppressWarnings("unchecked")
        Map<String, Object> naverUser = (Map<String, Object>) userInfoResponse.get("response");
        if (naverUser == null) {
            throw new RuntimeException("Naver user info missing 'response' field");
        }
        String email = (String) naverUser.get("email");
        String name = (String) naverUser.get("name");
        String naverId = (String) naverUser.get("id");

        // 3. 사용자 정보 DB 처리
        User user = OAuthUtils.processSocialUser(email, name, "naver", naverId, naverAccessToken, userRepository);

        // 4. JWT 토큰 생성 및 반환
        return OAuthUtils.generateJwtTokensForUser(user, jwtTokenProvider);
    }

    
    @Override
    public void disconnectNaverSocialAccount(String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new RuntimeException("사용자 정보를 찾을 수 없습니다.");
        }

        String revokeUrl = "https://nid.naver.com/oauth2.0/token?grant_type=delete"
                + "&client_id=" + naverClientId
                + "&client_secret=" + naverClientSecret
                + "&access_token=" + user.getSocialAccessToken()
                + "&service_provider=NAVER";
        OAuthUtils.disconnectSocialProvider(email, "naver", userRepository, revokeUrl);
    }


    @Override
    public SignInResponseDTO processKakaoSocialLogin(String code) {

        // 1. Kakao 인가 코드를 이용하여 액세스 토큰 교환
        String tokenUrl = "https://kauth.kakao.com/oauth/token";
        String kakaoAccessToken = OAuthUtils.exchangeCodeForAccessToken(
                tokenUrl,
                kakaoClientId,
                kakaoClientSecret,
                kakaoRedirectUri,
                code
        );

        // 2. Kakao 사용자 정보 조회
        String userInfoUrl = "https://kapi.kakao.com/v2/user/me";
        Map<String, Object> kakaoUserInfo = OAuthUtils.retrieveUserInfo(
                userInfoUrl,
                kakaoAccessToken
        );
        String kakaoId = String.valueOf(kakaoUserInfo.get("id"));
        @SuppressWarnings("unchecked")
        Map<String, Object> kakaoAccount = (Map<String, Object>) kakaoUserInfo.get("kakao_account");
        if (kakaoAccount == null || !kakaoAccount.containsKey("email")) {
            throw new RuntimeException("Kakao user info does not contain email");
        }
        String email = (String) kakaoAccount.get("email");

        // 사용자 이름 추출: 우선 최상위 profile_nickname, 없으면 properties.nickname
        String nickname = (String) kakaoUserInfo.get("profile_nickname");
        if (nickname == null || nickname.isBlank()) {
            @SuppressWarnings("unchecked")
            Map<String, Object> properties = (Map<String, Object>) kakaoUserInfo.get("properties");
            if (properties != null) {
                nickname = (String) properties.get("nickname");
            }
        }
        if (nickname == null || nickname.isBlank()) {
            nickname = "unknown";
        }

        // 3. 사용자 정보 DB 처리
        User user = OAuthUtils.processSocialUser(email, nickname, "kakao", kakaoId, kakaoAccessToken, userRepository);

        // 4. JWT 토큰 생성 및 반환
        return OAuthUtils.generateJwtTokensForUser(user, jwtTokenProvider);
    }


    @Override
    public void disconnectKakaoSocialAccount(String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new RuntimeException("사용자 정보를 찾을 수 없습니다.");
        }
        if (user.getProvider() != null && !user.getProvider().isBlank() &&
                "kakao".equalsIgnoreCase(user.getProvider())) {
            String socialAccessToken = user.getSocialAccessToken();
            if (socialAccessToken != null && !socialAccessToken.isBlank()) {
                // 카카오 연동 해제 API 호출
                String unlinkUrl = "https://kapi.kakao.com/v1/user/unlink";
                HttpHeaders headers = new HttpHeaders();
                headers.setBearerAuth(socialAccessToken);
                HttpEntity<?> entity = new HttpEntity<>(headers);
                RestTemplate restTemplate = new RestTemplate();
                try {
                    restTemplate.postForEntity(unlinkUrl, entity, String.class);
                } catch (Exception e) {
                    throw new RuntimeException("카카오 연동 해제에 실패하였습니다.");
                }
            }
        }
        userRepository.delete(user);
    }


    @Override
    public void disconnectSocialAccountByToken(String token) {
        if (!jwtTokenProvider.validateToken(token)) {
            throw new RuntimeException("유효하지 않은 토큰입니다.");
        }
        String email = jwtTokenProvider.getUsernameFromJWT(token);
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new RuntimeException("사용자 정보를 찾을 수 없습니다.");
        }
        if (user.getProvider() != null && !user.getProvider().isBlank()) {
            String provider = user.getProvider().toLowerCase();
            if ("google".equals(provider)) {
                disconnectGoogleSocialAccount(email);
            } else if ("naver".equals(provider)) {
                disconnectNaverSocialAccount(email);
            } else if ("kakao".equals(provider)) {
                disconnectKakaoSocialAccount(email);
            } else {
                throw new RuntimeException("지원되지 않는 소셜 로그인입니다.");
            }
        } else {
            throw new RuntimeException("일반 사용자는 해당 API를 사용하지 마십시오.");
        }
    }
}
