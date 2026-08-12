package com.junsang.course_backend.domain.activity.entity;

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
        name = "activity_categories",
        uniqueConstraints = @UniqueConstraint(name = "uk_activity_categories_code", columnNames = "code")
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class    ActivityCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    // 상위 카테고리 null, 하위 카테고리는 부모 활동을 가리킴
    @JoinColumn(name = "parent_id")
    private ActivityCategory parent;

    @Column(nullable = false, length = 50)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    private ActivityCategory(ActivityCategory parent, String code, String name, int displayOrder) {
        validateDisplayOrder(displayOrder);
        this.parent = parent;
        this.code = code;
        this.name = name;
        this.displayOrder = displayOrder;
        this.active = true;
    }

    // ── 생성 ───────────────────────────────────────────────────────────────
    public static ActivityCategory create(ActivityCategory parent, String code, String name, int displayOrder) {
        return new ActivityCategory(parent, code, name, displayOrder);
    }

    // ── 비즈니스 메서드 ─────────────────────────────────────────────────────
    public void deactivate() {
        this.active = false;
    }

    private static void validateDisplayOrder(int displayOrder) {
        if (displayOrder < 0) {
            throw new IllegalArgumentException("활동 카테고리 노출 순서는 0 이상이어야 합니다.");
        }
    }
}
