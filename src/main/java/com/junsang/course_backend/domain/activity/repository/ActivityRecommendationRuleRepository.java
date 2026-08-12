package com.junsang.course_backend.domain.activity.repository;

import com.junsang.course_backend.domain.activity.entity.ActivityRecommendationRule;
import com.junsang.course_backend.domain.activity.repository.projection.ActivityRecommendationRuleWeightProjection;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ActivityRecommendationRuleRepository extends JpaRepository<ActivityRecommendationRule, Long> {
    @EntityGraph(attributePaths = {"preferenceOption", "activityCategory"})
    List<ActivityRecommendationRule> findByPreferenceOptionIdInAndActivityCategoryIdIn(
            Collection<Long> preferenceOptionIds,
            Collection<Long> activityCategoryIds
    );

    @Query("""
            SELECT new com.junsang.course_backend.domain.activity.repository.projection.ActivityRecommendationRuleWeightProjection(
                rule.preferenceOption.id,
                rule.activityCategory.id,
                rule.weight
            )
            FROM ActivityRecommendationRule rule
            """)
    List<ActivityRecommendationRuleWeightProjection> findAllWeightProjections();
}
