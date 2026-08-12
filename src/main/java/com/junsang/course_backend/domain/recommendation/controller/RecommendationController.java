package com.junsang.course_backend.domain.recommendation.controller;

import com.junsang.course_backend.domain.activity.dto.response.ActivityRecommendationRuleMatrixResponse;
import com.junsang.course_backend.domain.activity.service.ActivityRecommendationRuleAdminService;
import com.junsang.course_backend.domain.recommendation.dto.request.ActivityRecommendationRequest;
import com.junsang.course_backend.domain.recommendation.dto.request.PlaceRecommendationRequest;
import com.junsang.course_backend.domain.recommendation.dto.response.activity.ActivityRecommendationResponse;
import com.junsang.course_backend.domain.recommendation.dto.response.place.PlaceRecommendationResponse;
import com.junsang.course_backend.domain.recommendation.service.RecommendationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final ActivityRecommendationRuleAdminService activityRecommendationRuleAdminService;
    private final RecommendationService recommendationService;

    @GetMapping("/config")
    public ActivityRecommendationRuleMatrixResponse getConfiguration() {
        return activityRecommendationRuleAdminService.getMatrix();
    }

    @PostMapping("/activities")
    public ActivityRecommendationResponse recommendActivities(
            @Valid @RequestBody ActivityRecommendationRequest request
    ) {
        return recommendationService.recommendActivities(request.preferenceOptionIds());
    }

    @PostMapping("/places")
    public PlaceRecommendationResponse recommendPlaces(
            @Valid @RequestBody PlaceRecommendationRequest request
    ) {
        return recommendationService.recommendPlaces(request);
    }
}
