package com.speakit.speakit.model.learning.survey;

import jakarta.persistence.*;
import java.util.List;

// survey 질문 저장 엔티티
@Entity
@Table(name = "survey_questions")
public class SurveyQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 첨부 파일에 나온 질문 텍스트 (예: "현재 귀하는 어느 분야에 종사하고 계십니까?")
    @Column(length = 2000)
    private String questionText;

    // "SINGLE" 또는 "MULTIPLE" 값으로 저장 (예: Part 4의 일부 질문은 다중 선택)
    @Column(length = 50)
    private String questionType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_part_id")
    private SurveyPart surveyPart;

    @OneToMany(mappedBy = "surveyQuestion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SurveyOption> options;

    // 기본 생성자, getter, setter 생략
}
