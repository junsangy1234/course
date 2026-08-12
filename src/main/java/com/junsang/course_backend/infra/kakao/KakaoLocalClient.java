package com.junsang.course_backend.infra.kakao;

import com.junsang.course_backend.global.exception.BusinessException;
import com.junsang.course_backend.global.exception.ErrorCode;
import com.junsang.course_backend.infra.kakao.config.KakaoLocalProperties;
import com.junsang.course_backend.infra.kakao.dto.request.KakaoKeywordSearchRequest;
import com.junsang.course_backend.infra.kakao.dto.response.KakaoKeywordSearchResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class KakaoLocalClient {

    private final KakaoLocalProperties properties;
    private final RestClient restClient;

    public KakaoLocalClient(RestClient.Builder restClientBuilder, KakaoLocalProperties properties) {
        this.properties = properties;
        this.restClient = restClientBuilder
                .baseUrl(properties.baseUrl().toString())
                .build();
    }

    // ── 카카오 키워드 장소 검색 ───────────────────────────────────────────
    public KakaoKeywordSearchResponse searchKeyword(KakaoKeywordSearchRequest request) {
        if (!properties.isApiKeyConfigured()) {
            throw new BusinessException(ErrorCode.KAKAO_API_KEY_NOT_CONFIGURED);
        }

        KakaoKeywordSearchResponse response;
        try {
            response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v2/local/search/keyword.json")
                            .queryParam("query", request.query())
                            .queryParam("x", request.longitude())
                            .queryParam("y", request.latitude())
                            .queryParam("radius", request.radius())
                            .queryParam("size", request.size())
                            .build())
                    .header(HttpHeaders.AUTHORIZATION, "KakaoAK " + properties.restApiKey())
                    .retrieve()
                    .body(KakaoKeywordSearchResponse.class);
        } catch (RestClientException exception) {
            throw new BusinessException(ErrorCode.KAKAO_LOCAL_API_REQUEST_FAILED);
        }

        if (response == null) {
            throw new BusinessException(ErrorCode.KAKAO_LOCAL_API_EMPTY_RESPONSE);
        }
        return response;
    }
}
