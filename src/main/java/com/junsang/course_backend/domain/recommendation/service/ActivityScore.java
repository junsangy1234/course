package com.junsang.course_backend.domain.recommendation.service;

public record ActivityScore(
        Long activityCategoryId,
        String code,
        String name,
        int score
) {
}
