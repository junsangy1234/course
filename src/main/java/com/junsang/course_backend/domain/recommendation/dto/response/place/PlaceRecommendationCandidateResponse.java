package com.junsang.course_backend.domain.recommendation.dto.response.place;

import com.junsang.course_backend.infra.kakao.dto.response.KakaoPlaceDocument;
import java.math.BigDecimal;

public record PlaceRecommendationCandidateResponse(
        String providerPlaceId,
        String name,
        String categoryName,
        String addressName,
        String roadAddressName,
        BigDecimal latitude,
        BigDecimal longitude,
        String placeUrl,
        Integer distanceMeters,
        int activityScore,
        int distanceScore,
        int finalScore
) {
    public static PlaceRecommendationCandidateResponse from(
            KakaoPlaceDocument document,
            Integer distanceMeters,
            int activityScore,
            int distanceScore
    ) {
        return new PlaceRecommendationCandidateResponse(
                document.id(),
                document.placeName(),
                document.categoryName(),
                document.addressName(),
                document.roadAddressName(),
                document.y(),
                document.x(),
                document.placeUrl(),
                distanceMeters,
                activityScore,
                distanceScore,
                activityScore + distanceScore
        );
    }
}
