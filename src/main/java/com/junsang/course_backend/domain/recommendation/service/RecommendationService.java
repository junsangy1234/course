package com.junsang.course_backend.domain.recommendation.service;

import com.junsang.course_backend.domain.activity.entity.ActivitySearchTerm;
import com.junsang.course_backend.domain.activity.repository.ActivitySearchTermRepository;
import com.junsang.course_backend.domain.place.entity.PlaceProvider;
import com.junsang.course_backend.domain.recommendation.dto.request.PlaceRecommendationRequest;
import com.junsang.course_backend.domain.recommendation.dto.response.activity.ActivityCandidateResponse;
import com.junsang.course_backend.domain.recommendation.dto.response.activity.ActivityRecommendationResponse;
import com.junsang.course_backend.domain.recommendation.dto.response.place.PlaceRecommendationCandidateResponse;
import com.junsang.course_backend.domain.recommendation.dto.response.place.PlaceRecommendationResponse;
import com.junsang.course_backend.global.exception.BusinessException;
import com.junsang.course_backend.global.exception.ErrorCode;
import com.junsang.course_backend.infra.kakao.KakaoLocalClient;
import com.junsang.course_backend.infra.kakao.dto.request.KakaoKeywordSearchRequest;
import com.junsang.course_backend.infra.kakao.dto.response.KakaoPlaceDocument;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private static final int PLACE_RESULT_SIZE = 10;
    private static final List<String> EXCLUDED_CATEGORY_KEYWORDS = List.of("주차장", "병원", "약국", "은행", "부동산");

    private final ActivityScoreCalculator activityScoreCalculator;
    private final ActivitySearchTermRepository activitySearchTermRepository;
    private final KakaoLocalClient kakaoLocalClient;

    // ── 활동 추천 ─────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public ActivityRecommendationResponse recommendActivities(List<Long> preferenceOptionIds) {
        return ActivityRecommendationResponse.of(toActivityCandidates(
                activityScoreCalculator.calculate(preferenceOptionIds)
        ));
    }

    // ── 장소 추천 ─────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public PlaceRecommendationResponse recommendPlaces(PlaceRecommendationRequest request) {
        List<ActivityCandidateResponse> activities = toActivityCandidates(
                activityScoreCalculator.calculate(request.preferenceOptionIds())
        );
        ActivityCandidateResponse selectedActivity = activities.stream()
                .filter(activity -> activity.id().equals(request.activityCategoryId()))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.ACTIVITY_CATEGORY_NOT_FOUND));

        List<ActivitySearchTerm> searchTerms = activitySearchTermRepository
                .findActiveSearchTermsForCategoryAndProvider(
                        selectedActivity.id(), PlaceProvider.KAKAO
                );

        Map<String, KakaoPlaceDocument> candidates = new LinkedHashMap<>();
        searchByTerms(searchTerms.stream().filter(term -> !term.isFallback()).toList(), request, candidates);
        if (candidates.size() < 3) {
            searchByTerms(searchTerms.stream().filter(ActivitySearchTerm::isFallback).toList(), request, candidates);
        }

        return PlaceRecommendationResponse.of(
                selectedActivity,
                candidates.values().stream()
                        .filter(this::isAllowedCandidate)
                        .map(document -> toPlaceCandidate(document, selectedActivity.score()))
                        .sorted(Comparator
                                .comparingInt(PlaceRecommendationCandidateResponse::finalScore).reversed()
                                .thenComparing(candidate -> candidate.distanceMeters() == null
                                        ? Integer.MAX_VALUE
                                        : candidate.distanceMeters())
                                .thenComparing(PlaceRecommendationCandidateResponse::name))
                        .limit(3)
                        .toList()
        );
    }

    // ── Helper: 활동 응답 변환 ─────────────────────────────────────────────
    // ── 활동 후보 응답 변환 ───────────────────────────────────────────────
    private List<ActivityCandidateResponse> toActivityCandidates(
            List<ActivityScore> activityScores
    ) {
        return activityScores.stream()
                .map(ActivityCandidateResponse::from)
                .toList();
    }

    // ── Helper: 카카오 장소 검색 ──────────────────────────────────────────
    // ── 검색어별 장소 후보 조회 ───────────────────────────────────────────
    private void searchByTerms(
            List<ActivitySearchTerm> terms,
            PlaceRecommendationRequest request,
            Map<String, KakaoPlaceDocument> candidates
    ) {
        for (ActivitySearchTerm term : terms) {
            List<KakaoPlaceDocument> documents = kakaoLocalClient.searchKeyword(new KakaoKeywordSearchRequest(
                    term.getKeyword(),
                    request.longitude(),
                    request.latitude(),
                    request.radius(),
                    PLACE_RESULT_SIZE
            )).documents();

            if (documents == null) {
                continue;
            }

            documents.forEach(document -> candidates.putIfAbsent(document.id(), document));
        }
    }

    // ── Helper: 장소 후보 필터링 ──────────────────────────────────────────
    // ── 허용 가능한 장소 후보 확인 ─────────────────────────────────────────
    private boolean isAllowedCandidate(KakaoPlaceDocument document) {
        String categoryName = document.categoryName() == null ? "" : document.categoryName().toLowerCase(Locale.ROOT);
        return EXCLUDED_CATEGORY_KEYWORDS.stream()
                .noneMatch(excludedKeyword -> categoryName.contains(excludedKeyword.toLowerCase(Locale.ROOT)));
    }

    // ── Helper: 장소 후보 점수 및 응답 변환 ───────────────────────────────
    // ── 장소 후보 점수 계산 및 응답 변환 ──────────────────────────────────
    private PlaceRecommendationCandidateResponse toPlaceCandidate(
            KakaoPlaceDocument document,
            int activityScore
    ) {
        Integer distanceMeters = parseDistance(document.distance());
        int distanceScore = distanceMeters == null ? 0 : Math.max(0, 30 - Math.round(distanceMeters / 100.0f));
        return PlaceRecommendationCandidateResponse.from(
                document,
                distanceMeters,
                activityScore,
                distanceScore
        );
    }

    // ── 거리 문자열 파싱 ──────────────────────────────────────────────────
    private Integer parseDistance(String distance) {
        // 카카오의 distance는 문자열이므로 숫자로 변환할 수 없으면 거리 점수를 0으로 처리한다.
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
