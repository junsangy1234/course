package com.junsang.course_backend.domain.place.entity;

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
@Table(
        name = "places",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_places_provider_place_id",
                columnNames = {"provider", "provider_place_id"}
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Place {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PlaceProvider provider;

    @Column(name = "provider_place_id", nullable = false, length = 100)
    private String providerPlaceId;

    @Enumerated(EnumType.STRING)
    @Column(name = "place_type", nullable = false, length = 20)
    private PlaceType type;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(name = "category_name", length = 500)
    private String categoryName;

    @Column(name = "address_name", nullable = false, length = 500)
    private String addressName;

    @Column(name = "road_address_name", length = 500)
    private String roadAddressName;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column(name = "place_url", length = 1_000)
    private String placeUrl;

    @OneToMany(mappedBy = "place", cascade = CascadeType.ALL, orphanRemoval = true)
    // 장소에 부여된 추천용 태그 목록
    private final List<PlaceTag> placeTags = new ArrayList<>();

    private Place(
            PlaceProvider provider,
            String providerPlaceId,
            PlaceType type,
            String name,
            String categoryName,
            String addressName,
            String roadAddressName,
            BigDecimal latitude,
            BigDecimal longitude,
            String placeUrl
    ) {
        this.provider = provider;
        this.providerPlaceId = providerPlaceId;
        this.type = type;
        this.name = name;
        this.categoryName = categoryName;
        this.addressName = addressName;
        this.roadAddressName = roadAddressName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.placeUrl = placeUrl;
    }

    // ── 생성 ───────────────────────────────────────────────────────────────
    public static Place create(
            PlaceProvider provider,
            String providerPlaceId,
            PlaceType type,
            String name,
            String categoryName,
            String addressName,
            String roadAddressName,
            BigDecimal latitude,
            BigDecimal longitude,
            String placeUrl
    ) {
        return new Place(
                provider,
                providerPlaceId,
                type,
                name,
                categoryName,
                addressName,
                roadAddressName,
                latitude,
                longitude,
                placeUrl
        );
    }

    // ── 비즈니스 메서드 ─────────────────────────────────────────────────────
    public void addTag(Tag tag, TagSource source, int confidence) {
        // 같은 태그가 이미 있으면 중복으로 추가하지 않는다.
        if (placeTags.stream().anyMatch(placeTag -> placeTag.hasTag(tag))) {
            return;
        }
        placeTags.add(PlaceTag.create(this, tag, source, confidence));
    }
}
