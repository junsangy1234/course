package com.junsang.course_backend.domain.courseanchor.entity;

import com.junsang.course_backend.domain.activity.entity.ActivityCategory;
import com.junsang.course_backend.domain.preference.entity.PreferenceOption;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "course_anchors", uniqueConstraints = @UniqueConstraint(
        name = "uk_course_anchors_code", columnNames = "code"
))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CourseAnchor {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(name = "anchor_type", nullable = false, length = 20)
    private CourseAnchorType type;

    @Column(name = "parent_region_name", nullable = false, length = 100)
    private String parentRegionName;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column(name = "radius_meters", nullable = false)
    private int radiusMeters;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    @OneToMany(mappedBy = "courseAnchor", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<CourseAnchorActivityRule> activityRules = new ArrayList<>();

    @OneToMany(mappedBy = "courseAnchor", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<CourseAnchorPreferenceRule> preferenceRules = new ArrayList<>();

    private CourseAnchor(String code, CourseAnchorType type, String parentRegionName, String name,
                         String description, BigDecimal latitude, BigDecimal longitude,
                         int radiusMeters, int displayOrder) {
        validate(radiusMeters, displayOrder);
        this.code = code;
        this.type = type;
        this.parentRegionName = parentRegionName;
        this.name = name;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
        this.radiusMeters = radiusMeters;
        this.displayOrder = displayOrder;
        this.active = true;
    }

    public static CourseAnchor create(String code, CourseAnchorType type, String parentRegionName, String name,
                                      String description, BigDecimal latitude, BigDecimal longitude,
                                      int radiusMeters, int displayOrder) {
        return new CourseAnchor(code, type, parentRegionName, name, description, latitude, longitude,
                radiusMeters, displayOrder);
    }

    public void addActivityRule(ActivityCategory activityCategory, int weight) {
        if (activityCategory == null) throw new IllegalArgumentException("활동 카테고리는 필수입니다.");
        if (activityRules.stream().anyMatch(rule -> rule.hasActivityCategory(activityCategory))) return;
        activityRules.add(CourseAnchorActivityRule.create(this, activityCategory, weight));
    }

    public void addPreferenceRule(PreferenceOption preferenceOption, int weight) {
        if (preferenceOption == null) throw new IllegalArgumentException("선호도 옵션은 필수입니다.");
        if (preferenceRules.stream().anyMatch(rule -> rule.hasPreferenceOption(preferenceOption))) return;
        preferenceRules.add(CourseAnchorPreferenceRule.create(this, preferenceOption, weight));
    }

    public void deactivate() { this.active = false; }

    public void activate() { this.active = true; }

    private static void validate(int radiusMeters, int displayOrder) {
        if (radiusMeters < 1) throw new IllegalArgumentException("코스 중심지 검색 반경은 1m 이상이어야 합니다.");
        if (displayOrder < 0) throw new IllegalArgumentException("코스 중심지 노출 순서는 0 이상이어야 합니다.");
    }
}
