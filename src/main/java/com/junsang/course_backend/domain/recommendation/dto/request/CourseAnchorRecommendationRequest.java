package com.junsang.course_backend.domain.recommendation.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record CourseAnchorRecommendationRequest(
        @NotBlank String regionName,
        @NotNull List<@NotNull Long> preferenceOptionIds,
        @Min(1) @Max(20) Integer size
) {
}
