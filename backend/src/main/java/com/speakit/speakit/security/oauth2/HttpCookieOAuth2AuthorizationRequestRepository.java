package com.speakit.speakit.security.oauth2;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

/**
 * HttpCookieOAuth2AuthorizationRequestRepository는 OAuth2AuthorizationRequest를 쿠키에 저장하고,
 * 로드하며, 제거하는 역할을 수행합니다.
 */
public class HttpCookieOAuth2AuthorizationRequestRepository implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    private static final Logger logger = LoggerFactory.getLogger(HttpCookieOAuth2AuthorizationRequestRepository.class);

    public static final String OAUTH2_AUTH_REQUEST_COOKIE_NAME = "oauth2_auth_request";
    public static final String REDIRECT_URI_PARAM_COOKIE_NAME = "redirect_uri";
    private static final int COOKIE_EXPIRE_SECONDS = 180;

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        Cookie cookie = CookieUtils.getCookie(request, OAUTH2_AUTH_REQUEST_COOKIE_NAME).orElse(null);
        if (cookie != null) {
            logger.debug("Loaded OAuth2AuthorizationRequest from cookie");

            return CookieUtils.deserialize(cookie, OAuth2AuthorizationRequest.class);
        }
        logger.debug("No OAuth2AuthorizationRequest cookie found");

        return null;
    }

    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest, HttpServletRequest request,
                                         HttpServletResponse response) {
        if (authorizationRequest == null) {
            logger.debug("AuthorizationRequest is null, deleting cookies");

            deleteAuthorizationRequestCookies(request, response);
            return;
        }
        String serializedRequest = CookieUtils.serialize(authorizationRequest);
        CookieUtils.addCookie(response, OAUTH2_AUTH_REQUEST_COOKIE_NAME, serializedRequest, COOKIE_EXPIRE_SECONDS);

        logger.debug("Saved OAuth2AuthorizationRequest in cookie");

        String redirectUriAfterLogin = request.getParameter(REDIRECT_URI_PARAM_COOKIE_NAME);
        if (redirectUriAfterLogin != null && !redirectUriAfterLogin.isBlank()) {
            CookieUtils.addCookie(response, REDIRECT_URI_PARAM_COOKIE_NAME, redirectUriAfterLogin, COOKIE_EXPIRE_SECONDS);
            logger.debug("Saved redirect URI in cookie: {}", redirectUriAfterLogin);

        }
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request, HttpServletResponse response) {
        OAuth2AuthorizationRequest authRequest = loadAuthorizationRequest(request);
        deleteAuthorizationRequestCookies(request, response);
        logger.debug("Removed OAuth2AuthorizationRequest cookies");
        return authRequest;
    }

    public void deleteAuthorizationRequestCookies(HttpServletRequest request, HttpServletResponse response) {
        CookieUtils.deleteCookie(request, response, OAUTH2_AUTH_REQUEST_COOKIE_NAME);
        CookieUtils.deleteCookie(request, response, REDIRECT_URI_PARAM_COOKIE_NAME);
        logger.debug("Deleted OAuth2 state cookies");
    }
}
