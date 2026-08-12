package com.junsang.course_backend.infra.kakao.dto.request;

import java.math.BigDecimal;

public record KakaoKeywordSearchRequest(
        String query,
        BigDecimal longitude,
        BigDecimal latitude,
        int radius,
        int size,
        int page
) {
}
