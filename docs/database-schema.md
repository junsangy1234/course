# Course 데이터베이스 설계 문서

> **Last updated:** 2026-08-07  
> **Migration source of truth:** `src/main/resources/db/migration/V1__*.sql ~ V3__*.sql`  
> **Database:** PostgreSQL 17  
> **Document rule:** DB 테이블·컬럼·제약조건·시드 데이터가 추가/수정/삭제될 때마다, 같은 변경에서 이 문서와 Flyway 마이그레이션을 함께 갱신한다.

---

## 1. 문서 개요 및 표기

이 문서는 나들이 코스 추천 서비스의 현재 PostgreSQL 스키마를 정리한다.  
실제 스키마 변경의 기준은 Flyway 마이그레이션이며, 이 문서는 사람이 빠르게 구조와 의도를 파악하기 위한 최신 설명서다.

| 태그 | 의미 |
|---|---|
| 🔑 PK | Primary Key |
| 🔗 FK | Foreign Key |
| 🟣 UQ | Unique 제약 |
| 📍 IDX | Index |
| ✅ CHECK | 값 범위 제약 |

### DB 변경 작업 규칙

1. 현재 개발 단계의 V1~V3은 계층별 통합 baseline이다. 같은 계층의 구조·시드 변경은 새 migration을 무분별하게 늘리지 말고 해당 baseline 파일을 수정한다.
2. baseline을 수정한 뒤에는 Flyway checksum이 달라지므로 개발 DB `public` 스키마를 재생성하고 migration을 다시 적용해야 한다. 배포 이후 적용된 migration은 수정하지 않는다.
3. 같은 작업에서 이 문서의 테이블 정의, 관계, 인덱스, 시드 데이터를 함께 갱신한다.
4. 데이터 수정용 SQL을 임시로 직접 실행하지 않는다. 초기 데이터와 구조 변경은 migration으로 남긴다.

---

## 2. 전체 구조

```text
┌──────────┐       ┌───────────┐
│  places  │ 1:N   │ place_tags│ N:1 ┌──────┐
└──────────┘──────▶└───────────┘─────▶│ tags │
                                     └──────┘

┌───────────────────┐ 1:N ┌────────────────────┐
│ preference_groups │─────▶│ preference_options │
└───────────────────┘     └─────────┬──────────┘
                                    N:1
                                     │
                         ┌──────────▼────────────────┐
                         │ activity_recommendation_   │
                         │ rules                      │
                         └──────────┬─────────────────┘
                                    N:1
                                     │
┌────────────────────────┐ 1:N ┌────▼────────────────────┐
│ activity_search_terms  │◀────│ activity_categories      │
└────────────────────────┘     │ └─ parent_id (self FK)  │
                               └─────────────────────────┘

┌──────────────────┐ 1:N ┌────────────────────┐ N:1 ┌────────────────────┐
│     courses      │─────▶│ course_preferences │─────▶│ preference_options │
└────────┬─────────┘     └────────────────────┘     └────────────────────┘
         │ 1:N
         ▼
┌──────────────────┐ N:1 ┌────────┐
│   course_items   │─────▶│ places │
└──────────────────┘     └────────┘

┌─────────────────────┐ 1:N ┌─────────────────────────────────┐ N:1 ┌─────────────────────┐
│ course_anchors  │─────▶│ course_anchor_activity_rules │─────▶│ activity_categories │
└─────────────────────┘     └─────────────────────────────────┘     └─────────────────────┘

┌─────────────────────┐ 1:N ┌───────────────────────────────────┐ N:1 ┌────────────────────┐
│ course_anchors  │─────▶│ course_anchor_preference_rules │─────▶│ preference_options │
└─────────────────────┘     └───────────────────────────────────┘     └────────────────────┘
```

### 추천 설정 데이터 흐름

```text
사용자 선택 옵션
→ activity_recommendation_rules 가중치 합산
→ 활동 대분류 점수 계산
→ course_anchor_preference_rules 가중치 합산
→ course_anchor_activity_rules 가중치 합산
→ AREA/HUB 탐색지·코스 후보 계산
→ 선택/자동 선택된 활동의 activity_search_terms 조회
→ Kakao 키워드 장소 검색
→ 후보 장소 점수 계산
```

활동·장소 후보의 계산 점수와 순위, 추천 진행 중인 코스 초안은 저장하지 않는다. 사용자가 저장 또는 공유하기로 한 코스와 그 안에서 확정한 장소만 저장한다.

---

