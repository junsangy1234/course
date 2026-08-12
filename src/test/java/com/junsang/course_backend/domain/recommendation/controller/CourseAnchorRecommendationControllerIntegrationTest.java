package com.junsang.course_backend.domain.recommendation.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.junsang.course_backend.domain.preference.repository.PreferenceOptionRepository;
import com.junsang.course_backend.domain.recommendation.dto.request.CourseAnchorRecommendationRequest;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class CourseAnchorRecommendationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PreferenceOptionRepository preferenceOptionRepository;

    @Autowired
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    // ── 코스 중심지 추천 API ──────────────────────────────────────────────
    @Test
    void recommendsCourseAnchors() throws Exception {
        List<Long> preferenceOptionIds = preferenceOptionRepository.findAll().stream()
                .limit(4)
                .map(option -> option.getId())
                .toList();

        CourseAnchorRecommendationRequest request = new CourseAnchorRecommendationRequest(
                "서울",
                preferenceOptionIds,
                5
        );

        mockMvc.perform(post("/api/course-anchors/recommendations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.regionName").value("서울"))
                .andExpect(jsonPath("$.activityScores").isArray())
                .andExpect(jsonPath("$.activityScores[0].activityCategoryId").isNumber())
                .andExpect(jsonPath("$.courseAnchors").isArray())
                .andExpect(jsonPath("$.courseAnchors[0].rank").value(1))
                .andExpect(jsonPath("$.courseAnchors[0].finalScore").isNumber());
    }

    // ── 요청 검증 API ────────────────────────────────────────────────────
    @Test
    void returnsBadRequestWhenRegionNameIsBlank() throws Exception {
        String invalidRequest = """
                {
                  "regionName": " ",
                  "preferenceOptionIds": []
                }
                """;

        mockMvc.perform(post("/api/course-anchors/recommendations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
    }

    // ── 지역 미존재 API ───────────────────────────────────────────────────
    @Test
    void returnsNotFoundWhenNoCourseAnchorsExistInRegion() throws Exception {
        CourseAnchorRecommendationRequest request = new CourseAnchorRecommendationRequest(
                "존재하지않는지역",
                List.of(),
                5
        );

        mockMvc.perform(post("/api/course-anchors/recommendations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("COURSE_ANCHOR_NOT_FOUND"));
    }
}
