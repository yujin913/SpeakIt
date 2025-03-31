package com.speakit.speakit.model.learning.survey;

import jakarta.persistence.*;
import java.util.List;

// 파트별 survey 엔티티
@Entity
@Table(name = "survey_parts")
public class SurveyPart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 예: "Part 1", "Part 2", ...
    private String partName;

    // 파트 순서를 지정할 필요가 있다면 사용 (예: 1, 2, …)
    private Integer partOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_id")
    private Survey survey;

    @OneToMany(mappedBy = "surveyPart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SurveyQuestion> questions;

    // 기본 생성자, getter, setter 생략
}
