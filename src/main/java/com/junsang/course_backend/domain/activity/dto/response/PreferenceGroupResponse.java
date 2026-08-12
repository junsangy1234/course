package com.junsang.course_backend.domain.activity.dto.response;

import com.junsang.course_backend.domain.preference.entity.PreferenceGroup;
import java.util.List;

public record PreferenceGroupResponse(
        Long id,
        String code,
        String name,
        boolean allowsMultipleSelection,
        List<PreferenceOptionResponse> options
) {
    public static PreferenceGroupResponse from(
            PreferenceGroup preferenceGroup,
            List<PreferenceOptionResponse> options
    ) {
        return new PreferenceGroupResponse(
                preferenceGroup.getId(),
                preferenceGroup.getCode(),
                preferenceGroup.getName(),
                preferenceGroup.isAllowsMultipleSelection(),
                options
        );
    }
}
