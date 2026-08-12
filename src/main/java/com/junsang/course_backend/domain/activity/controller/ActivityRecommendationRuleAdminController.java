package com.junsang.course_backend.domain.activity.controller;

import com.junsang.course_backend.domain.activity.dto.request.UpdateActivityRecommendationRulesRequest;
import com.junsang.course_backend.domain.activity.dto.response.ActivityRecommendationRuleMatrixResponse;
import com.junsang.course_backend.domain.activity.service.ActivityRecommendationRuleAdminService;
import com.junsang.course_backend.global.security.LocalAdminAccessVerifier;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/activity-recommendation-rules")
@RequiredArgsConstructor
public class ActivityRecommendationRuleAdminController {

    private final ActivityRecommendationRuleAdminService activityRecommendationRuleAdminService;
    private final LocalAdminAccessVerifier localAdminAccessVerifier;

    @GetMapping
    public ActivityRecommendationRuleMatrixResponse getRules(HttpServletRequest request) {
        localAdminAccessVerifier.verify(request);
        return activityRecommendationRuleAdminService.getMatrix();
    }

    @PutMapping
    public ActivityRecommendationRuleMatrixResponse updateRules(
            HttpServletRequest request,
            @Valid @RequestBody UpdateActivityRecommendationRulesRequest updateRequest
    ) {
        localAdminAccessVerifier.verify(request);
        return activityRecommendationRuleAdminService.updateRules(updateRequest);
    }
}
