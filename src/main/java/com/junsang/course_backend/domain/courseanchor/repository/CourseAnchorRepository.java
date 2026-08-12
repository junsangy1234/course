package com.junsang.course_backend.domain.courseanchor.repository;

import com.junsang.course_backend.domain.courseanchor.entity.CourseAnchor;
import com.junsang.course_backend.domain.courseanchor.repository.projection.CourseAnchorRecommendationProjection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CourseAnchorRepository extends JpaRepository<CourseAnchor, Long> {
    @Query(value = """
            WITH candidate_anchors AS (
                SELECT id, code, anchor_type, name, description, latitude, longitude, radius_meters, display_order
                FROM course_anchors
                WHERE parent_region_name = :regionName
                  AND is_active = TRUE
            ),
            activity_scores AS (
                SELECT category.id AS activity_category_id,
                       LEAST(GREATEST(COALESCE(SUM(rule.weight), 0), 0), 100)::NUMERIC / 100 AS normalized_score
                FROM activity_categories category
                LEFT JOIN activity_recommendation_rules rule
                    ON rule.activity_category_id = category.id
                    AND rule.preference_option_id = ANY(CAST(:preferenceOptionIds AS BIGINT[]))
                WHERE category.parent_id IS NULL
                  AND category.is_active = TRUE
                GROUP BY category.id
            ),
            direct_preference_scores AS (
                SELECT rule.course_anchor_id,
                       SUM(rule.weight)::INTEGER AS direct_preference_score
                FROM course_anchor_preference_rules rule
                JOIN candidate_anchors anchor ON anchor.id = rule.course_anchor_id
                WHERE rule.preference_option_id = ANY(CAST(:preferenceOptionIds AS BIGINT[]))
                GROUP BY rule.course_anchor_id
            ),
            activity_based_scores AS (
                SELECT rule.course_anchor_id,
                       SUM(activity_scores.normalized_score * rule.weight) AS activity_based_score
                FROM course_anchor_activity_rules rule
                JOIN candidate_anchors anchor ON anchor.id = rule.course_anchor_id
                JOIN activity_scores ON activity_scores.activity_category_id = rule.activity_category_id
                GROUP BY rule.course_anchor_id
            )
            SELECT anchor.id AS id,
                   anchor.code AS code,
                   anchor.name AS name,
                   anchor.anchor_type AS "anchorType",
                   anchor.description AS description,
                   anchor.latitude AS latitude,
                   anchor.longitude AS longitude,
                   anchor.radius_meters AS "radiusMeters",
                   COALESCE(direct_preference_scores.direct_preference_score, 0) AS "directPreferenceScore",
                   COALESCE(activity_based_scores.activity_based_score, 0) AS "activityBasedScore",
                   COALESCE(direct_preference_scores.direct_preference_score, 0)
                       + COALESCE(activity_based_scores.activity_based_score, 0) AS "finalScore"
            FROM candidate_anchors anchor
            LEFT JOIN direct_preference_scores ON direct_preference_scores.course_anchor_id = anchor.id
            LEFT JOIN activity_based_scores ON activity_based_scores.course_anchor_id = anchor.id
            ORDER BY "finalScore" DESC, anchor.display_order ASC, anchor.name ASC
            LIMIT :size
            """, nativeQuery = true)
    List<CourseAnchorRecommendationProjection> findRecommendationScoreProjections(
            @Param("regionName") String regionName,
            @Param("preferenceOptionIds") Long[] preferenceOptionIds,
            @Param("size") int size
    );

    @Query(value = """
            SELECT id AS id,
                   code AS code,
                   name AS name,
                   anchor_type AS "anchorType",
                   description AS description,
                   latitude AS latitude,
                   longitude AS longitude,
                   radius_meters AS "radiusMeters",
                   0 AS "directPreferenceScore",
                   0::NUMERIC AS "activityBasedScore",
                   0::NUMERIC AS "finalScore"
            FROM course_anchors
            WHERE parent_region_name = :regionName
              AND is_active = TRUE
            ORDER BY display_order ASC, name ASC
            LIMIT :size
            """, nativeQuery = true)
    List<CourseAnchorRecommendationProjection> findZeroScoreProjections(
            @Param("regionName") String regionName,
            @Param("size") int size
    );
}
