package com.speakit.speakit.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.security.core.Authentication;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * JwtTokenProvider는 application.properties에 저장된 jwt.secret, jwt.accessTokenExpiration, jwt.refreshTokenExpiration을 사용하여
 * JWT 토큰을 생성 및 검증하는 기능을 제공합니다.
 */
@Component
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    // application.properties에 정의된 jwt.secret 값
    @Value("${jwt.secret}")
    private String jwtSecret;

    // application.properties에 정의된 Access Token 만료 시간 (밀리초 단위)
    @Value("${jwt.accessTokenExpiration}")
    private long jwtAccessTokenExpiration;

    // application.properties에 정의된 Refresh Token 만료 시간 (밀리초 단위)
    @Value("${jwt.refreshTokenExpiration}")
    private long jwtRefreshTokenExpiration;

    // SecretKey 객체는 애플리케이션 시작 시 한 번 초기화됩니다.
    private SecretKey jwtSecretKey;

    // @PostConstruct를 사용하여 빈 초기화 후, 고정된 SecretKey를 생성합니다.
    @PostConstruct
    public void init() {
        // jwtSecret의 바이트 배열을 사용해 HS512 알고리즘에 적합한 SecretKey 생성
        jwtSecretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    /**
     * 인증 객체를 기반으로 Access Token을 생성합니다.
     */
    public String generateAccessToken(Authentication authentication) {
        String username = authentication.getName();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtAccessTokenExpiration);

        logger.debug("Generating access token for user: {}, expires at: {}", username, expiryDate);

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(jwtSecretKey)
                .compact();
    }

    /**
     * 인증 객체를 기반으로 Refresh Token을 생성합니다.
     */
    public String generateRefreshToken(Authentication authentication) {
        String username = authentication.getName();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtRefreshTokenExpiration);

        logger.debug("Generating refresh token for user: {}, expires at: {}", username, expiryDate);

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(jwtSecretKey)
                .compact();
    }

    /**
     * JWT 토큰에서 사용자 이름(이메일)을 추출합니다.
     */
    public String getUsernameFromJWT(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(jwtSecretKey)
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    /**
     * JWT 토큰의 유효성을 검사합니다.
     */
    public boolean validateToken(String authToken) {
        try {
            Jwts.parser().setSigningKey(jwtSecretKey).parseClaimsJws(authToken);
            return true;
        } catch (SignatureException ex) {
            // 서명이 올바르지 않은 경우
        } catch (MalformedJwtException ex) {
            // 토큰 형식이 잘못된 경우
        } catch (ExpiredJwtException ex) {
            // 토큰이 만료된 경우
        } catch (UnsupportedJwtException ex) {
            // 지원되지 않는 토큰인 경우
        } catch (IllegalArgumentException ex) {
            // 토큰이 비어 있는 경우
        }
        return false;
    }
}
