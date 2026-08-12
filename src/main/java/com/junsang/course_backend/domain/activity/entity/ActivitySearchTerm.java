package com.junsang.course_backend.domain.activity.entity;

import com.junsang.course_backend.domain.place.entity.PlaceProvider;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
        name = "activity_search_terms",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_activity_search_terms_category_provider_keyword",
                columnNames = {"activity_category_id", "provider", "keyword"}
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ActivitySearchTerm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    // 이 검색어가 대표하는 활동 카테고리
    @JoinColumn(name = "activity_category_id", nullable = false)
    private ActivityCategory activityCategory;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PlaceProvider provider;

    @Column(nullable = false, length = 100)
    private String keyword;

    @Column(name = "search_priority", nullable = false)
    private int searchPriority;

    @Column(name = "is_fallback", nullable = false)
    private boolean fallback;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    private ActivitySearchTerm(
            ActivityCategory activityCategory,
            PlaceProvider provider,
            String keyword,
            int searchPriority,
            boolean fallback
    ) {
        validateSearchPriority(searchPriority);
        this.activityCategory = activityCategory;
        this.provider = provider;
        this.keyword = keyword;
        this.searchPriority = searchPriority;
        this.fallback = fallback;
        this.active = true;
    }

    // ── 생성 ───────────────────────────────────────────────────────────────
    public static ActivitySearchTerm create(
            ActivityCategory activityCategory,
            PlaceProvider provider,
            String keyword,
            int searchPriority,
            boolean fallback
    ) {
        return new ActivitySearchTerm(activityCategory, provider, keyword, searchPriority, fallback);
    }

    // ── 비즈니스 메서드 ─────────────────────────────────────────────────────
    public void deactivate() {
        // 검색 품질이 낮은 키워드는 삭제하지 않고 비활성화한다.
        this.active = false;
    }

    private static void validateSearchPriority(int searchPriority) {
        if (searchPriority < 0) {
            throw new IllegalArgumentException("검색 우선순위는 0 이상이어야 합니다.");
        }
    }
}
