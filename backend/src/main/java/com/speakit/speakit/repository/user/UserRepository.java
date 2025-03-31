package com.speakit.speakit.repository.user;

import com.speakit.speakit.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    // 이메일로 사용자 정보를 조회하는 메서드
    User findByEmail(String email);
}