## 3. Place 도메인

### 📋 places

외부 제공자에서 수집해 서비스에서 재사용할 수 있는 장소의 기본 정보다.

| 컬럼 | 타입 | 태그 | 설명 |
|---|---|---|---|
| `id` | BIGINT | 🔑 PK | 장소 ID, identity |
| `provider` | VARCHAR(20) | | `KAKAO`, `TOUR_API` |
| `provider_place_id` | VARCHAR(100) | | 제공자 내부 장소 ID. `provider`와 복합 unique |
| `place_type` | VARCHAR(20) | | `ACTIVITY`, `RESTAURANT`, `CAFE` |
| `name` | VARCHAR(200) | | 장소명 |
| `category_name` | VARCHAR(500) | | 제공자가 내려준 원본 카테고리명 |
| `address_name` | VARCHAR(500) | | 지번 주소 |
| `road_address_name` | VARCHAR(500) | | 도로명 주소, nullable |
| `latitude` | NUMERIC(10,7) | | 위도 |
| `longitude` | NUMERIC(10,7) | | 경도 |
| `place_url` | VARCHAR(1000) | | 제공자 상세 링크, nullable |

**제약조건**

- `uk_places_provider_place_id (provider, provider_place_id)`

**인덱스**

- 📍 `idx_places_type_latitude_longitude (place_type, latitude, longitude)`
  - 장소 캐시에서 유형을 먼저 고르고 위도·경도 bounding box로 후보를 줄일 때 사용한다.
  - 실제 원형 반경 검색과 경로 계산은 향후 PostGIS `geography` 도입 후 처리한다.

### 📋 tags

장소 추천에 쓰는 공통 태그 사전이다.

| 컬럼 | 타입 | 태그 | 설명 |
|---|---|---|---|
| `id` | BIGINT | 🔑 PK | 태그 ID |
| `tag_group` | VARCHAR(30) | | 태그 그룹. `code`와 복합 unique |
| `code` | VARCHAR(50) | | 변경하지 않는 내부 코드 |
| `display_name` | VARCHAR(100) | | 화면 표시명 |

`tag_group` 값: `FOOD_TYPE`, `ACTIVITY_TYPE`, `ATMOSPHERE`, `ENVIRONMENT`, `PURPOSE_FIT`, `TIME_FIT`, `WEATHER_FIT`, `PRICE_RANGE`, `FEATURE`

**제약조건**

- `uk_tags_group_code (tag_group, code)`

### 📋 place_tags

장소와 태그의 N:M 연결 테이블이다.

| 컬럼 | 타입 | 태그 | 설명 |
|---|---|---|---|
| `id` | BIGINT | 🔑 PK | 연결 ID |
| `place_id` | BIGINT | 🔗 FK | `places.id` |
| `tag_id` | BIGINT | 🔗 FK | `tags.id` |
| `source` | VARCHAR(30) | | `KAKAO_CATEGORY`, `CURATED`, `USER_FEEDBACK`, `DERIVED` |
| `confidence` | INTEGER | ✅ CHECK | 태그 신뢰도, 0~100 |

**제약조건**

- `uk_place_tags_place_tag (place_id, tag_id)`
- 📍 `idx_place_tags_tag_place (tag_id, place_id)`
  - 추천 시 태그에 연결된 장소를 역방향으로 조회하기 위한 인덱스다.
- `ck_place_tags_confidence CHECK (confidence BETWEEN 0 AND 100)`

---

## 4. 선호도 선택지 도메인

선호도는 코드 enum에 고정하지 않고 운영 데이터로 관리한다. UI 질문과 선택지를 변경해도 Java 코드를 수정하지 않기 위함이다.

### 📋 preference_groups

사용자에게 보여 줄 질문 묶음이다.

| 컬럼 | 타입 | 태그 | 설명 |
|---|---|---|---|
| `id` | BIGINT | 🔑 PK | 그룹 ID |
| `code` | VARCHAR(50) | 🟣 UQ | 그룹 코드 |
| `name` | VARCHAR(100) | | 질문 문구 |
| `allows_multiple_selection` | BOOLEAN | | 복수 선택 허용 여부 |
| `display_order` | INTEGER | ✅ CHECK | 0 이상의 노출 순서 |
| `is_active` | BOOLEAN | | 비활성화된 질문은 UI에서 숨김 |

**인덱스**

- 📍 `idx_preference_groups_active_order (is_active, display_order)`
  - 활성 질문 그룹을 노출 순서대로 조회하는 설정 화면/API에 사용한다.

