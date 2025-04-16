package com.speakit.speakit.service.user;

import com.speakit.speakit.dto.user.*;
import org.springframework.security.core.Authentication;

// 일반 회원 관련 비즈니스 로직 인터페이스
public interface UserService {

    SignUpResponseDTO signUp(SignUpRequestDTO signUpRequestDTO);
    SignInResponseDTO signIn(SignInRequestDTO signInRequestDTO);

    ProfileResponseDTO getProfileByEmail(String email);
    ProfileResponseDTO updateProfile(String email, ProfileUpdateRequestDTO updateRequestDTO);

    void deleteAccount(String email, DeleteAccountRequestDTO deleteAccountRequestDTO);
    void logout(String email);

    boolean isAuthenticated(Authentication authentication);
}
