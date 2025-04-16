package com.speakit.speakit.security.oauth2;

import com.speakit.speakit.util.CookieUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

import static com.speakit.speakit.util.Constants.*;

// OAuth2AuthorizationRequest를 쿠키에 저장하고, 조회하며, 제거하는 역할을 수행합니다.
public class HttpCookieOAuth2AuthorizationRequestRepository implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    // 쿠키의 유효 기간을 초 단위로 설정 (180초 = 3분)
    private static final int COOKIE_EXPIRE_SECONDS = 180;


    // 요청에서 OAuth2AuthorizationRequest 쿠키를 읽어서 객체로 역직렬화합니다.
    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        // CookieUtils를 사용하여 "oauth2_auth_request" 이름의 쿠키를 검색
        Cookie cookie = CookieUtils.getCookie(request, OAUTH2_AUTH_REQUEST_COOKIE_NAME).orElse(null);

        // 쿠키가 존재하면, 해당 쿠키 값을 역직렬화하여 OAuth2AuthorizationRequest 객체로 변환 후 반환
        if (cookie != null) {
            return CookieUtils.deserialize(cookie, OAuth2AuthorizationRequest.class);
        }

        // 쿠키가 없으면 null 반환
        return null;
    }


    // OAuth2AuthorizationRequest를 쿠키에 저장하는데, 만약 전달된 authorizationRequest가 null이면, 관련 쿠키들을 삭제합니다.
    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest, HttpServletRequest request,
                                         HttpServletResponse response) {

        // authorizationRequest가 null이면, 기존 쿠키 삭제 후 메서드 종료
        if (authorizationRequest == null) {
            deleteAuthorizationRequestCookies(request, response);
            return;
        }

        // OAuth2AuthorizationRequest 객체를 직렬화하여 문자열로 변환
        String serializedRequest = CookieUtils.serialize(authorizationRequest);

        // 직렬화된 값을 "oauth2_auth_request" 쿠키에 저장 (설정된 만료 시간 적용)
        CookieUtils.addCookie(response, OAUTH2_AUTH_REQUEST_COOKIE_NAME, serializedRequest, COOKIE_EXPIRE_SECONDS);

        // 로그인 후 리다이렉트할 URL 값을 요청 파라미터에서 가져옴
        String redirectUriAfterLogin = request.getParameter(REDIRECT_URI_PARAM_COOKIE_NAME);

        // 만약 redirect URI 값이 존재하면, 별도의 쿠키에 저장 (만료 시간 동일 적용)
        if (redirectUriAfterLogin != null && !redirectUriAfterLogin.isBlank()) {
            CookieUtils.addCookie(response, REDIRECT_URI_PARAM_COOKIE_NAME, redirectUriAfterLogin, COOKIE_EXPIRE_SECONDS);
        }
    }


    // 요청에서 OAuth2AuthorizationRequest를 불러온 후, 관련 쿠키들을 삭제하고 해당 객체를 반환
    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request, HttpServletResponse response) {
        // 현재 요청에서 저장된 OAuth2AuthorizationRequest를 불러옴
        OAuth2AuthorizationRequest authRequest = loadAuthorizationRequest(request);

        // 관련된 모든 쿠키들을 삭제
        deleteAuthorizationRequestCookies(request, response);

        // 로드한 객체 반환 (삭제 후에도 반환하여 후속 처리가 가능)
        return authRequest;
    }


    // oauth2_auth_request와 redirect_uri 쿠키를 삭제합니다.
    public void deleteAuthorizationRequestCookies(HttpServletRequest request, HttpServletResponse response) {
        CookieUtils.deleteCookie(request, response, OAUTH2_AUTH_REQUEST_COOKIE_NAME);
        CookieUtils.deleteCookie(request, response, REDIRECT_URI_PARAM_COOKIE_NAME);
    }
}
