package com.junsang.course_backend.domain.activity.repository;

import com.junsang.course_backend.domain.activity.entity.ActivityCategory;
import java.util.Collection;
import java.util.List;

import com.junsang.course_backend.domain.activity.repository.projection.ActivityScoreProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ActivityCategoryRepository extends JpaRepository<ActivityCategory, Long> {

    List<ActivityCategory> findByParentIsNullAndActiveTrueOrderByDisplayOrderAsc();

    @Query("""
            SELECT category.id AS activityCategoryId,
                   category.code AS code,
                   category.name AS name,
                   COALESCE(SUM(rule.weight), 0L) AS score
            FROM ActivityCategory category
            LEFT JOIN ActivityRecommendationRule rule
                ON rule.activityCategory = category
                AND rule.preferenceOption.id IN :preferenceOptionIds
            WHERE category.parent IS NULL
              AND category.active = TRUE
            GROUP BY category.id, category.code, category.name
            ORDER BY COALESCE(SUM(rule.weight), 0L) DESC, category.name ASC
            """)
    List<ActivityScoreProjection> findActiveRootScoreProjectionsByPreferenceOptionIds(
            @Param("preferenceOptionIds") Collection<Long> preferenceOptionIds
    );

    @Query("""
            SELECT category.id AS activityCategoryId,
                   category.code AS code,
                   category.name AS name,
                   0L AS score
            FROM ActivityCategory category
            WHERE category.parent IS NULL
              AND category.active = TRUE
            ORDER BY category.name ASC
            """)
    List<ActivityScoreProjection> findActiveRootZeroScoreProjections();
}
