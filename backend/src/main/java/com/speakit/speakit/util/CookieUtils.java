package com.speakit.speakit.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.util.SerializationUtils;

import java.time.Duration;
import java.util.Base64;
import java.util.Optional;

/**
 * CookieUtils는 쿠키 추가, 조회, 삭제 및 객체의 직렬화/역직렬화를 담당합니다.
 * cross-origin 환경에서도 쿠키가 정상적으로 전송되도록 SameSite=None 옵션을 추가하며,
 * 개발 환경에서는 Secure 플래그를 비활성화하여 HTTP에서도 쿠키가 전송되도록 합니다.
 */
public class CookieUtils {

    // 개발 환경: HTTP 사용 시 false, 운영 환경(HTTPS)에서는 true로 설정하세요.
    private static final boolean IS_SECURE = false;

    /**
     * HttpServletRequest에서 지정한 이름의 쿠키를 Optional<Cookie>로 반환합니다.
     */
    public static Optional<Cookie> getCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(name)) {
                    return Optional.of(cookie);
                }
            }
        }
        return Optional.empty();
    }

    /**
     * 쿠키를 직접 문자열로 구성하여 Set-Cookie 헤더에 추가합니다.
     * SameSite=None; HttpOnly 옵션을 포함하며, IS_SECURE 값에 따라 Secure 속성을 조건부로 추가합니다.
     *
     * @param response HttpServletResponse
     * @param name     쿠키 이름
     * @param value    쿠키 값
     * @param maxAge   쿠키의 최대 생명 주기 (초)
     */
    public static void addCookie(HttpServletResponse response, String name, String value, int maxAge) {
        String secureAttribute = IS_SECURE ? "; Secure" : "";
        String cookieValue = String.format("%s=%s; Path=/; Max-Age=%d; HttpOnly%s; SameSite=None",
                name, value, maxAge, secureAttribute);
        response.addHeader("Set-Cookie", cookieValue);
    }


    /**
     * JWT 토큰(Access Token과 Refresh Token)을 HttpOnly 쿠키에 저장합니다.
     * 전달받은 accessTokenExpiration, refreshTokenExpiration 값은 밀리초 단위이며, 쿠키 설정은 초 단위로 변환합니다.
     *
     * @param response        HttpServletResponse
     * @param accessToken     JWT Access Token
     * @param refreshToken    JWT Refresh Token
     * @param accessTokenExp  access token 만료 시간 (밀리초)
     * @param refreshTokenExp refresh token 만료 시간 (밀리초)
     */
    public static void setAuthCookies(HttpServletResponse response, String accessToken, String refreshToken,
                                      Duration accessTokenExp, Duration refreshTokenExp) {

        // Duration을 초 단위로 변환합니다.
        int accessTokenSeconds = (int) accessTokenExp.toSeconds();
        int refreshTokenSeconds = (int) refreshTokenExp.toSeconds();

        Cookie accessCookie = new Cookie("accessToken", accessToken);
        accessCookie.setHttpOnly(true);
        accessCookie.setPath("/");
        accessCookie.setMaxAge(accessTokenSeconds);
        accessCookie.setSecure(IS_SECURE);

        Cookie refreshCookie = new Cookie("refreshToken", refreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(refreshTokenSeconds);
        refreshCookie.setSecure(IS_SECURE);

        response.addCookie(accessCookie);
        response.addCookie(refreshCookie);
    }


    /**
     * 지정한 이름의 쿠키를 삭제합니다.
     *
     * @param request  HttpServletRequest
     * @param response HttpServletResponse
     * @param name     삭제할 쿠키 이름
     */
    public static void deleteCookie(HttpServletRequest request, HttpServletResponse response, String name) {
        getCookie(request, name).ifPresent(cookie -> {
            String secureAttribute = IS_SECURE ? "; Secure" : "";
            String cookieValue = String.format("%s=; Path=/; Max-Age=0; HttpOnly%s; SameSite=None",
                    name, secureAttribute);
            response.addHeader("Set-Cookie", cookieValue);
        });
    }


    /**
     * 여러 쿠키를 한 번에 삭제하는 헬퍼 메서드.
     * @param response HttpServletResponse
     * @param cookieNames 삭제할 쿠키 이름 목록
     */
    public static void clearCookies(HttpServletResponse response, String... cookieNames) {
        for (String cookieName : cookieNames) {
            Cookie cookie = new Cookie(cookieName, null);
            cookie.setPath("/");
            cookie.setMaxAge(0);
            cookie.setHttpOnly(true);
            response.addCookie(cookie);
        }
    }


    /**
     * 객체를 직렬화하여 Base64 URL 인코딩된 문자열로 변환합니다.
     */
    public static String serialize(Object object) {
        return Base64.getUrlEncoder().encodeToString(SerializationUtils.serialize(object));
    }

    /**
     * 쿠키의 값을 역직렬화하여 지정된 타입의 객체로 변환합니다.
     */
    public static <T> T deserialize(Cookie cookie, Class<T> cls) {
        return cls.cast(SerializationUtils.deserialize(Base64.getUrlDecoder().decode(cookie.getValue())));
    }
}
