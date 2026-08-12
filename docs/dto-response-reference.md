# DTO API 계약 문서

이 문서는 추천 기능의 Request/Response DTO 형태와 API 예시를 정리한다.
DTO를 추가하거나 필드를 변경할 때는 반드시 이 문서도 함께 갱신한다.

## DTO 패키지 구조

추천 응답 DTO는 API 성격에 따라 `activity`, `courseanchor`, `place` 패키지로 나눈다. 최상위 응답과 하위 항목 응답은 모두 별도 record 파일로 관리하며, 중첩 record를 사용하지 않는다.

```text
domain/recommendation/dto/response/
├─ activity/
│  ├─ ActivityRecommendationResponse
│  ├─ ActivityCandidateResponse
│  └─ ActivityScoreResponse
├─ courseanchor/
│  ├─ CourseAnchorRecommendationResponse
│  └─ CourseAnchorCandidateResponse
└─ place/
   ├─ PlaceRecommendationResponse
   └─ PlaceRecommendationCandidateResponse
```

활동 규칙 설정 응답도 `domain/activity/dto/response` 아래의 최상위 record 파일로 분리한다.

## 목차

- [ActivityRecommendationRuleMatrixResponse](#activityrecommendationrulematrixresponse)
- [ActivityRecommendationRequest / ActivityRecommendationResponse](#activityrecommendationrequest--activityrecommendationresponse)
- [PlaceRecommendationRequest / PlaceRecommendationResponse](#placerecommendationrequest--placerecommendationresponse)
- [PlaceCandidateResponse](#placecandidateresponse)
- [CourseAnchorRecommendationRequest / CourseAnchorRecommendationResponse](#courseanchorrecommendationrequest--courseanchorrecommendationresponse)
- [ErrorResponse](#errorresponse)

---

## ActivityRecommendationRuleMatrixResponse

### 사용처

`GET /api/recommendations/config`

사용자에게 보여줄 선호도 질문과 선택지, 선택지별 활동 카테고리 가중치를 반환한다.

### Response 예시

```json
{
  "activityCategories": [
    { "id": 1, "code": "CULTURE", "name": "전시·문화" },
    { "id": 2, "code": "WORKSHOP", "name": "공방·클래스" }
  ],
  "preferenceGroups": [
    {
      "id": 4,
      "code": "ENERGY",
      "name": "어떤 분위기가 좋나요?",
      "allowsMultipleSelection": true,
      "options": [
        {
          "id": 14,
          "code": "QUIET",
          "name": "조용한",
          "activityWeights": [
            { "activityCategoryId": 1, "weight": 20 },
            { "activityCategoryId": 2, "weight": 10 }
          ]
        }
      ]
    }
  ]
}
```

| 필드 | 의미 |
| --- | --- |
| `activityCategories` | 추천 가능한 대분류 활동 목록 |
| `preferenceGroups` | 사용자에게 보여줄 질문 그룹 |
| `options` | 질문 그룹 안의 선택지 |
| `activityWeights` | 선택지가 활동 카테고리에 주는 가중치 |

---

## ActivityRecommendationRequest / ActivityRecommendationResponse

### 사용처

`POST /api/recommendations/activities`

선택한 선호도 옵션으로 활동 카테고리 점수를 계산한다. `preferenceOptionIds`는 비어 있을 수 있으며, 이 경우 모든 활동 점수는 0점이다.

### Request

```json
{
  "preferenceOptionIds": [5, 10, 14]
}
```

### Response

```json
{
  "activities": [
    { "id": 2, "code": "WORKSHOP", "name": "공방·클래스", "score": 55 },
    { "id": 7, "code": "HEALING", "name": "휴식·힐링", "score": 55 }
  ]
}
```

`activities`는 점수 내림차순, 동점이면 활동명 오름차순으로 정렬된다.

---

## PlaceRecommendationRequest / PlaceRecommendationResponse

### 사용처

`POST /api/recommendations/places`

선택한 활동과 위치를 바탕으로 카카오 검색어를 결정하고 실제 장소 후보를 반환한다.

### Request

```json
{
  "activityCategoryId": 2,
  "preferenceOptionIds": [5, 10, 14],
  "latitude": 37.5326,
  "longitude": 126.9905,
  "radius": 3000
}
```

| 필드 | 필수 | 의미 |
| --- | --- | --- |
| `activityCategoryId` | 예 | 사용자가 확정한 활동 카테고리 ID |
| `preferenceOptionIds` | 예 | 활동 점수 계산에 쓸 선호도 옵션 ID 목록 |
| `latitude`, `longitude` | 예 | 검색 중심 좌표 |
| `radius` | 예 | 검색 반경(m), 0~20,000 |

### Response

```json
{
  "selectedActivity": {
    "id": 2,
    "code": "WORKSHOP",
    "name": "공방·클래스",
    "score": 55
  },
  "places": [
    {
      "providerPlaceId": "968751832",
      "name": "아뜰리에키마",
      "categoryName": "문화,예술 > 미술,공예 > 화랑",
      "addressName": "서울 용산구 이촌동 300-27",
      "roadAddressName": "서울 용산구 녹사평대로32길 47",
      "latitude": 37.5327049,
      "longitude": 126.9907904,
      "placeUrl": "http://place.map.kakao.com/968751832",
      "distanceMeters": 28,
      "activityScore": 55,
      "distanceScore": 30,
      "finalScore": 85
    }
  ]
}
```

현재 MVP 점수식은 다음과 같다.

```text
finalScore = activityScore + distanceScore
```

---

## PlaceCandidateResponse

### 사용처

`GET /api/places/candidates`

추천 규칙 없이 사용자가 전달한 검색어를 카카오에 직접 검색한 결과다.

### Request 예시

```text
GET /api/places/candidates?query=용산%20쇼핑&type=ACTIVITY&latitude=37.5326&longitude=126.9905&radius=3000&size=15
```

### Response

```json
[
  {
    "provider": "KAKAO",
    "providerPlaceId": "8142949",
    "type": "ACTIVITY",
    "name": "한강쇼핑센터",
    "categoryName": "가정,생활 > 상가,아케이드",
    "addressName": "서울 용산구 이촌동 300-27",
    "roadAddressName": "서울 용산구 이촌로 224",
    "latitude": 37.5200987,
    "longitude": 126.9718331,
    "placeUrl": "http://place.map.kakao.com/8142949",
    "distanceMeters": 2155
  }
]
```

---

## CourseAnchorRecommendationRequest / CourseAnchorRecommendationResponse

### 사용처

`POST /api/course-anchors/recommendations`

사용자가 선택한 상위 지역 안에서 `AREA`(성수동·서촌 등)와 `HUB`(코엑스·아이파크몰 등)를 하나의 후보 목록으로 추천한다.

요청한 상위 지역에 활성 코스 중심지가 하나도 없으면 `404 Not Found`를 반환한다.

### Request

```json
{
  "regionName": "서울",
  "preferenceOptionIds": [1, 11, 14, 19],
  "size": 5
}
```

| 필드 | 필수 | 의미 |
| --- | --- | --- |
| `regionName` | 예 | 사용자가 선택한 상위 지역명. 공백만 있는 값은 불가 |
| `preferenceOptionIds` | 예 | 선택한 선호도 옵션 ID 목록. 빈 배열은 가능 |
| `size` | 아니오 | 반환할 최대 후보 수. 1~20이며 생략 시 서비스 기본값 적용 |

### Response

```json
{
  "regionName": "서울",
  "activityScores": [
    {
      "activityCategoryId": 1,
      "code": "CULTURE",
      "name": "전시·문화",
      "score": 75
    },
    {
      "activityCategoryId": 2,
      "code": "WORKSHOP",
      "name": "공방·클래스",
      "score": 55
    }
  ],
  "courseAnchors": [
    {
      "rank": 1,
      "id": 1,
      "code": "SEONGSU",
      "name": "성수동",
      "type": "AREA",
      "description": "편집숍과 팝업스토어, 카페가 모인 지역",
      "latitude": 37.5446,
      "longitude": 127.0557,
      "radiusMeters": 1500,
      "directPreferenceScore": 20,
      "activityBasedScore": 33.25,
      "finalScore": 53.25
    }
  ]
}
```

| 필드 | 의미 |
| --- | --- |
| `activityScores` | 동일한 선호도 입력으로 계산한 활동 카테고리 점수 |
| `courseAnchors` | 최종 점수 내림차순 후보 목록 |
| `rank` | 목록 내 추천 순위. 1부터 시작 |
| `type` | `AREA` 또는 `HUB` |
| `directPreferenceScore` | 선호도 옵션과 중심지를 직접 연결한 규칙의 합계 |
| `activityBasedScore` | 활동 점수와 중심지 활동 규칙으로 계산한 기여 점수 |
| `finalScore` | `directPreferenceScore + activityBasedScore` |

점수 계산 상세는 [course-anchor-recommendation-flow.md](course-anchor-recommendation-flow.md)를 따른다.

---

## ErrorResponse

비즈니스 예외, 요청 검증 실패, HTTP 메서드 오류 및 예상하지 못한 서버 오류는 공통 오류 응답을 반환한다. 성공 응답의 기존 형식은 변경하지 않는다.

```json
{
  "code": "COURSE_ANCHOR_NOT_FOUND",
  "message": "해당 지역의 코스 중심지를 찾을 수 없습니다."
}
```

| 필드 | 의미 |
| --- | --- |
| `code` | 클라이언트가 분기 처리에 사용할 `ErrorCode` 이름 |
| `message` | 사용자에게 보여줄 수 있는 오류 메시지 |

---

## 응답 데이터 저장 정책

| Response | 데이터 출처 | 요청 결과 저장 여부 |
| --- | --- | --- |
| `ActivityRecommendationRuleMatrixResponse` | 자체 DB의 선호도·활동·규칙 테이블 | 저장하지 않음 |
| `ActivityRecommendationResponse` | 자체 DB 규칙을 요청마다 계산 | 저장하지 않음 |
| `PlaceRecommendationResponse` | 자체 DB 규칙·검색어 + 카카오 API | 저장하지 않음 |
| `PlaceCandidateResponse` | 카카오 API | 저장하지 않음 |
| `CourseAnchorRecommendationResponse` | 자체 DB의 중심지·선호도·활동 규칙을 요청마다 계산 | 저장하지 않음 |
