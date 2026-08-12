package com.junsang.course_backend.domain.recommendation.dto.response.courseanchor;

import com.junsang.course_backend.domain.recommendation.dto.response.activity.ActivityScoreResponse;
import java.util.List;

public record CourseAnchorRecommendationResponse(
        String regionName,
        List<ActivityScoreResponse> activityScores,
        List<CourseAnchorCandidateResponse> courseAnchors
) {
    public static CourseAnchorRecommendationResponse of(
            String regionName,
            List<ActivityScoreResponse> activityScores,
            List<CourseAnchorCandidateResponse> courseAnchors
    ) {
        return new CourseAnchorRecommendationResponse(regionName, activityScores, courseAnchors);
    }
}
