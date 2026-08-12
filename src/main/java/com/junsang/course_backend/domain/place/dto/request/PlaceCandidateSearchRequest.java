package com.junsang.course_backend.domain.place.dto.request;

import com.junsang.course_backend.domain.place.entity.PlaceType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlaceCandidateSearchRequest {

    @NotBlank
    private String query;

    @NotNull
    private PlaceType type;

    @NotNull
    @DecimalMin(value = "-90.0")
    @DecimalMax(value = "90.0")
    private BigDecimal latitude;

    @NotNull
    @DecimalMin(value = "-180.0")
    @DecimalMax(value = "180.0")
    private BigDecimal longitude;

    @Min(0)
    @Max(20000)
    private Integer radius = 3000;

    @Min(1)
    @Max(15)
    private Integer size = 15;
}
