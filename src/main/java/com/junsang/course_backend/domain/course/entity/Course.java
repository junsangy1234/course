package com.junsang.course_backend.domain.course.entity;

import com.junsang.course_backend.domain.place.entity.Place;
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
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "courses")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "selected_region_name", nullable = false, length = 100)
    private String selectedRegionName;

    @Column(name = "selected_region_latitude", nullable = false, precision = 10, scale = 7)
    private BigDecimal selectedRegionLatitude;

    @Column(name = "selected_region_longitude", nullable = false, precision = 10, scale = 7)
    private BigDecimal selectedRegionLongitude;

    @Column(name = "selected_region_radius_meters", nullable = false)
    private int selectedRegionRadiusMeters;

    @Column(name = "scheduled_at", nullable = false)
    private LocalDateTime scheduledAt;

    @Column(name = "ends_at", nullable = false)
    private LocalDateTime endsAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CourseStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<CoursePreference> preferences = new ArrayList<>();

    @OrderBy("sequenceNo ASC")
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<CourseItem> items = new ArrayList<>();

    private Course(
            String selectedRegionName,
            BigDecimal selectedRegionLatitude,
            BigDecimal selectedRegionLongitude,
            int selectedRegionRadiusMeters,
            LocalDateTime scheduledAt,
            LocalDateTime endsAt
    ) {
        validateSchedule(selectedRegionRadiusMeters, scheduledAt, endsAt);
        this.selectedRegionName = selectedRegionName;
        this.selectedRegionLatitude = selectedRegionLatitude;
        this.selectedRegionLongitude = selectedRegionLongitude;
        this.selectedRegionRadiusMeters = selectedRegionRadiusMeters;
        this.scheduledAt = scheduledAt;
        this.endsAt = endsAt;
        this.status = CourseStatus.SAVED;
    }

    // ── 생성 ───────────────────────────────────────────────────────────────
    public static Course create(
            String selectedRegionName,
            BigDecimal selectedRegionLatitude,
            BigDecimal selectedRegionLongitude,
            int selectedRegionRadiusMeters,
            LocalDateTime scheduledAt,
            LocalDateTime endsAt
    ) {
        return new Course(
                selectedRegionName,
                selectedRegionLatitude,
                selectedRegionLongitude,
                selectedRegionRadiusMeters,
                scheduledAt,
                endsAt
        );
    }

    // ── 비즈니스 메서드 ─────────────────────────────────────────────────────
    public void addPreference(PreferenceOption preferenceOption) {
        if (preferenceOption == null) {
            throw new IllegalArgumentException("선호도 옵션은 필수입니다.");
        }
        if (preferences.stream().anyMatch(preference -> preference.hasPreferenceOption(preferenceOption))) {
            return;
        }
        preferences.add(CoursePreference.create(this, preferenceOption));
    }

    public void addItem(CourseItemType type, Place place) {
        if (type == null) {
            throw new IllegalArgumentException("코스 항목 유형은 필수입니다.");
        }
        items.add(CourseItem.create(this, items.size() + 1, type, place));
    }

    public void removeItem(CourseItem item) {
        if (!items.remove(item)) {
            return;
        }
        normalizeItemSequence();
    }

    public void archive() {
        this.status = CourseStatus.ARCHIVED;
    }

    private void normalizeItemSequence() {
        for (int index = 0; index < items.size(); index++) {
            items.get(index).changeSequenceNo(index + 1);
        }
    }

    @PrePersist
    private void setCreatedAt() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    private void setUpdatedAt() {
        this.updatedAt = LocalDateTime.now();
    }

    private static void validateSchedule(
            int selectedRegionRadiusMeters,
            LocalDateTime scheduledAt,
            LocalDateTime endsAt
    ) {
        if (selectedRegionRadiusMeters < 1) {
            throw new IllegalArgumentException("지역 검색 반경은 1m 이상이어야 합니다.");
        }
        if (scheduledAt == null || endsAt == null || !endsAt.isAfter(scheduledAt)) {
            throw new IllegalArgumentException("코스 종료 시간은 시작 시간보다 뒤여야 합니다.");
        }
    }
}
