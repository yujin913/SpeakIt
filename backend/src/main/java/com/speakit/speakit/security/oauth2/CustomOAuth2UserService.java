package com.speakit.speakit.security.oauth2;

import com.speakit.speakit.model.user.User;
import com.speakit.speakit.repository.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.*;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.core.user.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * CustomOAuth2UserService는 DefaultOAuth2UserService를 확장하여,
 * OAuth2 공급자로부터 받은 사용자 정보를 기반으로 애플리케이션 내 사용자 정보를 조회하거나 저장합니다.
 */
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private static final Logger logger = LoggerFactory.getLogger(CustomOAuth2UserService.class);

    @Autowired
    private UserRepository userRepository;

    /**
     * loadUser 메서드는 OAuth2 공급자로부터 사용자 정보를 로드한 후,
     * 공급자별로 필요한 사용자 속성을 추출하고, 이를 바탕으로 DB에 사용자 정보를 저장하거나 갱신합니다.
     */
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        logger.debug("Loading OAuth2 user information");

        // 기본 구현을 호출하여 사용자 정보를 로드
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // 공급자별 사용자 속성을 가져옴 (예: Google의 경우 'sub', 'email', 'name' 등)
        Map<String, Object> attributes = oAuth2User.getAttributes();

        // 공급자 식별자 (예: "google")
        String provider = userRequest.getClientRegistration().getRegistrationId();
        // 공급자에서 제공하는 고유 식별자 ("sub"는 Google에서 사용자 ID로 사용)
        String providerId = attributes.get("sub").toString();

        // 이메일과 이름 추출 (공급자 API에 따라 키가 다를 수 있으니 필요 시 수정)
        String email = attributes.get("email").toString();
        String username = attributes.get("name").toString();


        logger.debug("Provider: {}, ProviderId: {}, Email: {}, Username: {}", provider, providerId, email, username);


        // DB에서 이메일로 사용자 조회
        User user = userRepository.findByEmail(email);
        if (user == null) {
            logger.debug("No user found for email {}, creating new user", email);

            // 사용자가 없으면 새로 생성 (소셜 로그인은 비밀번호가 필요 없으므로 더미 값을 넣을 수 있음)
            user = User.builder()
                    .email(email)
                    .username(username)
                    .provider(provider)
                    .providerId(providerId)
                    .createdAt(LocalDateTime.now())
                    .password("SOCIAL_LOGIN") // 소셜 로그인인 경우 비밀번호는 실제로 사용되지 않음
                    .role("ROLE_USER")
                    .build();
            userRepository.save(user);

            logger.debug("New user created: {}", user);

        } else {
            logger.debug("Existing user found: {}", user);


            // 기존 사용자가 있다면 공급자 정보 업데이트 (필요 시)
            user.setProvider(provider);
            user.setProviderId(providerId);
            userRepository.save(user);

            logger.debug("User updated with provider info");
        }
        // DB에 저장된 role을 GrantedAuthority로 변환하여 DefaultOAuth2User에 포함
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(user.getRole());

        logger.debug("Returning DefaultOAuth2User with authority: {}", authority);

        // DefaultOAuth2User를 생성하여 반환. 여기서 "sub"를 사용자 식별 키로 사용
        return new DefaultOAuth2User(
                oAuth2User.getAuthorities(),
                attributes,
                "sub"
        );
    }
}
