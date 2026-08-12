package com.junsang.course_backend.domain.recommendation.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public record ActivityRecommendationRequest(
        @NotNull List<@NotNull Long> preferenceOptionIds
) {
}
