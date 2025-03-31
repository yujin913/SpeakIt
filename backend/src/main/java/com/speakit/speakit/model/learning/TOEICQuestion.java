package com.speakit.speakit.model.learning;

import jakarta.persistence.*;

// 토익 스피킹 기출문제 엔티티
@Entity
@Table(name = "toeic_questions")
public class TOEICQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 2000)
    private String questionText;

    // 필요에 따라 카테고리나 파트 정보를 추가할 수 있음

    // 기본 생성자, getter, setter 생략
}
