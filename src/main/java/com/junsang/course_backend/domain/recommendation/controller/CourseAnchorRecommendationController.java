package com.junsang.course_backend.domain.recommendation.controller;

import com.junsang.course_backend.domain.recommendation.dto.request.CourseAnchorRecommendationRequest;
import com.junsang.course_backend.domain.recommendation.dto.response.courseanchor.CourseAnchorRecommendationResponse;
import com.junsang.course_backend.domain.recommendation.service.CourseAnchorRecommendationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/course-anchors")
@RequiredArgsConstructor
public class CourseAnchorRecommendationController {

    private final CourseAnchorRecommendationService courseAnchorRecommendationService;

    @PostMapping("/recommendations")
    public CourseAnchorRecommendationResponse recommend(
            @Valid @RequestBody CourseAnchorRecommendationRequest request
    ) {
        return courseAnchorRecommendationService.recommendCourseAnchor(request);
    }
}
