package com.junsang.course_backend.domain.place.service;

import com.junsang.course_backend.domain.place.dto.response.PlaceCandidateResponse;
import com.junsang.course_backend.domain.place.entity.PlaceType;
import com.junsang.course_backend.infra.kakao.KakaoLocalClient;
import com.junsang.course_backend.infra.kakao.dto.request.KakaoKeywordSearchRequest;
import com.junsang.course_backend.infra.kakao.dto.response.KakaoKeywordSearchResponse;
import com.junsang.course_backend.infra.kakao.dto.response.KakaoPlaceDocument;
import java.math.BigDecimal;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlaceSearchService {

    private final KakaoLocalClient kakaoLocalClient;

    // ── 장소 후보 검색 ────────────────────────────────────────────────────
    public List<PlaceCandidateResponse> searchCandidates(
            String query,
            PlaceType type,
            BigDecimal latitude,
            BigDecimal longitude,
            int radius,
            int size
    ) {
        KakaoKeywordSearchResponse response = kakaoLocalClient.searchKeyword(
                new KakaoKeywordSearchRequest(query, longitude, latitude, radius, size)
        );

        List<KakaoPlaceDocument> documents = response.documents() == null ? List.of() : response.documents();

        return documents.stream()
                .map(document -> PlaceCandidateResponse.from(document, type, parseDistance(document.distance())))
                .toList();
    }

    // ── Helper: 거리 파싱 ─────────────────────────────────────────────────
    // ── 거리 문자열 파싱 ──────────────────────────────────────────────────
    private Integer parseDistance(String distance) {
        if (distance == null || distance.isBlank()) {
            return null;
        }
        try {
            return Integer.valueOf(distance);
        } catch (NumberFormatException exception) {
            return null;
        }
    }
}
