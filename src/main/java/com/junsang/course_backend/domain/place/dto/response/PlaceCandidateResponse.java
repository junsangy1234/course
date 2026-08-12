package com.junsang.course_backend.domain.place.dto.response;

import com.junsang.course_backend.domain.place.entity.PlaceProvider;
import com.junsang.course_backend.domain.place.entity.PlaceType;
import com.junsang.course_backend.infra.kakao.dto.response.KakaoPlaceDocument;
import java.math.BigDecimal;

public record PlaceCandidateResponse(
        PlaceProvider provider,
        String providerPlaceId,
        PlaceType type,
        String name,
        String categoryName,
        String addressName,
        String roadAddressName,
        BigDecimal latitude,
        BigDecimal longitude,
        String placeUrl,
        Integer distanceMeters
) {
    public static PlaceCandidateResponse from(
            KakaoPlaceDocument document,
            PlaceType type,
            Integer distanceMeters
    ) {
        return new PlaceCandidateResponse(
                PlaceProvider.KAKAO,
                document.id(),
                type,
                document.placeName(),
                document.categoryName(),
                document.addressName(),
                document.roadAddressName(),
                document.y(),
                document.x(),
                document.placeUrl(),
                distanceMeters
        );
    }
}
