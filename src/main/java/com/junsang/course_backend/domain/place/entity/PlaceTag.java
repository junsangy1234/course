package com.junsang.course_backend.domain.place.entity;

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
        name = "place_tags",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_place_tags_place_tag",
                columnNames = {"place_id", "tag_id"}
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlaceTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    // 태그가 부여된 장소
    @JoinColumn(name = "place_id", nullable = false)
    private Place place;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    // 장소에 부여할 태그
    @JoinColumn(name = "tag_id", nullable = false)
    private Tag tag;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TagSource source;

    @Column(nullable = false)
    private int confidence;

    private PlaceTag(Place place, Tag tag, TagSource source, int confidence) {
        if (confidence < 0 || confidence > 100) {
            throw new IllegalArgumentException("태그 신뢰도는 0에서 100 사이여야 합니다.");
        }
        this.place = place;
        this.tag = tag;
        this.source = source;
        this.confidence = confidence;
    }

    // ── 생성 ───────────────────────────────────────────────────────────────
    public static PlaceTag create(Place place, Tag tag, TagSource source, int confidence) {
        return new PlaceTag(place, tag, source, confidence);
    }

    // ── 조회 ───────────────────────────────────────────────────────────────
    public boolean hasTag(Tag candidate) {
        // 영속화된 태그 ID를 기준으로 동일 태그인지 확인한다.
        return tag.getId() != null && tag.getId().equals(candidate.getId());
    }
}
