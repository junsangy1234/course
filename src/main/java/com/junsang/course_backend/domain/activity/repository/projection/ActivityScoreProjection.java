package com.junsang.course_backend.domain.activity.repository.projection;

public interface ActivityScoreProjection {

    Long getActivityCategoryId();

    String getCode();

    String getName();

    Long getScore();
}
