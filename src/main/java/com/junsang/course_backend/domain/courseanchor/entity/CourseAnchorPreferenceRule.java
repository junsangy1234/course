package com.junsang.course_backend.domain.courseanchor.entity;

import com.junsang.course_backend.domain.preference.entity.PreferenceOption;
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
@Table(name = "course_anchor_preference_rules", uniqueConstraints = @UniqueConstraint(
        name = "uk_course_anchor_preference_rules_anchor_option",
        columnNames = {"course_anchor_id", "preference_option_id"}
))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CourseAnchorPreferenceRule {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_anchor_id", nullable = false)
    private CourseAnchor courseAnchor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "preference_option_id", nullable = false)
    private PreferenceOption preferenceOption;

    @Column(nullable = false)
    private int weight;

    private CourseAnchorPreferenceRule(CourseAnchor courseAnchor, PreferenceOption preferenceOption, int weight) {
        this.courseAnchor = courseAnchor;
        this.preferenceOption = preferenceOption;
        this.weight = weight;
    }

    static CourseAnchorPreferenceRule create(CourseAnchor courseAnchor, PreferenceOption preferenceOption, int weight) {
        return new CourseAnchorPreferenceRule(courseAnchor, preferenceOption, weight);
    }

    public void changeWeight(int weight) {
        this.weight = weight;
    }

    boolean hasPreferenceOption(PreferenceOption preferenceOption) {
        return this.preferenceOption == preferenceOption;
    }
}
