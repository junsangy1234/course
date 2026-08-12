package com.junsang.course_backend.domain.recommendation.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.junsang.course_backend.domain.preference.repository.PreferenceOptionRepository;
import com.junsang.course_backend.domain.recommendation.dto.request.CourseAnchorRecommendationRequest;
import com.junsang.course_backend.domain.recommendation.dto.response.courseanchor.CourseAnchorRecommendationResponse;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CourseAnchorRecommendationServiceIntegrationTest {

    @Autowired
    private CourseAnchorRecommendationService courseAnchorRecommendationService;

    @Autowired
    private PreferenceOptionRepository preferenceOptionRepository;

    // ── 선택 선호도 기반 중심지 집계 조회 ──────────────────────────────────
    @Test
    void recommendsCourseAnchorsWithDatabaseAggregatedScores() {
        List<Long> preferenceOptionIds = preferenceOptionRepository.findAll().stream()
                .limit(4)
                .map(option -> option.getId())
                .toList();

        CourseAnchorRecommendationResponse response = courseAnchorRecommendationService.recommendCourseAnchor(
                new CourseAnchorRecommendationRequest("서울", preferenceOptionIds, 5)
        );

        assertThat(response.courseAnchors()).isNotEmpty();
        assertThat(response.courseAnchors()).hasSizeLessThanOrEqualTo(5);
        assertThat(response.courseAnchors().getFirst().finalScore()).isGreaterThanOrEqualTo(0);
        assertThat(response.activityScores()).isNotEmpty();
    }
}
