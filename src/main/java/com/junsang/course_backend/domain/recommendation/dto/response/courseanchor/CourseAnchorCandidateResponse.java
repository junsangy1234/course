package com.junsang.course_backend.domain.recommendation.dto.response.courseanchor;

import com.junsang.course_backend.domain.courseanchor.entity.CourseAnchorType;
import java.math.BigDecimal;

public record CourseAnchorCandidateResponse(
        int rank,
        Long id,
        String code,
        String name,
        CourseAnchorType type,
        String description,
        BigDecimal latitude,
        BigDecimal longitude,
        int radiusMeters,
        int directPreferenceScore,
        double activityBasedScore,
        double finalScore
) {
    public static CourseAnchorCandidateResponse of(
            int rank,
            Long id,
            String code,
            String name,
            CourseAnchorType type,
            String description,
            BigDecimal latitude,
            BigDecimal longitude,
            Integer radiusMeters,
            int directPreferenceScore,
            double activityBasedScore,
            double finalScore
    ) {
        return new CourseAnchorCandidateResponse(
                rank,
                id,
                code,
                name,
                type,
                description,
                latitude,
                longitude,
                radiusMeters,
                directPreferenceScore,
                activityBasedScore,
                finalScore
        );
    }
}