초기 그룹:

| 코드 | 질문 | 선택 방식 |
|---|---|---|
| `COMPANION` | 누구와 만나나요? | 하나 |
| `OCCASION` | 어떤 자리인가요? | 하나 |
| `ENVIRONMENT` | 어디가 좋나요? | 하나 |
| `ENERGY` | 어떤 분위기가 좋나요? | 복수 |
| `HIGHLIGHT` | 오늘 무엇을 가장 중요하게 생각하나요? | 복수 |
| `DATE_MOOD` | 데이트 분위기를 더 골라볼까요? | 복수, 데이트 선택 시 UI 노출 |

### 📋 preference_options

각 질문에 속하는 실제 선택 항목이다.

| 컬럼 | 타입 | 태그 | 설명 |
|---|---|---|---|
| `id` | BIGINT | 🔑 PK | 옵션 ID |
| `preference_group_id` | BIGINT | 🔗 FK | `preference_groups.id` |
| `code` | VARCHAR(50) | | 그룹 안에서 유일한 내부 코드 |
| `name` | VARCHAR(100) | | 표시명 |
| `display_order` | INTEGER | ✅ CHECK | 0 이상의 노출 순서 |
| `is_active` | BOOLEAN | | 선택지 활성화 여부 |

**제약조건 및 인덱스**

- `uk_preference_options_group_code (preference_group_id, code)`
- 📍 `idx_preference_options_group_active_order (preference_group_id, is_active, display_order)`

초기 옵션은 `V2__create_recommendation_configuration.sql`이 관리한다.

---

## 5. 활동 추천 설정 도메인

### 📋 activity_categories

추천·검색에 사용하는 활동 카테고리다. `parent_id`로 대분류와 하위 카테고리를 구성할 수 있다.

| 컬럼 | 타입 | 태그 | 설명 |
|---|---|---|---|
| `id` | BIGINT | 🔑 PK | 활동 카테고리 ID |
| `parent_id` | BIGINT | 🔗 FK | `activity_categories.id`. 대분류면 null |
| `code` | VARCHAR(50) | 🟣 UQ | 변경하지 않는 운영 코드 |
| `name` | VARCHAR(100) | | 표시명 |
| `display_order` | INTEGER | ✅ CHECK | 0 이상의 노출 순서 |
| `is_active` | BOOLEAN | | 추천·검색 대상 활성화 여부 |

초기 대분류:

| 코드 | 표시명 |
|---|---|
| `CULTURE` | 전시·문화 |
| `WORKSHOP` | 공방·클래스 |
| `PLAY` | 놀이·게임 |
| `SPORT` | 스포츠·액티비티 |
| `OUTDOOR` | 산책·자연 |
| `SHOPPING` | 쇼핑·구경 |
| `HEALING` | 휴식·힐링 |
| `SEASONAL_EVENT` | 시즌·행사 |

**인덱스**

- 📍 `idx_activity_categories_parent_active_order (parent_id, is_active, display_order)`

### 📋 activity_search_terms

활동 카테고리를 실제 외부 API 검색어로 변환한다. 같은 활동에 여러 우선 검색어와 fallback 검색어를 둘 수 있다.

| 컬럼 | 타입 | 태그 | 설명 |
|---|---|---|---|
| `id` | BIGINT | 🔑 PK | 검색어 ID |
| `activity_category_id` | BIGINT | 🔗 FK | `activity_categories.id` |
| `provider` | VARCHAR(20) | | `KAKAO`, 향후 `TOUR_API` |
| `keyword` | VARCHAR(100) | | 실제 제공자에 넘길 검색어 |
| `search_priority` | INTEGER | ✅ CHECK | 0 이상의 호출 우선순위 |
| `is_fallback` | BOOLEAN | | 후보 부족 시에만 호출할지 여부 |
| `is_active` | BOOLEAN | | 검색어 활성화 여부 |

**제약조건 및 인덱스**

- `uk_activity_search_terms_category_provider_keyword (activity_category_id, provider, keyword)`
- 📍 `idx_activity_search_terms_category_active_priority (activity_category_id, is_active, is_fallback, search_priority)`

초기 카카오 검색어는 `V2__create_recommendation_configuration.sql`이 관리한다.

### 📋 activity_recommendation_rules

선호도 옵션이 특정 활동 대분류 점수에 주는 가중치다. 사용자별 점수나 추천 결과를 저장하는 테이블이 아니다.

