package com.junsang.course_backend.domain.activity.dto.response;

import com.junsang.course_backend.domain.preference.entity.PreferenceOption;
import java.util.List;

public record PreferenceOptionResponse(
        Long id,
        String code,
        String name,
        List<ActivityWeightResponse> activityWeights
) {
    public static PreferenceOptionResponse from(
            PreferenceOption preferenceOption,
            List<ActivityWeightResponse> activityWeights
    ) {
        return new PreferenceOptionResponse(
                preferenceOption.getId(),
                preferenceOption.getCode(),
                preferenceOption.getName(),
                activityWeights
        );
    }
}
