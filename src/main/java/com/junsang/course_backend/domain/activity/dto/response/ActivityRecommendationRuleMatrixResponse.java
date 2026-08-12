package com.junsang.course_backend.domain.activity.dto.response;

import java.util.List;

public record ActivityRecommendationRuleMatrixResponse(
        List<ActivityCategoryResponse> activityCategories,
        List<PreferenceGroupResponse> preferenceGroups
) {
    public static ActivityRecommendationRuleMatrixResponse of(
            List<ActivityCategoryResponse> activityCategories,
            List<PreferenceGroupResponse> preferenceGroups
    ) {
        return new ActivityRecommendationRuleMatrixResponse(activityCategories, preferenceGroups);
    }
}