| 컬럼 | 타입 | 태그 | 설명 |
|---|---|---|---|
| `id` | BIGINT | 🔑 PK | 규칙 ID |
| `preference_option_id` | BIGINT | 🔗 FK | `preference_options.id` |
| `activity_category_id` | BIGINT | 🔗 FK | `activity_categories.id` |
| `weight` | INTEGER | | 활동 점수에 합산할 가중치. 음수 가능 |

**제약조건 및 인덱스**

- `uk_activity_recommendation_rules_option_category (preference_option_id, activity_category_id)`
  - 선택한 `preference_option_id`별 활동 점수 집계의 선행 컬럼이므로 별도 중복 인덱스가 필요 없다.

예시:

```text
DATE(데이트) + WORKSHOP(공방·클래스) +25
QUIET(조용한) + CULTURE(전시·문화) +20
INDOOR(실내) + HEALING(휴식·힐링) +20
```

실제 현재 가중치는 로컬 규칙 관리 화면 또는 DB의 이 테이블을 기준으로 확인한다. 시드 기본값은 `V2` migration에 기록돼 있다.

---

## 6. 탐색지 추천 설정 도메인

큰 지역(예: 서울)을 선택한 경우, 사용자가 실제 장소를 찾기 전에 코스를 시작할 만한 중심지를 추천하기 위한 운영 데이터다. 사용자에게는 `AREA`, `HUB`를 따로 고르게 하지 않고, 모두 같은 코스 후보 카드로 보여 준다.

### 📋 course_anchors

코스 탐색의 중심점이다. `AREA`는 동네·거리 단위, `HUB`는 아이파크몰·코엑스처럼 특정 복합 시설 또는 명소 중심 단위다.

| 컬럼 | 타입 | 태그 | 설명 |
|---|---|---|---|
| `id` | BIGINT | 🔑 PK | 탐색지 ID |
| `code` | VARCHAR(50) | 🟣 UQ | 변경하지 않는 내부 코드 |
| `anchor_type` | VARCHAR(20) | ✅ CHECK | `AREA`, `HUB` |
| `parent_region_name` | VARCHAR(100) | 📍 IDX | 상위 지역명. 초기값은 `서울` |
| `name` | VARCHAR(100) | | 탐색지 표시명. 예: 성수동, 아이파크몰 |
| `description` | VARCHAR(500) | | 추천 카드에 표시할 설명, nullable |
| `latitude` | NUMERIC(10,7) | | 중심 위도 |
| `longitude` | NUMERIC(10,7) | | 중심 경도 |
| `radius_meters` | INTEGER | ✅ CHECK | 탐색지 중심 장소 검색 반경. 0 초과 |
| `display_order` | INTEGER | ✅ CHECK | 동점 또는 선호도 미선택 시 노출 순서 |
| `is_active` | BOOLEAN | | 비활성 탐색지는 추천 대상에서 제외 |

**인덱스**

- 📍 `idx_course_anchors_region_active_order (parent_region_name, is_active, display_order)`
  - 지역별 활성 중심지 후보를 먼저 제한한다. 최종 점수는 요청마다 계산되므로 `finalScore` 인덱스는 만들지 않는다.

초기 서울 탐색지:

| 코드 | 유형 | 이름 |
|---|---|---|
| `SEONGSU` | AREA | 성수동 |
| `SEOCHON` | AREA | 서촌 |
| `HONGDAE_YEONNAM` | AREA | 홍대·연남 |
| `ITAEWON` | AREA | 이태원 |
| `IPARK_MALL` | HUB | 아이파크몰 |
| `COEX` | HUB | 코엑스 |
| `THE_HYUNDAI_SEOUL` | HUB | 더현대 서울 |

### 📋 course_anchor_activity_rules

탐색지가 활동 카테고리에 주는 가중치다. 선호도에서 계산한 활동 점수와 이 가중치를 함께 사용해 탐색지·코스 후보 순위를 계산한다. 사용자별 점수나 추천 결과를 저장하는 테이블은 아니다.

| 컬럼 | 타입 | 태그 | 설명 |
|---|---|---|---|
| `id` | BIGINT | 🔑 PK | 규칙 ID |
| `course_anchor_id` | BIGINT | 🔗 FK | `course_anchors.id`. 탐색지 삭제 시 함께 삭제 |
| `activity_category_id` | BIGINT | 🔗 FK | `activity_categories.id` |
| `weight` | INTEGER | | 탐색지 점수에 합산할 가중치. 음수 가능 |

**제약조건 및 인덱스**

