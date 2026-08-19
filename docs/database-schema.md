# Course 데이터베이스 설계 문서

> **Last updated:** 2026-08-14
> **Migration source of truth:** `src/main/resources/db/migration/V1__create_location_and_place_domain.sql`
> **Database:** PostgreSQL
> **Document rule:** 테이블·컬럼·제약조건·인덱스를 변경할 때는 같은 작업에서 Flyway와 이 문서를 함께 갱신한다.

---

## 📚 목차

1. [문서 개요 및 표기 약속](#1-문서-개요-및-표기-약속)
2. [전체 도메인 구조](#2-전체-도메인-구조)
3. [위치 도메인](#3-위치-도메인)
4. [태그 도메인](#4-태그-도메인)
5. [장소 도메인](#5-장소-도메인)
6. [인덱스 및 수집·동기화 정책](#6-인덱스-및-수집동기화-정책)
7. [Flyway 변경 이력](#7-flyway-변경-이력)

---

## 1. 문서 개요 및 표기 약속

이 문서는 나들이 코스 추천 서비스의 PostgreSQL 데이터베이스 구조를 정리한다. 실제 스키마 변경의 기준은 Flyway 마이그레이션이다.

| 태그 | 의미 |
|------|------|
| 🔑 PK | Primary Key |
| 🔗 FK | Foreign Key |
| 🟣 UQ | Unique |
| 📍 IDX | Index |
| ✅ CHECK | 값 범위 제약 |

현재 시간 컬럼은 PostgreSQL `TIMESTAMP`, Java `LocalDateTime`을 사용한다.

---

## 2. 전체 도메인 구조

```text
cities 1:N areas 1:N places
             │           │
             │ N:M       │ N:M
             ▼           ▼
          area_tags   place_tags
                \       /
                 ▼     ▼
                   tags
```

1. City 입력 후 사용자 키워드와 `area_tags`를 점수화해 Area를 추천한다.
2. Area 선택 후 사용자 키워드와 `place_tags`를 점수화해 앵커 후보와 일정 장소를 추천한다.
3. 특정 Place를 직접 입력하면, 앵커 후보 여부와 관계없이 이번 코스의 앵커가 된다.

---

## 3. 위치 도메인

### 📋 cities

도시 단위의 상위 위치다. 예: 서울, 대전.

| 컬럼 | 타입 | 태그 | 설명 |
|------|------|------|------|
| `id` | BIGINT | 🔑 PK | 도시 ID, identity |
| `code` | VARCHAR(50) | 🟣 UQ | 변경하지 않는 내부 도시 코드 |
| `name` | VARCHAR(100) | | 화면 표시명 |

### 📋 areas

도시 안에서 장소와 앵커 후보를 탐색하는 세부 지역이다. 예: 성수, 서촌, 용산.

| 컬럼 | 타입 | 태그 | 설명 |
|------|------|------|------|
| `id` | BIGINT | 🔑 PK | 세부 지역 ID, identity |
| `city_id` | BIGINT | 🔗 FK | `cities.id` |
| `code` | VARCHAR(50) | 🟣 UQ (도시 내) | 도시 안에서 유일한 내부 코드 |
| `name` | VARCHAR(100) | | 화면 표시명 |

**제약조건 및 인덱스**

- `uk_areas_city_code (city_id, code)` — 같은 도시 안에서 세부 지역 코드 중복 방지
- 📍 `idx_areas_city_id (city_id)` — 도시별 세부 지역 목록 조회

---

## 4. 태그 도메인

### 📋 tags

사용자 입력, Area, Place를 같은 기준으로 연결하는 공통 태그 사전이다. 허용 키워드는 코드의 `TagCode` enum에서 관리하며, 유사한 화면 선택지는 하나의 대표 태그로 정규화한다.

| 컬럼 | 타입 | 태그 | 설명 |
|------|------|------|------|
| `id` | BIGINT | 🔑 PK | 태그 ID, identity |
| `code` | VARCHAR(50) | 🟣 UQ | `TagCode` enum 값. 허용 키워드의 단일 기준 |

### 📋 area_tags

세부 지역이 어떤 활동·음식·분위기를 대표하는지 나타내는 Area와 Tag의 연결 테이블이다.

| 컬럼 | 타입 | 태그 | 설명 |
|------|------|------|------|
| `id` | BIGINT | 🔑 PK | 연결 ID, identity |
| `area_id` | BIGINT | 🔗 FK | `areas.id` |
| `tag_id` | BIGINT | 🔗 FK | `tags.id` |
| `weight` | INTEGER | ✅ CHECK | 해당 Area에서 태그의 대표성. 0~100 |

### 📋 place_tags

장소가 제공하는 활동·음식·분위기 특성을 나타내는 Place와 Tag의 연결 테이블이다.

| 컬럼 | 타입 | 태그 | 설명 |
|------|------|------|------|
| `id` | BIGINT | 🔑 PK | 연결 ID, identity |
| `place_id` | BIGINT | 🔗 FK | `places.id` |
| `tag_id` | BIGINT | 🔗 FK | `tags.id` |
| `weight` | INTEGER | ✅ CHECK | 해당 Place에서 태그의 대표성. 0~100 |

**💡 설계 포인트**

- `TagCode` enum은 추천 점수에 사용하는 대표 키워드와 화면 선택지 목록을 함께 관리한다. 예: `EXHIBITION("전시", "전시 관람", "갤러리", "미술관·박물관")`.
- 화면은 `TagCode.options`를 선택지로 노출한다. 어떤 선택지를 고르든 해당 `TagCode` 하나만 요청·저장·점수 계산에 사용한다.
- 사용자가 같은 `TagCode`에 속한 선택지를 여러 개 고르더라도, 추천 점수 계산 전에는 `TagCode` 기준으로 중복 제거한다.
- `tags`는 `TagCode`를 DB 관계에 사용할 수 있도록 보관하는 공통 사전이며, `area_tags`와 `place_tags`는 같은 태그를 다른 대상에 연결한다.
- `weight`는 태그 보유 여부가 아닌, 태그가 Area 또는 Place를 얼마나 대표하는지 나타낸다.
- 같은 Area 또는 Place에 같은 Tag를 중복 연결할 수 없다.
- Area와 Place는 서로 다른 테이블이므로 연결 테이블을 분리한다. 하나의 `target_type`, `target_id` 테이블로 합치면 FK 무결성을 보장할 수 없다.

---

## 5. 장소 도메인

### 📋 places

외부 제공자에서 수집·정제한 장소 마스터다. 추천은 외부 API 응답을 즉시 사용하지 않고, 이 테이블의 활성 장소를 대상으로 수행한다.

| 컬럼 | 타입 | 태그 | 설명 |
|------|------|------|------|
| `id` | BIGINT | 🔑 PK | 장소 ID, identity |
| `provider` | VARCHAR(30) | ✅ CHECK | 외부 제공자: `KAKAO`, `TOUR` |
| `provider_place_id` | VARCHAR(100) | 🟣 UQ (제공자 내) | 외부 제공자가 발급한 장소 ID |
| `area_id` | BIGINT | 🔗 FK | 소속 세부 지역. `areas.id` |
| `place_type` | VARCHAR(20) | ✅ CHECK | `ACTIVITY`, `FOOD`, `CAFE` |
| `name` | VARCHAR(200) | | 장소명 |
| `address_name` | VARCHAR(500) | | 지번 주소. nullable |
| `road_address_name` | VARCHAR(500) | | 도로명 주소. nullable |
| `latitude` / `longitude` | NUMERIC(10,7) | | 위도 / 경도 |
| `place_url` | VARCHAR(1000) | | 외부 상세 URL. nullable |
| `phone` | VARCHAR(50) | | 전화번호. nullable |
| `is_anchor_candidate` | BOOLEAN | | 앵커 후보 노출 여부. 기본 `false` |
| `selection_count` | BIGINT | ✅ CHECK | 코스 일정 확정 누적 횟수. 기본 `0`, 음수 불가 |
| `last_selected_at` | TIMESTAMP | | 마지막 일정 확정 시각. nullable |
| `is_active` | BOOLEAN | | 추천·수집 대상 활성 여부. 기본 `true` |
| `operating_hours` / `operating_days` | VARCHAR | | 운영 정보 원문. nullable |
| `last_synced_at` | TIMESTAMP | | 외부 정보 마지막 동기화 시각 |
| `created_at` / `updated_at` | TIMESTAMP | | 최초 생성 / DB 레코드 마지막 수정 시각 |

**제약조건**

- `uk_places_provider_place_id (provider, provider_place_id)` — 같은 제공자의 같은 장소 중복 수집 방지
- `ck_places_provider` — `KAKAO`, `TOUR`만 허용
- `ck_places_place_type` — `ACTIVITY`, `FOOD`, `CAFE`만 허용
- `ck_places_selection_count_non_negative` — `selection_count >= 0`

**💡 설계 포인트**

- 외부 제공자 원본 카테고리는 저장하지 않고, 서비스 분류인 `place_type`만 추천 기준으로 사용한다.
- `is_anchor_candidate=false`인 Place라도 사용자가 직접 선택하면 이번 코스의 앵커가 될 수 있다.
- `selection_count`는 후보 카드 노출 횟수가 아니라 장소를 실제 코스 일정으로 확정했을 때만 증가한다.
- `updated_at`은 모든 DB 수정 시 갱신되고, `last_synced_at`은 외부 정보 최신화 배치의 기준이다.

---

## 6. 인덱스 및 수집·동기화 정책

| 인덱스 / 제약 | 용도 |
|------|------|
| `uk_places_provider_place_id (provider, provider_place_id)` | 동일 외부 장소 중복 수집 방지 및 upsert 대상 식별 |
| `idx_places_area_anchor_active (area_id, is_anchor_candidate, is_active)` | 세부 지역별 활성 앵커 후보 조회 |
| `idx_places_area_type_active (area_id, place_type, is_active)` | 세부 지역·일정 유형별 활성 장소 후보 조회 |
| `idx_places_last_synced_at (last_synced_at)` | 장기간 동기화되지 않은 장소를 배치로 조회 |
| `idx_area_tags_tag_area (tag_id, area_id)` | 선택한 키워드로 Area 후보를 역방향 조회 |
| `idx_place_tags_tag_place (tag_id, place_id)` | 선택한 키워드로 Place 후보를 역방향 조회 |

```text
외부 API 수집
→ provider + provider_place_id로 기존 장소 확인
→ 없으면 Place 생성
→ 있으면 외부 정보 갱신 및 last_synced_at 갱신

last_synced_at이 오래된 장소 조회
→ 장소명 + 주소 또는 주변 좌표로 외부 API 재검색
→ 일치 장소 정보 갱신
```

---

## 7. Flyway 변경 이력

| 버전 | 파일 | 내용 |
|------|------|------|
| V1 | `V1__create_location_and_place_domain.sql` | 도시, 세부 지역, 장소, 공통 태그 및 초기 인덱스 생성 |
