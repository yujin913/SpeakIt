package com.speakit.speakit.model.learning.survey;

import com.speakit.speakit.model.user.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

// 사용자가 제출한 설문 응답 엔티티
@Entity
@Table(name = "survey_responses")
public class SurveyResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 응답한 사용자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // 응답한 설문
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_id")
    private Survey survey;

    private LocalDateTime submittedAt;

    @OneToMany(mappedBy = "surveyResponse", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SurveyAnswer> answers;

    // 기본 생성자, getter, setter 생략
}