- `uk_course_anchor_activity_rules_anchor_category (course_anchor_id, activity_category_id)`
- 📍 `idx_course_anchor_activity_rules_category_anchor (activity_category_id, course_anchor_id)`
  - `uk_course_anchor_activity_rules_anchor_category`도 중심지별 활동 규칙 조인에 사용된다.

예시:

```text
성수동 + 쇼핑·구경 +25
성수동 + 시즌·행사 +25
아이파크몰 + 쇼핑·구경 +25
코엑스 + 놀이·게임 +20
```

### 📋 course_anchor_preference_rules

탐색지가 특정 선호도 옵션에 얼마나 적합한지를 기록한다. 추가 질문을 위한 데이터가 아니다. 사용자가 최초에 선택한 같은 선호도 옵션을 AREA/HUB 점수에 직접 반영하는 규칙이다.

| 컬럼 | 타입 | 태그 | 설명 |
|---|---|---|---|
| `id` | BIGINT | 🔑 PK | 규칙 ID |
| `course_anchor_id` | BIGINT | 🔗 FK | `course_anchors.id`. 탐색지 삭제 시 함께 삭제 |
| `preference_option_id` | BIGINT | 🔗 FK | `preference_options.id` |
| `weight` | INTEGER | | 탐색지 점수에 합산할 가중치. 음수 가능 |

**제약조건 및 인덱스**

- `uk_course_anchor_preference_rules_anchor_option (course_anchor_id, preference_option_id)`
- 📍 `idx_course_anchor_preference_rules_option_anchor (preference_option_id, course_anchor_id)`
  - 선택 선호도 옵션으로 직접 적합도 점수를 집계할 때 사용한다.

예시:

```text
성수동 + 볼거리·사진 +20
서촌 + 조용한 +20
아이파크몰 + 실내 +25
```

모든 탐색지와 선호도 조합을 저장하지 않는다. 의미 있는 적합도만 저장하고, 규칙이 없는 조합은 0점으로 처리한다.

---

## 7. 저장 코스 도메인

추천을 받는 과정의 임시 일정은 프론트엔드 상태와 API 요청 DTO로만 관리한다. 사용자가 `저장하기` 또는 `공유하기`를 선택할 때 해당 시점의 확정 코스를 이 도메인에 스냅샷으로 보관한다. 로그인·사용자 도메인은 아직 없으므로 현재는 사용자 소유자 정보 없이 코스 자체만 저장한다.

### 📋 courses

저장 또는 공유 대상인 하나의 나들이 코스 전체다. 현재는 선택한 지역 정보를 코스에 스냅샷으로 보관하며, 다음 단계에서 `course_anchors` 도메인을 도입하면 선택한 `AREA` 또는 `HUB`를 연결한다.

| 컬럼 | 타입 | 태그 | 설명 |
|---|---|---|---|
| `id` | BIGINT | 🔑 PK | 코스 ID |
| `selected_region_name` | VARCHAR(100) | | 사용자가 선택한 지역명. 예: 성수동, 서울 |
| `selected_region_latitude` | NUMERIC(10,7) | | 선택 지역 중심 위도 |
| `selected_region_longitude` | NUMERIC(10,7) | | 선택 지역 중심 경도 |
| `selected_region_radius_meters` | INTEGER | ✅ CHECK | 장소 추천에 적용할 지역 반경. 0 초과 |
| `scheduled_at` | TIMESTAMP | | 약속 시작 일시 |
| `ends_at` | TIMESTAMP | ✅ CHECK | 약속 종료 일시. 시작보다 뒤여야 함 |
| `status` | VARCHAR(20) | ✅ CHECK | `SAVED`, `ARCHIVED` |
| `created_at` | TIMESTAMP | | 저장 일시 |
| `updated_at` | TIMESTAMP | | 코스 수정 일시. 애플리케이션에서 갱신 예정 |

### 📋 course_preferences

저장 코스를 만들 때 선택한 선호도 옵션을 보관하는 N:M 연결 테이블이다. 이 정보는 추후 선호도별 저장 코스·장소 선택 패턴을 분석하는 데 사용한다.

| 컬럼 | 타입 | 태그 | 설명 |
|---|---|---|---|
| `id` | BIGINT | 🔑 PK | 연결 ID |
| `course_id` | BIGINT | 🔗 FK | `courses.id`. 코스 삭제 시 함께 삭제 |
| `preference_option_id` | BIGINT | 🔗 FK | `preference_options.id` |

**제약조건**

