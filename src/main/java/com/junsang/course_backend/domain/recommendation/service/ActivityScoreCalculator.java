package com.junsang.course_backend.domain.recommendation.service;

import com.junsang.course_backend.domain.activity.repository.ActivityCategoryRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ActivityScoreCalculator {

    private final ActivityCategoryRepository activityCategoryRepository;

    // ── 활동 점수 계산 ────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<ActivityScore> calculate(List<Long> preferenceOptionIds) {
        if (preferenceOptionIds.isEmpty()) {
            return activityCategoryRepository.findActiveRootZeroScoreProjections().stream()
                    .map(projection -> new ActivityScore(
                            projection.getActivityCategoryId(),
                            projection.getCode(),
                            projection.getName(),
                            projection.getScore().intValue()
                    ))
                    .toList();
        }

        return activityCategoryRepository.findActiveRootScoreProjectionsByPreferenceOptionIds(preferenceOptionIds)
                .stream()
                .map(projection -> new ActivityScore(
                        projection.getActivityCategoryId(),
                        projection.getCode(),
                        projection.getName(),
                        projection.getScore().intValue()
                ))
                .toList();
    }
}
