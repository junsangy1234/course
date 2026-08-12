package com.junsang.course_backend.domain.activity.entity;

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
@Table(
        name = "activity_recommendation_rules",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_activity_recommendation_rules_option_category",
                columnNames = {"preference_option_id", "activity_category_id"}
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ActivityRecommendationRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    // 사용자가 선택한 선호도 옵션
    @JoinColumn(name = "preference_option_id", nullable = false)
    private PreferenceOption preferenceOption;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    // 점수를 부여받는 활동 카테고리
    @JoinColumn(name = "activity_category_id", nullable = false)
    private ActivityCategory activityCategory;

    @Column(nullable = false)
    private int weight;

    private ActivityRecommendationRule(
            PreferenceOption preferenceOption,
            ActivityCategory activityCategory,
            int weight
    ) {
        this.preferenceOption = preferenceOption;
        this.activityCategory = activityCategory;
        this.weight = weight;
    }

    // ── 생성 ───────────────────────────────────────────────────────────────
    public static ActivityRecommendationRule create(
            PreferenceOption preferenceOption,
            ActivityCategory activityCategory,
            int weight
    ) {
        return new ActivityRecommendationRule(preferenceOption, activityCategory, weight);
    }

    // ── 비즈니스 메서드 ─────────────────────────────────────────────────────
    public void changeWeight(int weight) {
        // 관리자 화면에서 수정한 가중치를 반영한다.
        this.weight = weight;
    }
}