- `uk_course_preferences_course_option (course_id, preference_option_id)`

### 📋 course_items

저장된 코스 안의 개별 일정 항목이다. 추천 대기 칸은 저장하지 않으며, 각 항목은 사용자가 최종적으로 확정한 장소를 반드시 하나 가진다.

| 컬럼 | 타입 | 태그 | 설명 |
|---|---|---|---|
| `id` | BIGINT | 🔑 PK | 코스 항목 ID |
| `course_id` | BIGINT | 🔗 FK | 소속 `courses.id`. 코스 삭제 시 함께 삭제 |
| `sequence_no` | INTEGER | 🟣 UQ (코스 내) | 코스 순서. 1 이상 |
| `item_type` | VARCHAR(20) | ✅ CHECK | `ACTIVITY`, `MEAL`, `CAFE`, `OTHER` |
| `place_id` | BIGINT | 🔗 FK | 확정한 `places.id`. null 불가 |

예시:

```text
1번  ACTIVITY  place_id = 전시 A
2번  MEAL      place_id = 파스타 B
3번  CAFE      place_id = 카페 C
```

**인덱스**

- 📍 `idx_courses_status_created_at (status, created_at DESC)`
- 📍 `idx_course_items_course_sequence (course_id, sequence_no)`

---

## 8. 현재 추천 계산과 DB 저장 범위

### 저장하는 데이터

```text
장소 기본 정보      → places
장소 추천 태그      → tags, place_tags
질문·선택지         → preference_groups, preference_options
활동 대분류         → activity_categories
외부 검색 키워드    → activity_search_terms
선호도 점수 규칙    → activity_recommendation_rules
탐색지 기본 정보    → course_anchors
탐색지 활동 규칙    → course_anchor_activity_rules
탐색지 선호도 규칙  → course_anchor_preference_rules
저장된 코스 전체    → courses
저장 코스의 선호도  → course_preferences
코스의 개별 일정    → course_items
```

### 저장하지 않는 데이터

```text
이번 요청에서 계산한 활동 점수
이번 요청에서 계산한 장소 점수·순위
```

추천 중 선택값, 점수와 순위는 API 요청 처리 중 메모리에서 계산한다. 사용자가 저장 또는 공유하기로 한 시점에만 지역·일정·선호도·확정 장소를 `courses` 구조로 저장한다.

### 추천 조회 최적화 원칙

```text
선택한 선호도 옵션
→ DB에서 활동 카테고리별 SUM(weight) 집계
→ DB에서 지역별 코스 중심지 직접 점수·활동 기반 점수 집계
→ 중심지 후보 행만 애플리케이션으로 반환
```

- 추천 요청에서 규칙 엔티티 전체를 조회한 뒤 Java stream으로 필터·합산하지 않는다.
- `course_anchor_preference_rules`, `course_anchor_activity_rules`는 Repository의 native 집계 쿼리에서만 추천 점수 계산에 사용한다.
- 관리자 매트릭스는 질문 그룹, 옵션, 규칙 가중치를 각각 bulk query로 조회하며 lazy 연관관계를 반복 순회하지 않는다.

---

## 9. Flyway 변경 이력

| 버전 | 파일 | 내용 |
|---|---|---|
| V1 | `V1__create_place_domain.sql` | `places`, `tags`, `place_tags` 생성 |
| V2 | `V2__create_recommendation_configuration.sql` | 선호도·활동·검색어·AREA/HUB·모든 추천 규칙 스키마 및 현재 초기 설정 데이터 |
| V3 | `V3__create_course_domain.sql` | 저장 코스, 저장 코스 선호도, 확정 코스 항목 생성 |

---

## 10. 향후 DB 변경 예정 항목

아래 항목은 아직 테이블을 만들지 않았다.

- 장소 수집 이력·마지막 갱신 시각: 캐시 신선도 및 신규 장소 재탐색 판단
- 장소 태깅 작업·검수 이력: 자동 태그와 수동 큐레이션 변경 추적
- 사용자·코스 소유자: 로그인 및 개인별 코스 보관 기능 도입 시
- 코스 공유 정보: 공개 범위, 공유 토큰, 공유 만료 일시
- 추천 행동 이벤트: 추천 카드 노출·선택·제외·저장에 따른 추천 품질 개선
- 사용자 피드백: 클릭, 저장, 제외, 방문 완료에 따른 추천 품질 개선
- 날씨·영업시간·혼잡도·예약 정보: 컨텍스트 기반 점수 확장
