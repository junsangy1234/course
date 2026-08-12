package com.junsang.course_backend.domain.preference.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "preference_groups",
        uniqueConstraints = @UniqueConstraint(name = "uk_preference_groups_code", columnNames = "code")
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PreferenceGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "allows_multiple_selection", nullable = false)
    // 분위기처럼 여러 개를 고를 수 있는 질문인지 여부
    private boolean allowsMultipleSelection;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    private PreferenceGroup(String code, String name, boolean allowsMultipleSelection, int displayOrder) {
        validateDisplayOrder(displayOrder);
        this.code = code;
        this.name = name;
        this.allowsMultipleSelection = allowsMultipleSelection;
        this.displayOrder = displayOrder;
        this.active = true;
    }

    // ── 생성 ───────────────────────────────────────────────────────────────
    public static PreferenceGroup create(
            String code,
            String name,
            boolean allowsMultipleSelection,
            int displayOrder
    ) {
        return new PreferenceGroup(code, name, allowsMultipleSelection, displayOrder);
    }

    // ── 비즈니스 메서드 ─────────────────────────────────────────────────────
    public void deactivate() {
        // 화면에 더 이상 노출하지 않되 기존 규칙과 이력은 보존한다.
        this.active = false;
    }

    private static void validateDisplayOrder(int displayOrder) {
        if (displayOrder < 0) {
            throw new IllegalArgumentException("선택지 그룹 노출 순서는 0 이상이어야 합니다.");
        }
    }
}
