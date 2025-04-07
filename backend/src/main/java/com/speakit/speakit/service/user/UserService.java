package com.speakit.speakit.service.user;

import com.speakit.speakit.dto.user.*;

/**
 * UserService 인터페이스는 회원 관련 비즈니스 로직에 대한 계약(Contract)을 정의합니다.
 * - 회원가입, 로그인, 회원정보 조회, 회원정보 수정, 회원 탈퇴 등의 기능을 포함합니다.
 * - 이는 컨트롤러에서 호출되어 실제 구현체(UserServiceImpl)에서 처리됩니다.
 */
public interface UserService {

    SignUpResponseDTO signUp(SignUpRequestDTO signUpRequestDTO);
    SignInResponseDTO signIn(SignInRequestDTO signInRequestDTO);
    ProfileResponseDTO getProfileByEmail(String email);
    ProfileResponseDTO updateProfile(String email, ProfileUpdateRequestDTO updateRequestDTO);
    void deleteAccount(String email, DeleteAccountRequestDTO deleteAccountRequestDTO);

    // JWT 로그아웃 시 refresh token을 제거하기 위한 메서드 추가
    void logout(String email);
}
