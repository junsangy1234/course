package com.junsang.course_backend.domain.activity.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateActivityRecommendationRuleWeightRequest(
        @NotNull Long preferenceOptionId,
        @NotNull Long activityCategoryId,
        @NotNull @Min(-100) @Max(100) Integer weight
) {
}
