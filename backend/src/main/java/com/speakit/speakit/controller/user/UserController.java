package com.speakit.speakit.controller.user;

import com.speakit.speakit.dto.user.*;
import com.speakit.speakit.service.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }


    // 회원가입 API: POST /user/signUp
    @PostMapping("/signUp")
    public ResponseEntity<SignUpResponseDTO> signUp(@Valid @RequestBody SignUpRequestDTO signUpRequestDTO) {
        SignUpResponseDTO responseDTO = userService.signUp(signUpRequestDTO);
        return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);
    }


    // 로그인 API: POST /user/signIn
    @PostMapping("/signIn")
    public ResponseEntity<SignInResponseDTO> signIn(@Valid @RequestBody SignInRequestDTO signInRequestDTO,
                                                    HttpServletRequest request) {
        SignInResponseDTO responseDTO = userService.signIn(signInRequestDTO);
        // 로그인 성공 후, SecurityContext를 세션에 저장
        request.getSession(true).setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }


    // 로그아웃 API: POST /user/logout
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpSession session) {
        SecurityContextHolder.clearContext();
        return new ResponseEntity<>("로그아웃 성공", HttpStatus.OK);
    }


    // 회원정보 조회 API: GET /user/profile
    @GetMapping("/profile")
    public ResponseEntity<ProfileResponseDTO> getProfile() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        String email = auth.getName();
        ProfileResponseDTO profileDTO = userService.getProfileByEmail(email);
        return new ResponseEntity<>(profileDTO, HttpStatus.OK);
    }


    // 회원정보 수정 API: PATCH /user/profile
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
    public ResponseEntity<String> deleteAccount(@Valid @RequestBody DeleteAccountRequestDTO deleteAccountRequestDTO) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        String email = auth.getName();
        userService.deleteAccount(email, deleteAccountRequestDTO);
        SecurityContextHolder.clearContext();
        return new ResponseEntity<>("회원 탈퇴 성공", HttpStatus.OK);
    }
}
