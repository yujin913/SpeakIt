package com.speakit.speakit.model.learning;

import com.speakit.speakit.model.common.DifficultyLevel;
import com.speakit.speakit.model.common.ExamType;
import com.speakit.speakit.model.user.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// 학습 세션 정보 엔티티 (시험 종류, 시작/종료 시간, 주제, 난이도, 생성된 대본 등)
@Entity
@Table(name = "practice_sessions")
public class PracticeSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @Enumerated(EnumType.STRING)
    private ExamType examType; // TOEIC_SPEAKING 또는 OPIc

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    // OPIc 관련 주제: 최대 3가지 주제를 저장 (TOEIC인 경우 null)
    @ElementCollection
    @CollectionTable(name = "opic_topics", joinColumns = @JoinColumn(name = "practice_session_id"))
    @Column(name = "topic")
    private List<String> topics = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private DifficultyLevel difficultyLevel;  // ENUM 타입

    @Column(length = 2000)
    private String generatedScript;  // ChatGPT API로 생성된 대본

    // TOEIC 전용: 기출문제와 연관 (OPIc인 경우 null)
    @ManyToOne(fetch = FetchType.LAZY)
    private TOEICQuestion toeicQuestion;

    @OneToMany(mappedBy = "practiceSession", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Dialogue> dialogues = new ArrayList<>();

    // 기본 생성자
    public PracticeSession() {
    }

    // 필요한 생성자, getter 및 setter 추가
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public ExamType getExamType() {
        return examType;
    }

    public void setExamType(ExamType examType) {
        this.examType = examType;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public List<String> getTopics() {
        return topics;
    }

    public void setTopics(List<String> topics) {
        this.topics = topics;
    }

    public DifficultyLevel getDifficultyLevel() {
        return difficultyLevel;
    }

    public void setDifficultyLevel(DifficultyLevel difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }

    public String getGeneratedScript() {
        return generatedScript;
    }

    public void setGeneratedScript(String generatedScript) {
        this.generatedScript = generatedScript;
    }

    public TOEICQuestion getToeicQuestion() {
        return toeicQuestion;
    }

    public void setToeicQuestion(TOEICQuestion toeicQuestion) {
        this.toeicQuestion = toeicQuestion;
    }

    public List<Dialogue> getDialogues() {
        return dialogues;
    }

    public void setDialogues(List<Dialogue> dialogues) {
        this.dialogues = dialogues;
    }
}
