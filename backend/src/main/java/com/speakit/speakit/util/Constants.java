package com.speakit.speakit.util;

// 상수 관리 클래스
public class Constants {

    // 클라이언트 URL
    public static final String MAIN_PAGE_URL = "http://localhost:3000";
    public static final String SIGN_IN_URL = "http://localhost:3000/signIn";

    // OAuth2 쿠키 이름
    public static final String OAUTH2_AUTH_REQUEST_COOKIE_NAME = "oauth2_auth_request";
    public static final String REDIRECT_URI_PARAM_COOKIE_NAME = "redirect_uri";


    // 인스턴스화 방지용 생성자
    // 이 클래스는 상수만을 위한 용도로 사용되므로 인스턴스화할 수 없습니다.
    private Constants() {

    }
}
