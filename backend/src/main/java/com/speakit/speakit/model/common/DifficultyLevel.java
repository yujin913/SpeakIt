package com.speakit.speakit.model.common;

// 난이도 구분 열거형
public enum DifficultyLevel {
    ADVANCED_HIGH("Advanced High"),
    ADVANCED_MID("Advanced Mid"),
    ADVANCED_LOW("Advanced Low"),
    INTERMEDIATE_HIGH("Intermediate High"),
    INTERMEDIATE_MID("Intermediate Mid"),
    INTERMEDIATE_LOW("Intermediate Low"),
    NOVICE_HIGH("Novice High"),
    NOVICE_MID("Novice Mid"),
    NOVICE_LOW("Novice Low");

    private final String label;

    DifficultyLevel(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
