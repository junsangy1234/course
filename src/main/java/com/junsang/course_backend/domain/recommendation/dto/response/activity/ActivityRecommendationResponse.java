package com.junsang.course_backend.domain.recommendation.dto.response.activity;

import java.util.List;

public record ActivityRecommendationResponse(
        List<ActivityCandidateResponse> activities
) {
    public static ActivityRecommendationResponse of(List<ActivityCandidateResponse> activities) {
        return new ActivityRecommendationResponse(activities);
    }
}
