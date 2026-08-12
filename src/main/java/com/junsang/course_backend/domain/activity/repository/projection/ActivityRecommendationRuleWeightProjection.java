package com.junsang.course_backend.domain.activity.repository.projection;

public record ActivityRecommendationRuleWeightProjection(
        Long preferenceOptionId,
        Long activityCategoryId,
        int weight
) {
}
