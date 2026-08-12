package com.junsang.course_backend.domain.activity.dto.response;

public record ActivityWeightResponse(Long activityCategoryId, int weight) {
    public static ActivityWeightResponse of(Long activityCategoryId, int weight) {
        return new ActivityWeightResponse(activityCategoryId, weight);
    }
}
