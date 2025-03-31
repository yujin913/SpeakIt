package com.speakit.speakit.model.chat;

import com.speakit.speakit.model.common.ChatRoomType;
import com.speakit.speakit.model.common.ExamType;
import com.speakit.speakit.model.user.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

// 채팅방 정보 (채팅방 이름, 타입, 시험종류, 참여자 목록) 엔티티
@Entity
@Table(name = "chat_rooms")
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String roomName;

    @Enumerated(EnumType.STRING)
    private ChatRoomType roomType;  // GROUP 또는 PRIVATE

    // 단체채팅의 경우 시험별 그룹 채팅 지원 (개인 채팅은 null)
    @Enumerated(EnumType.STRING)
    private ExamType examType;

    private LocalDateTime createdAt;

    @ManyToMany
    @JoinTable(
            name = "chat_room_participants",
            joinColumns = @JoinColumn(name = "chat_room_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> participants = new HashSet<>();

    // 기본 생성자, getter, setter 생략
}
