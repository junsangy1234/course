package com.junsang.course_backend.domain.activity.dto.response;

import com.junsang.course_backend.domain.activity.entity.ActivityCategory;

public record ActivityCategoryResponse(Long id, String code, String name) {
    public static ActivityCategoryResponse from(ActivityCategory activityCategory) {
        return new ActivityCategoryResponse(
                activityCategory.getId(),
                activityCategory.getCode(),
                activityCategory.getName()
        );
    }
}
