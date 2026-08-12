package com.junsang.course_backend.domain.course.entity;

import com.junsang.course_backend.domain.preference.entity.PreferenceOption;
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
@Table(
        name = "course_preferences",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_course_preferences_course_option",
                columnNames = {"course_id", "preference_option_id"}
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CoursePreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "preference_option_id", nullable = false)
    private PreferenceOption preferenceOption;

    private CoursePreference(Course course, PreferenceOption preferenceOption) {
        this.course = course;
        this.preferenceOption = preferenceOption;
    }

    // ── 생성 ───────────────────────────────────────────────────────────────
    static CoursePreference create(Course course, PreferenceOption preferenceOption) {
        return new CoursePreference(course, preferenceOption);
    }

    boolean hasPreferenceOption(PreferenceOption preferenceOption) {
        return this.preferenceOption == preferenceOption;
    }
}
