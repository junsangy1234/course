package com.junsang.course_backend.domain.place.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
        name = "tags",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_tags_group_code",
                columnNames = {"tag_group", "code"}
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    // 음식 종류, 분위기, 활동 유형 등 태그의 분류
    @Column(name = "tag_group", nullable = false, length = 30)
    private TagGroup group;

    @Column(nullable = false, length = 50)
    private String code;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    private Tag(TagGroup group, String code, String displayName) {
        this.group = group;
        this.code = code;
        this.displayName = displayName;
    }

    // ── 생성 ───────────────────────────────────────────────────────────────
    public static Tag create(TagGroup group, String code, String displayName) {
        return new Tag(group, code, displayName);
    }
}
