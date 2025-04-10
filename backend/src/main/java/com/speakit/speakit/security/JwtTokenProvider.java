package com.speakit.speakit.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.security.core.Authentication;
import org.springframework.util.Assert;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.util.Date;

/**
 * JwtTokenProvider는 application.properties에 저장된 jwt.secret, jwt.accessTokenExpiration, jwt.refreshTokenExpiration을 사용하여
 * JWT 토큰을 생성 및 검증하는 기능을 제공합니다.
 */
@Component
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    private final SecretKey jwtSecretKey;
    @Getter
    private final Duration accessTokenExpiration;
    @Getter
    private final Duration refreshTokenExpiration;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.accessTokenExpiration}") Duration accessTokenExpiration,
            @Value("${jwt.refreshTokenExpiration}") Duration refreshTokenExpiration) {
        Assert.hasText(secret, "JWT secret must not be empty");
        this.jwtSecretKey = Keys.hmacShaKeyFor(secret.getBytes());
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    /**
     * 인증 객체를 기반으로 Access Token을 생성합니다.
     */
    public String generateAccessToken(Authentication authentication) {
        String username = authentication.getName();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenExpiration.toMillis());

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
        Date expiryDate = new Date(now.getTime() + refreshTokenExpiration.toMillis());

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
