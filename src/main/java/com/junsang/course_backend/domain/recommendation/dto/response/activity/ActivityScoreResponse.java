package com.junsang.course_backend.domain.recommendation.dto.response.activity;

import com.junsang.course_backend.domain.recommendation.service.ActivityScore;

public record ActivityScoreResponse(
        Long activityCategoryId,
        String code,
        String name,
        int score
) {
    public static ActivityScoreResponse from(ActivityScore activityScore) {
        return new ActivityScoreResponse(
                activityScore.activityCategoryId(),
                activityScore.code(),
                activityScore.name(),
                activityScore.score()
        );
    }
}
