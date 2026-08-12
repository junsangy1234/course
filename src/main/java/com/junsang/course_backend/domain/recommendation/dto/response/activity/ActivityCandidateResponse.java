package com.junsang.course_backend.domain.recommendation.dto.response.activity;

import com.junsang.course_backend.domain.recommendation.service.ActivityScore;

public record ActivityCandidateResponse(
        Long id,
        String code,
        String name,
        int score
) {
    public static ActivityCandidateResponse from(ActivityScore activityScore) {
        return new ActivityCandidateResponse(
                activityScore.activityCategoryId(),
                activityScore.code(),
                activityScore.name(),
                activityScore.score()
        );
    }
}
