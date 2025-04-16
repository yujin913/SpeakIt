package com.speakit.speakit.controller.user;

import com.speakit.speakit.dto.user.*;
import com.speakit.speakit.security.jwt.JwtTokenProvider;
import com.speakit.speakit.service.user.UserService;
import com.speakit.speakit.util.CookieUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

import static com.speakit.speakit.util.Constants.MAIN_PAGE_URL;

@RestController
@RequestMapping("/user")
public class UserController {


    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;

    public UserController(UserService userService, JwtTokenProvider jwtTokenProvider) {
        this.userService = userService;
        this.jwtTokenProvider = jwtTokenProvider;
    }


    // 회원가입 API: POST /user/signUp
    @PostMapping("/signUp")
    public ResponseEntity<SignUpResponseDTO> signUp(@Valid @RequestBody SignUpRequestDTO signUpRequestDTO) {
        SignUpResponseDTO responseDTO = userService.signUp(signUpRequestDTO);
        return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);
    }


    // 로그인 페이지 API: GET /user/signIn
    @GetMapping("/signIn")
    public void getSignIn(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 쿠키에 accessToken이 존재하고 유효하면 메인 페이지로 자동 리다이렉트
        String token = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("accessToken".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }

        // 이미 유효한 JWT 토큰이 있으면 메인 페이지로 리다이렉트
        if (token != null && jwtTokenProvider.validateToken(token)) {
            response.sendRedirect(MAIN_PAGE_URL);
        } else {
            response.setStatus(HttpServletResponse.SC_OK);
        }
    }


    // 로그인 로직 API: POST /user/signIn
    @PostMapping("/signIn")
    public ResponseEntity<?> signIn(@Valid @RequestBody SignInRequestDTO signInRequestDTO, HttpServletResponse response) {
        // 인증을 수행하고 JWT 토큰을 포함한 응답 DTO를 받음
        SignInResponseDTO responseDTO = userService.signIn(signInRequestDTO);

        // JwtTokenProvider에서 Duration 타입의 만료 시간을 직접 가져와 CookieUtils.setAuthCookies에 전달합니다.
        CookieUtils.setAuthCookies(response,
                responseDTO.getAccessToken(), responseDTO.getRefreshToken(),
                jwtTokenProvider.getAccessTokenExpiration(), jwtTokenProvider.getRefreshTokenExpiration());

        // HttpOnly 쿠키에 JWT 토큰을 저장하여 클라이언트에서 직접 접근 불가하니 사용자 정보만 반환
        SignInResponseDTO minimalResponse = SignInResponseDTO.builder()
                .id(responseDTO.getId())
                .username(responseDTO.getUsername())
                .email(responseDTO.getEmail())
                .createdAt(responseDTO.getCreatedAt())
                .build();

        return new ResponseEntity<>(minimalResponse, HttpStatus.OK);
    }


    // 로그인 상태 조회 API: GET /user/loginStatus
    @GetMapping("/loginStatus")
    public ResponseEntity<?> loginStatus(Authentication authentication) {
        if (userService.isAuthenticated(authentication)) {
            ProfileResponseDTO profile = userService.getProfileByEmail(authentication.getName());
            return ResponseEntity.ok(Map.of("loggedIn", true, "username", profile.getUsername()));
        } else {
            return ResponseEntity.ok(Map.of("loggedIn", false));
        }
    }


    // 로그아웃 API: POST /user/logout
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletResponse response, Authentication authentication) {
        // 인증되지 않은 상태일 경우 401 UNAUTHORIZED 반환
        if (!userService.isAuthenticated(authentication)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        // 사용자 서비스 레이어를 통해 로그아웃 처리
        String email = authentication.getName();
        userService.logout(email);

        // JSESSIONID, accessToken, refreshToken 쿠키 만료 처리
        CookieUtils.clearCookies(response, "JSESSIONID", "accessToken", "refreshToken");

        return new ResponseEntity<>("로그아웃 성공", HttpStatus.OK);
    }


    // 회원정보 조회 API: GET /user/profile
    @GetMapping("/profile")
    public ResponseEntity<ProfileResponseDTO> getProfile(Authentication authentication) {
        if (!userService.isAuthenticated(authentication)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        String email = authentication.getName();
        ProfileResponseDTO profileDTO = userService.getProfileByEmail(email);
        return new ResponseEntity<>(profileDTO, HttpStatus.OK);
    }


    // 회원정보 수정 API: PATCH /user/profile
    @PatchMapping("/profile")
    public ResponseEntity<ProfileResponseDTO> updateProfile(@Valid @RequestBody ProfileUpdateRequestDTO updateRequestDTO) {
        // JwtAuthenticationFilter를 통해 SecurityContext에 설정된 Authentication 객체를 사용
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!userService.isAuthenticated(auth)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        String email = auth.getName();
        ProfileResponseDTO profileDTO = userService.updateProfile(email, updateRequestDTO);
        return new ResponseEntity<>(profileDTO, HttpStatus.OK);
    }


    // 회원탈퇴 API: DELETE /user/deleteAccount
    @DeleteMapping("/deleteAccount")
    public ResponseEntity<String> deleteAccount(@Valid @RequestBody DeleteAccountRequestDTO deleteAccountRequestDTO,
                                                HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!userService.isAuthenticated(auth)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        String email = auth.getName();
        userService.deleteAccount(email, deleteAccountRequestDTO);
        CookieUtils.clearCookies(response, "JSESSIONID", "accessToken", "refreshToken");
        return new ResponseEntity<>("회원 탈퇴 성공", HttpStatus.OK);
    }
}
