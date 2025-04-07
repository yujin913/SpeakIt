package com.speakit.speakit.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import com.speakit.speakit.service.user.CustomUserDetailsService;
import java.io.IOException;

/**
 * JwtAuthenticationFilter는 들어오는 HTTP 요청의 쿠키에서 "accessToken" 쿠키를 추출하여,
 * 토큰이 유효하면 해당 사용자 정보를 SecurityContext에 설정합니다.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String token = getJwtFromRequest(request);
        logger.debug("Extracted JWT token: {}", token);

        if (StringUtils.hasText(token)) {
            if (tokenProvider.validateToken(token)) {
                logger.debug("JWT token is valid.");
                String username = tokenProvider.getUsernameFromJWT(token);
                logger.debug("Token subject (username): {}", username);

                try {
                    UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    logger.debug("User authenticated and set in SecurityContextHolder.");
                } catch (Exception ex) {
                    logger.error("Error loading user details for username: {}", username, ex);
                }
            } else {
                logger.warn("JWT token is invalid or expired.");
            }
        } else {
            logger.debug("No JWT token found in request cookies.");
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 오직 "accessToken" 쿠키에서 JWT 토큰을 추출합니다.
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("accessToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // OAuth2 콜백 URL은 JWT 필터에서 제외
        return path.startsWith("/login/oauth2/");
    }
}
