package com.junsang.course_backend.domain.recommendation.service;

import com.junsang.course_backend.domain.courseanchor.repository.CourseAnchorRepository;
import com.junsang.course_backend.domain.courseanchor.repository.projection.CourseAnchorRecommendationProjection;
import com.junsang.course_backend.domain.courseanchor.entity.CourseAnchorType;
import com.junsang.course_backend.domain.recommendation.dto.request.CourseAnchorRecommendationRequest;
import com.junsang.course_backend.domain.recommendation.dto.response.activity.ActivityScoreResponse;
import com.junsang.course_backend.domain.recommendation.dto.response.courseanchor.CourseAnchorCandidateResponse;
import com.junsang.course_backend.domain.recommendation.dto.response.courseanchor.CourseAnchorRecommendationResponse;
import com.junsang.course_backend.global.exception.BusinessException;
import com.junsang.course_backend.global.exception.ErrorCode;
import java.util.List;
import java.util.stream.IntStream;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CourseAnchorRecommendationService {

    private static final int DEFAULT_RESULT_SIZE = 5;

    private final CourseAnchorRepository courseAnchorRepository;
    private final ActivityScoreCalculator activityScoreCalculator;

    // ── 코스 중심지 추천 ──────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public CourseAnchorRecommendationResponse recommendCourseAnchor(CourseAnchorRecommendationRequest request) {
        int size = resolveSize(request.size());
        List<CourseAnchorRecommendationProjection> courseAnchorRecommendations = findCourseAnchorRecommendations(request, size);
        if (courseAnchorRecommendations.isEmpty()) {
            throw new BusinessException(ErrorCode.COURSE_ANCHOR_NOT_FOUND);
        }

        List<ActivityScore> activityScores = activityScoreCalculator.calculate(request.preferenceOptionIds());

        return CourseAnchorRecommendationResponse.of(
                request.regionName(),
                toActivityScoreResponses(activityScores),
                toCourseAnchorCandidates(courseAnchorRecommendations)
        );
    }

    // ── Helper: 응답 변환 ─────────────────────────────────────────────────
    // ── 활동 점수 응답 변환 ───────────────────────────────────────────────
    private List<ActivityScoreResponse> toActivityScoreResponses(
            List<ActivityScore> activityScores
    ) {
        return activityScores.stream()
                .map(ActivityScoreResponse::from)
                .toList();
    }

    // ── 코스 중심지 후보 응답 변환 ─────────────────────────────────────────
    private List<CourseAnchorCandidateResponse> toCourseAnchorCandidates(
            List<CourseAnchorRecommendationProjection> courseAnchorRecommendations
    ) {
        return IntStream.range(0, courseAnchorRecommendations.size())
                .mapToObj(index -> {
                    CourseAnchorRecommendationProjection recommendation = courseAnchorRecommendations.get(index);
                    return CourseAnchorCandidateResponse.of(
                            index + 1,
                            recommendation.getId(),
                            recommendation.getCode(),
                            recommendation.getName(),
                            CourseAnchorType.valueOf(recommendation.getAnchorType()),
                            recommendation.getDescription(),
                            recommendation.getLatitude(),
                            recommendation.getLongitude(),
                            recommendation.getRadiusMeters(),
                            recommendation.getDirectPreferenceScore(),
                            recommendation.getActivityBasedScore().doubleValue(),
                            recommendation.getFinalScore().doubleValue()
                    );
                })
                .toList();
    }

    // ── Helper: DB 집계 조회 ───────────────────────────────────────────────
    // ── 선택 선호도 기준 중심지 점수 조회 ──────────────────────────────────
    private List<CourseAnchorRecommendationProjection> findCourseAnchorRecommendations(
            CourseAnchorRecommendationRequest request,
            int size
    ) {
        // 선호도 선택 안한 상황
        if (request.preferenceOptionIds().isEmpty()) {
            return courseAnchorRepository.findZeroScoreProjections(request.regionName(), size);
        }
        return courseAnchorRepository.findRecommendationScoreProjections(
                request.regionName(),
                request.preferenceOptionIds().toArray(Long[]::new),
                size
        );
    }

    // ── Helper: 공통 계산 ─────────────────────────────────────────────────
    // ── 반환 후보 개수 결정 ───────────────────────────────────────────────
    private int resolveSize(Integer size) {
        return size == null ? DEFAULT_RESULT_SIZE : size;
    }

}
