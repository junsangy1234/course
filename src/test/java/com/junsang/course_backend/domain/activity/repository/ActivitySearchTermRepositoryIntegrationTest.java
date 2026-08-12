package com.junsang.course_backend.domain.activity.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.junsang.course_backend.domain.activity.repository.projection.ActivityScoreProjection;
import com.junsang.course_backend.domain.place.entity.PlaceProvider;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ActivitySearchTermRepositoryIntegrationTest {

    @Autowired
    private ActivityCategoryRepository activityCategoryRepository;

    @Autowired
    private ActivitySearchTermRepository activitySearchTermRepository;

    // ── 활동별 카카오 검색어 조회 ─────────────────────────────────────────
    @Test
    void findsActiveSearchTermsWithPlaceProviderEnum() {
        List<ActivityScoreProjection> categories = activityCategoryRepository.findActiveRootZeroScoreProjections();
        Long activityCategoryId = categories.stream()
                .filter(category -> category.getCode().equals("WORKSHOP"))
                .findFirst()
                .orElseThrow()
                .getActivityCategoryId();

        assertThat(activitySearchTermRepository.findActiveSearchTermsForCategoryAndProvider(
                activityCategoryId,
                PlaceProvider.KAKAO
        )).isNotEmpty();
    }
}
