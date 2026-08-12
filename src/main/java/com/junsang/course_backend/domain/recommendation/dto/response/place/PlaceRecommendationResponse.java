package com.junsang.course_backend.domain.recommendation.dto.response.place;

import com.junsang.course_backend.domain.recommendation.dto.response.activity.ActivityCandidateResponse;
import java.util.List;

public record PlaceRecommendationResponse(
        ActivityCandidateResponse selectedActivity,
        List<PlaceRecommendationCandidateResponse> places
) {
    public static PlaceRecommendationResponse of(
            ActivityCandidateResponse selectedActivity,
            List<PlaceRecommendationCandidateResponse> places
    ) {
        return new PlaceRecommendationResponse(selectedActivity, places);
    }
}
