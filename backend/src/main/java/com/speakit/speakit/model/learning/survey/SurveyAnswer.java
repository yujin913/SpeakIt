package com.speakit.speakit.model.learning.survey;

import jakarta.persistence.*;
import java.util.Set;

@Entity
@Table(name = "survey_answers")
public class SurveyAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 응답한 질문
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_question_id")
    private SurveyQuestion surveyQuestion;

    // 응답이 속한 설문 응답
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_response_id")
    private SurveyResponse surveyResponse;

    // 사용자가 선택한 옵션 (단일 선택의 경우에도 Set으로 처리 가능)
    @ManyToMany
    @JoinTable(name = "survey_answer_options",
            joinColumns = @JoinColumn(name = "survey_answer_id"),
            inverseJoinColumns = @JoinColumn(name = "survey_option_id"))
    private Set<SurveyOption> selectedOptions;

    // 기본 생성자, getter, setter 생략
}
