package com.speakit.speakit.model.learning.survey;

import jakarta.persistence.*;

// 각 질문에 대한 선택지 엔티티
@Entity
@Table(name = "survey_options")
public class SurveyOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 선택지 텍스트 (예: "사업/회사")
    @Column(length = 500)
    private String optionText;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_question_id")
    private SurveyQuestion surveyQuestion;

    // 기본 생성자, getter, setter 생략
}
