package com.junsang.course_backend.domain.activity.service;

import com.junsang.course_backend.domain.activity.dto.request.UpdateActivityRecommendationRuleWeightRequest;
import com.junsang.course_backend.domain.activity.dto.request.UpdateActivityRecommendationRulesRequest;
import com.junsang.course_backend.domain.activity.dto.response.ActivityRecommendationRuleMatrixResponse;
import com.junsang.course_backend.domain.activity.dto.response.ActivityCategoryResponse;
import com.junsang.course_backend.domain.activity.dto.response.ActivityWeightResponse;
import com.junsang.course_backend.domain.activity.dto.response.PreferenceGroupResponse;
import com.junsang.course_backend.domain.activity.dto.response.PreferenceOptionResponse;
import com.junsang.course_backend.domain.activity.entity.ActivityCategory;
import com.junsang.course_backend.domain.activity.entity.ActivityRecommendationRule;
import com.junsang.course_backend.domain.activity.repository.ActivityCategoryRepository;
import com.junsang.course_backend.domain.activity.repository.ActivityRecommendationRuleRepository;
import com.junsang.course_backend.domain.preference.entity.PreferenceGroup;
import com.junsang.course_backend.domain.preference.entity.PreferenceOption;
import com.junsang.course_backend.domain.preference.repository.PreferenceGroupRepository;
import com.junsang.course_backend.domain.preference.repository.PreferenceOptionRepository;
import com.junsang.course_backend.global.exception.BusinessException;
import com.junsang.course_backend.global.exception.ErrorCode;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ActivityRecommendationRuleAdminService {

    private final PreferenceGroupRepository preferenceGroupRepository;
    private final PreferenceOptionRepository preferenceOptionRepository;
    private final ActivityCategoryRepository activityCategoryRepository;
    private final ActivityRecommendationRuleRepository activityRecommendationRuleRepository;

    // ── 추천 규칙 매트릭스 조회 ───────────────────────────────────────────
    @Transactional(readOnly = true)
    public ActivityRecommendationRuleMatrixResponse getMatrix() {
        List<ActivityCategory> categories = activityCategoryRepository.findByParentIsNullAndActiveTrueOrderByDisplayOrderAsc();
        Map<RuleKey, Integer> weights = activityRecommendationRuleRepository.findAllWeightProjections().stream()
                .collect(Collectors.toMap(
                        rule -> new RuleKey(rule.preferenceOptionId(), rule.activityCategoryId()),
                        rule -> rule.weight()
                ));
        Map<Long, List<PreferenceOption>> optionsByGroupId = preferenceOptionRepository
                .findAllActiveWithGroupOrderByGroupAndOption()
                .stream()
                .collect(Collectors.groupingBy(option -> option.getGroup().getId()));

        return ActivityRecommendationRuleMatrixResponse.of(
                categories.stream()
                        .map(ActivityCategoryResponse::from)
                        .toList(),
                preferenceGroupRepository.findByActiveTrueOrderByDisplayOrderAsc().stream()
                        .map(group -> toGroupResponse(
                                group,
                                optionsByGroupId.getOrDefault(group.getId(), List.of()),
                                categories,
                                weights
                        ))
                        .toList()
        );
    }

    // ── 추천 규칙 가중치 수정 ────────────────────────────────────────────
    @Transactional
    public ActivityRecommendationRuleMatrixResponse updateRules(UpdateActivityRecommendationRulesRequest request) {
        Map<RuleKey, UpdateActivityRecommendationRuleWeightRequest> updatesByRuleKey = request.rules().stream()
                .collect(Collectors.toMap(
                        update -> new RuleKey(update.preferenceOptionId(), update.activityCategoryId()),
                        update -> update,
                        (first, second) -> second,
                        LinkedHashMap::new
                ));
        Map<Long, PreferenceOption> optionsById = findPreferenceOptions(updatesByRuleKey);
        Map<Long, ActivityCategory> categoriesById = findActivityCategories(updatesByRuleKey);
        Map<RuleKey, ActivityRecommendationRule> existingRulesByKey = activityRecommendationRuleRepository
                .findByPreferenceOptionIdInAndActivityCategoryIdIn(
                        optionsById.keySet(),
                        categoriesById.keySet()
                )
                .stream()
                .collect(Collectors.toMap(
                        rule -> new RuleKey(rule.getPreferenceOption().getId(), rule.getActivityCategory().getId()),
                        rule -> rule
                ));

        List<ActivityRecommendationRule> rulesToCreate = new java.util.ArrayList<>();
        List<ActivityRecommendationRule> rulesToDelete = new java.util.ArrayList<>();
        updatesByRuleKey.forEach((ruleKey, update) -> applyRuleUpdate(
                existingRulesByKey.get(ruleKey),
                optionsById.get(ruleKey.preferenceOptionId()),
                categoriesById.get(ruleKey.activityCategoryId()),
                update.weight(),
                rulesToCreate,
                rulesToDelete
        ));

        if (!rulesToDelete.isEmpty()) {
            activityRecommendationRuleRepository.deleteAllInBatch(rulesToDelete);
        }
        if (!rulesToCreate.isEmpty()) {
            activityRecommendationRuleRepository.saveAll(rulesToCreate);
        }
        return getMatrix();
    }

    // ── Helper: 매트릭스 응답 변환 ─────────────────────────────────────────
    // ── 선호도 그룹 응답 변환 ─────────────────────────────────────────────
    private PreferenceGroupResponse toGroupResponse(
            PreferenceGroup group,
            List<PreferenceOption> options,
            List<ActivityCategory> categories,
            Map<RuleKey, Integer> weights
    ) {
        List<PreferenceOptionResponse> optionResponses = options
                .stream()
                .map(option -> PreferenceOptionResponse.from(
                        option,
                        categories.stream()
                                .map(category -> ActivityWeightResponse.of(
                                        category.getId(),
                                        weights.getOrDefault(new RuleKey(option.getId(), category.getId()), 0)
                                ))
                                .toList()
                ))
                .toList();

        return PreferenceGroupResponse.from(group, optionResponses);
    }

    // ── Helper: 규칙 저장 ─────────────────────────────────────────────────
    // ── 요청 선호도 옵션 일괄 조회 ─────────────────────────────────────────
    private Map<Long, PreferenceOption> findPreferenceOptions(
            Map<RuleKey, UpdateActivityRecommendationRuleWeightRequest> updatesByRuleKey
    ) {
        List<Long> optionIds = updatesByRuleKey.keySet().stream().map(RuleKey::preferenceOptionId).distinct().toList();
        Map<Long, PreferenceOption> optionsById = preferenceOptionRepository.findAllById(optionIds).stream()
                .collect(Collectors.toMap(PreferenceOption::getId, option -> option));
        if (optionsById.size() != optionIds.size()) {
            throw new BusinessException(ErrorCode.PREFERENCE_OPTION_NOT_FOUND);
        }
        return optionsById;
    }

    // ── 요청 활동 카테고리 일괄 조회 ───────────────────────────────────────
    private Map<Long, ActivityCategory> findActivityCategories(
            Map<RuleKey, UpdateActivityRecommendationRuleWeightRequest> updatesByRuleKey
    ) {
        List<Long> categoryIds = updatesByRuleKey.keySet().stream().map(RuleKey::activityCategoryId).distinct().toList();
        Map<Long, ActivityCategory> categoriesById = activityCategoryRepository.findAllById(categoryIds).stream()
                .collect(Collectors.toMap(ActivityCategory::getId, category -> category));
        if (categoriesById.size() != categoryIds.size()) {
            throw new BusinessException(ErrorCode.ACTIVITY_CATEGORY_NOT_FOUND);
        }
        return categoriesById;
    }

    // ── 규칙 일괄 변경 적용 ───────────────────────────────────────────────
    private void applyRuleUpdate(
            ActivityRecommendationRule existingRule,
            PreferenceOption option,
            ActivityCategory category,
            int weight,
            List<ActivityRecommendationRule> rulesToCreate,
            List<ActivityRecommendationRule> rulesToDelete
    ) {
        if (existingRule == null) {
            if (weight != 0) {
                rulesToCreate.add(ActivityRecommendationRule.create(option, category, weight));
            }
            return;
        }
        if (weight == 0) {
            rulesToDelete.add(existingRule);
            return;
        }
        existingRule.changeWeight(weight);
    }

    private record RuleKey(Long preferenceOptionId, Long activityCategoryId) {
    }
}
