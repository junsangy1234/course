package com.junsang.course_backend.domain.courseanchor.entity;

import com.junsang.course_backend.domain.activity.entity.ActivityCategory;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "course_anchor_activity_rules", uniqueConstraints = @UniqueConstraint(
        name = "uk_course_anchor_activity_rules_anchor_category",
        columnNames = {"course_anchor_id", "activity_category_id"}
))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CourseAnchorActivityRule {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_anchor_id", nullable = false)
    private CourseAnchor courseAnchor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "activity_category_id", nullable = false)
    private ActivityCategory activityCategory;

    @Column(nullable = false)
    private int weight;

    private CourseAnchorActivityRule(CourseAnchor courseAnchor, ActivityCategory activityCategory, int weight) {
        this.courseAnchor = courseAnchor;
        this.activityCategory = activityCategory;
        this.weight = weight;
    }

    static CourseAnchorActivityRule create(CourseAnchor courseAnchor, ActivityCategory activityCategory, int weight) {
        return new CourseAnchorActivityRule(courseAnchor, activityCategory, weight);
    }

    public void changeWeight(int weight) {
        this.weight = weight;
    }

    boolean hasActivityCategory(ActivityCategory activityCategory) {
        return this.activityCategory == activityCategory;
    }
}
