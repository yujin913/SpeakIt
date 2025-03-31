package com.speakit.speakit.model.learning;

import jakarta.persistence.*;
import java.time.LocalDateTime;

// 학습 세션 내의 대화 기록 (사용자/AI 메시지, 음성 인식 결과, 피드백 등) 엔티티
@Entity
@Table(name = "dialogues")
public class Dialogue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sender; // "USER" 또는 "AI"

    @Column(length = 2000)
    private String message;  // 항상 텍스트 기록

    private LocalDateTime timestamp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "practice_session_id")
    private PracticeSession practiceSession;

    @Column(length = 2000)
    private String transcription;  // 음성 인식 결과 텍스트

    @Column(length = 2000)
    private String feedback;       // AI 피드백 메시지

    // 기본 생성자, getter, setter 생략
}
