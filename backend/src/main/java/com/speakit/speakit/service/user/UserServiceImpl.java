package com.speakit.speakit.service.user;

import com.speakit.speakit.dto.user.*;
import com.speakit.speakit.exception.MultiErrorException;
import com.speakit.speakit.model.user.User;
import com.speakit.speakit.repository.user.UserRepository;
import com.speakit.speakit.security.JwtTokenProvider;
import com.speakit.speakit.util.PasswordPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * UserServiceImpl 클래스는 UserService 인터페이스에 정의된 회원 관련 비즈니스 로직을 구현합니다.
 *
 * - 회원가입: 이메일 중복 체크, 비밀번호 조건 검증(PasswordPolicy 사용), 사용자 정보 저장
 * - 로그인: AuthenticationManager를 통해 인증 수행 후, SecurityContextHolder에 인증 정보 저장 및 사용자 정보 반환
 * - 회원정보 조회: 이메일 기반으로 사용자 정보를 조회하며, 가입일은 'yyyy-MM-dd' 형식으로 포맷하여 반환
 * - 회원정보 수정: 이름 또는 비밀번호를 선택적으로 수정
 * - 회원 탈퇴: 이메일 기반으로 사용자 계정을 삭제
 *
 */
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    public UserServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           AuthenticationManager authenticationManager,
                           JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
    }


    @Override
    public SignUpResponseDTO signUp(SignUpRequestDTO signUpRequestDTO) {
        List<String> errors = new ArrayList<>();

        // 이메일 중복 체크
        if (userRepository.findByEmail(signUpRequestDTO.getEmail()) != null) {
            errors.add("사용할 수 없는 이메일입니다. 다른 이메일을 입력해 주세요.");
        }
        // 비밀번호 검증: PasswordPolicy를 활용하여 체크
        errors.addAll(PasswordPolicy.validate(signUpRequestDTO.getPassword()));

        if (!errors.isEmpty()) {
            throw new MultiErrorException(errors);
        }

        User user = User.builder()
                .username(signUpRequestDTO.getUsername())
                .email(signUpRequestDTO.getEmail())
                .password(passwordEncoder.encode(signUpRequestDTO.getPassword()))
                .createdAt(LocalDateTime.now())
                .role("ROLE_USER")
                .build();
        User savedUser = userRepository.save(user);

        return SignUpResponseDTO.builder()
                .id(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .createdAt(savedUser.getCreatedAt())
                .build();
    }


    @Override
    public SignInResponseDTO signIn(SignInRequestDTO signInRequestDTO) {
        // 인증 객체 생성 후 인증 수행 (예외 발생 시 AuthenticationException)
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(signInRequestDTO.getEmail(), signInRequestDTO.getPassword());
        Authentication authentication = authenticationManager.authenticate(authToken);

        // JWT 토큰 생성 (access token과 refresh token)
        String accessToken = jwtTokenProvider.generateAccessToken(authentication);
        String refreshToken = jwtTokenProvider.generateRefreshToken(authentication);

        // DB에서 사용자 정보 조회 및 refresh token 저장
        User user = userRepository.findByEmail(signInRequestDTO.getEmail());
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        user.setRefreshToken(refreshToken);
        userRepository.save(user);

        return SignInResponseDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .createdAt(user.getCreatedAt())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }


    @Override
    public ProfileResponseDTO getProfileByEmail(String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new RuntimeException("회원 정보를 찾을 수 없습니다.");
        }
        // 가입일(LocalDateTime)에서 년월일만 추출 (예: "2025-03-25")
        String registrationDate = user.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        return ProfileResponseDTO.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .password(user.getPassword())
                .registrationDate(registrationDate)
                .build();
    }


    @Override
    public ProfileResponseDTO updateProfile(String email, ProfileUpdateRequestDTO updateRequestDTO) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new RuntimeException("회원 정보를 찾을 수 없습니다.");
        }
        // 현재 비밀번호 검증
        if (!passwordEncoder.matches(updateRequestDTO.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("현재 비밀번호가 올바르지 않습니다.");
        }

        // 이름과 비밀번호 업데이트 여부를 판단
        boolean updated = updateUserFields(user, updateRequestDTO);

        if (updated) {
            userRepository.save(user);
        }
        return getProfileByEmail(email);
    }


    @Override
    public void deleteAccount(String email, DeleteAccountRequestDTO deleteAccountRequestDTO) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new RuntimeException("회원 정보를 찾을 수 없습니다.");
        }

        // 현재 비밀번호 검증
        if (!passwordEncoder.matches(deleteAccountRequestDTO.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("현재 비밀번호가 올바르지 않습니다.");
        }
        userRepository.delete(user);
    }


    @Override
    public void logout(String email) {
        User user = userRepository.findByEmail(email);
        if (user != null) {
            // refresh token 제거로 재발급 방지
            user.setRefreshToken(null);
            userRepository.save(user);
        }
    }


    /**
     * updateUserFields는 ProfileUpdateRequestDTO에 담긴 값을 확인하여,
     * User 엔티티의 이름과 비밀번호를 업데이트합니다.
     * 업데이트된 항목이 있으면 true, 그렇지 않으면 false를 반환합니다.
     */
    private boolean updateUserFields(User user, ProfileUpdateRequestDTO updateRequestDTO) {
        boolean updated = false;
        // 이름 업데이트: 값이 존재하면 업데이트
        if (updateRequestDTO.getUsername() != null && !updateRequestDTO.getUsername().isBlank()) {
            user.setUsername(updateRequestDTO.getUsername());
            updated = true;
        }
        // 새로운 비밀번호 업데이트: 값이 존재하면 검증 후 업데이트
        if (updateRequestDTO.getNewPassword() != null && !updateRequestDTO.getNewPassword().isBlank()) {
            List<String> errors = PasswordPolicy.validate(updateRequestDTO.getNewPassword());
            if (!errors.isEmpty()) {
                throw new MultiErrorException(errors);
            }
            user.setPassword(passwordEncoder.encode(updateRequestDTO.getNewPassword()));
            updated = true;
        }
        return updated;
    }
}
