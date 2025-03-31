package com.speakit.speakit.model.chat;

import com.speakit.speakit.model.user.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;

// 채팅 메시지 정보 (채팅방, 누가, 언제, 메시지 내용) 엔티티
@Entity
@Table(name = "chat_messages")
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id")
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    private User sender;

    @Column(length = 2000)
    private String message;

    private LocalDateTime timestamp;

    // 기본 생성자, getter, setter 생략
}
