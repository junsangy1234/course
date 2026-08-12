package com.junsang.course_backend.domain.activity.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record UpdateActivityRecommendationRulesRequest(
        @NotEmpty List<@Valid UpdateActivityRecommendationRuleWeightRequest> rules
) {
}
