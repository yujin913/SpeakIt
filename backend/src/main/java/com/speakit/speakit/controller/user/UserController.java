package com.speakit.speakit.controller.user;

import com.speakit.speakit.dto.user.*;
import com.speakit.speakit.security.JwtTokenProvider;
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


    // 로그인 API: POST /user/signIn
    @PostMapping("/signIn")
    public ResponseEntity<?> signIn(@Valid @RequestBody SignInRequestDTO signInRequestDTO, HttpServletResponse response) {
        // 인증을 수행하고 JWT 토큰을 포함한 응답 DTO를 받음
        SignInResponseDTO responseDTO = userService.signIn(signInRequestDTO);

        // JwtTokenProvider에서 Duration 타입의 만료 시간을 직접 가져와 CookieUtils.setAuthCookies에 전달합니다.
        CookieUtils.setAuthCookies(response,
                responseDTO.getAccessToken(),
                responseDTO.getRefreshToken(),
                jwtTokenProvider.getAccessTokenExpiration(),
                jwtTokenProvider.getRefreshTokenExpiration());

        // JWT 토큰은 HttpOnly 쿠키에 저장되므로 클라이언트에서 직접 접근 불가.
        // 여기서는 사용자 정보만 반환합니다.
        SignInResponseDTO minimalResponse = SignInResponseDTO.builder()
                .id(responseDTO.getId())
                .username(responseDTO.getUsername())
                .email(responseDTO.getEmail())
                .createdAt(responseDTO.getCreatedAt())
                .build();

        return new ResponseEntity<>(minimalResponse, HttpStatus.OK);
    }


    /**
     * GET /user/signIn 엔드포인트: 클라이언트가 로그인 페이지에 접근할 때,
     * HttpOnly 쿠키에 "accessToken"이 존재하고 유효하면 메인 페이지로 자동 리다이렉트합니다.
     */
    @GetMapping("/signIn")
    public void getSignIn(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String token = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("accessToken".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }
        if (token != null && jwtTokenProvider.validateToken(token)) {
            // 이미 유효한 JWT 토큰이 있으면 메인 페이지로 리다이렉트
            response.sendRedirect("http://localhost:3000");
        } else {
            // 토큰이 없거나 유효하지 않으면 클라이언트에서 로그인 페이지를 보여주도록 합니다.
            // REST API 환경에서는 별도의 로그인 페이지를 서버에서 렌더링하지 않고,
            // 클라이언트 라우팅을 통해 로그인 UI를 표시하는 경우가 많으므로,
            // 여기서는 200 OK 응답을 보내거나 아무 응답 없이 끝내도 됩니다.
            response.setStatus(HttpServletResponse.SC_OK);
        }
    }


    // JWT 기반 로그아웃 API: Authorization 헤더에 토큰을 포함한 상태에서 호출
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        String email = authentication.getName();
        userService.logout(email);
        SecurityContextHolder.clearContext();

        // JSESSIONID, accessToken, refreshToken 쿠키 만료 처리
        CookieUtils.clearCookies(response, "JSESSIONID", "accessToken", "refreshToken");

        return new ResponseEntity<>("로그아웃 성공", HttpStatus.OK);
    }


    // JWT 기반 회원정보 조회 API: Authorization 헤더에 토큰을 포함한 상태에서 호출
    @GetMapping("/profile")
    public ResponseEntity<ProfileResponseDTO> getProfile(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        String email = authentication.getName();
        ProfileResponseDTO profileDTO = userService.getProfileByEmail(email);
        return new ResponseEntity<>(profileDTO, HttpStatus.OK);
    }


    // 회원정보 조회 및 수정 API: PATCH /user/profile
    @PatchMapping("/profile")
    public ResponseEntity<ProfileResponseDTO> updateProfile(@Valid @RequestBody ProfileUpdateRequestDTO updateRequestDTO) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
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
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        String email = auth.getName();
        userService.deleteAccount(email, deleteAccountRequestDTO);
        SecurityContextHolder.clearContext();
        CookieUtils.clearCookies(response, "JSESSIONID", "accessToken", "refreshToken");
        return new ResponseEntity<>("회원 탈퇴 성공", HttpStatus.OK);
    }


    // 로그인 상태 조회 API: GET /user/loginStatus
    @GetMapping("/loginStatus")
    public ResponseEntity<?> loginStatus(Authentication authentication) {
        // Authentication 객체가 null이 아니고, 인증된 사용자이며, "anonymousUser"가 아닐 경우
        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal())) {
            // 추가로 필요한 정보가 있으면 authentication.getName() 등으로 제공할 수 있음
            ProfileResponseDTO profile = userService.getProfileByEmail(authentication.getName());
            return ResponseEntity.ok(Map.of("loggedIn", true, "username", profile.getUsername()));
        } else {
            return ResponseEntity.ok(Map.of("loggedIn", false));
        }
    }
}
