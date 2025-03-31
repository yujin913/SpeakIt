package com.speakit.speakit.model.learning.survey;

import jakarta.persistence.*;
import java.util.List;

// 오픽 사전 background survey 엔티티
@Entity
@Table(name = "surveys")
public class Survey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 예: "OPIc Pre-survey"
    private String title;

    @OneToMany(mappedBy = "survey", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SurveyPart> parts;

    // 기본 생성자, getter, setter 생략
}
