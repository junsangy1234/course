package com.junsang.course_backend.domain.courseanchor.repository.projection;

import java.math.BigDecimal;

public interface CourseAnchorRecommendationProjection {

    Long getId();

    String getCode();

    String getName();

    String getAnchorType();

    String getDescription();

    BigDecimal getLatitude();

    BigDecimal getLongitude();

    Integer getRadiusMeters();

    Integer getDirectPreferenceScore();

    BigDecimal getActivityBasedScore();

    BigDecimal getFinalScore();
}
