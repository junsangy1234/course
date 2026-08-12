package com.junsang.course_backend.domain.course.entity;

import com.junsang.course_backend.domain.place.entity.Place;
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
        name = "course_items",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_course_items_course_sequence",
                columnNames = {"course_id", "sequence_no"}
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CourseItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "sequence_no", nullable = false)
    private int sequenceNo;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false, length = 20)
    private CourseItemType type;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "place_id", nullable = false)
    private Place place;

    private CourseItem(Course course, int sequenceNo, CourseItemType type, Place place) {
        validateSequenceNo(sequenceNo);
        if (place == null) {
            throw new IllegalArgumentException("저장할 코스 항목의 장소는 필수입니다.");
        }
        this.course = course;
        this.sequenceNo = sequenceNo;
        this.type = type;
        this.place = place;
    }

    // ── 생성 ───────────────────────────────────────────────────────────────
    static CourseItem create(Course course, int sequenceNo, CourseItemType type, Place place) {
        return new CourseItem(course, sequenceNo, type, place);
    }

    // ── 비즈니스 메서드 ─────────────────────────────────────────────────────
    void changeSequenceNo(int sequenceNo) {
        validateSequenceNo(sequenceNo);
        this.sequenceNo = sequenceNo;
    }

    private static void validateSequenceNo(int sequenceNo) {
        if (sequenceNo < 1) {
            throw new IllegalArgumentException("코스 항목 순서는 1 이상이어야 합니다.");
        }
    }
}
