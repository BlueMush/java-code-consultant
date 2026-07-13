# 네이밍 규칙

이름만 보고 계층·역할·단위를 알 수 있어야 한다. 애매하면 이 파일의 표를 따른다.

## 클래스 접미사 (계층이 이름에 드러나게)

| 접미사 | 역할 | 예 |
|---|---|---|
| `Controller` | 웹 인바운드 어댑터 | `CouponController` |
| `UseCase` | 인바운드 포트(변경) | `IssueCouponUseCase` |
| `Query` | 인바운드 포트(조회) | `GetCouponQuery` |
| `Command` / `Result` | 유스케이스 입력/출력 record | `IssueCouponCommand` |
| `Service` | UseCase 구현체 | `IssueCouponService` |
| `OutPort` | 아웃바운드 포트 | `LoadCouponOutPort` |
| `Adapter` | 아웃바운드 구현 | `CouponPersistenceAdapter` |
| `JpaEntity` / `JpaRepository` | 영속 모델/스프링 데이터 | `CouponJpaEntity` |
| `Request` / `Response` | 웹 입출력 record | `IssueCouponRequest` |
| `Event` | 도메인 이벤트 — **과거형 동사** | `CouponIssuedEvent` |
| `Policy` | 규칙·판정 순수 객체 | `DiscountPolicy` |
| `Mapper` | 도메인↔엔티티 변환 | `CouponMapper` |
| `Properties` | `@ConfigurationProperties` record | `CouponProperties` |

- 도메인 모델(`Coupon`, `Order`)은 **접미사 없이 도메인 언어 그대로.** `CouponVO`·`CouponModel` 금지.
- `Util`/`Helper`/`Manager`/`Processor` 접미사 금지 — 책임 정의 실패 신호(decomposition.md).
- `Impl` 접미사 금지 — 구현체는 역할 이름으로(`IssueCouponService`, `CouponPersistenceAdapter`).

## 메서드 동사 사전

| 동사 | 계약 | 비고 |
|---|---|---|
| `find...` | 없으면 `Optional.empty()`/빈 리스트 | 예외 안 던짐 |
| `get...` | 없으면 예외 | 반환은 non-null 보장 |
| `load...` / `save...` | OutPort 조회/저장 | 포트 전용 |
| `exists...` / `count...` | boolean / long | |
| `create` / `issue` / `register` | 새 자원 생성 | 도메인 용어 우선(쿠폰은 issue) |
| `change...` / `cancel` / `expire` | 상태 전이 — 도메인 언어로 | 만능 `update` 지양 |
| `calculate...` / `apply...` | 순수 계산 | 부수효과 없음 |
| `to...` / `from...` | 변환(인스턴스/정적 팩토리) | DTO 규칙(layers.md) |
| `validate...` | 실패 시 예외 | boolean 반환이면 `is...`로 |

- boolean은 `is`/`has`/`can` 접두사: `isExpired()`, `canStack()`. 부정형 이름(`isNotX`) 금지.
- 이벤트 핸들러는 `on+과거형`: `onCouponIssued(...)`.

## 변수·필드 — 단위와 의미

- **금액·비율은 단위 접미사 필수**: `amountWon`, `feeWon`, `rateBp`(10000=100%), `quantityCount`.
  단위 없는 `amount`, `rate`는 리뷰 지적 대상 — 원인지 포인트인지, %인지 bp인지 이름이 답해야 한다.
- 기간·시각은 타입으로 해결: `Duration reserveTtl`, `Instant issuedAt`(과거분사+At),
  `LocalDate settledOn`. 정수로 둘 수밖에 없으면 `ttlSeconds`처럼 단위 접미사.
- 컬렉션은 복수형(`coupons`), Map은 `xByY`(`couponById`).
- 축약 금지: `cnt`, `tmp`, `mgr`, `svc`, `res`, `req`, `msg` → 전체 단어로.
  허용 축약: `id`, `url`, `api`, `dto`, `ttl`, `bp` (업계 표준만).
- 상수는 `UPPER_SNAKE_CASE` + 단위 포함: `MAX_DAILY_REWARD_WON`.

## 패키지·기타

- 패키지는 소문자 한 단어, 도메인 언어 단수형: `coupon`, `order`, `settlement`.
  구조는 layers.md 패키지 표준을 따른다.
- 한국어 발음 로마자 표기 금지(`jeongsan` ✗) — 영어 도메인 용어로 통일하고, 대응이 애매한
  용어는 팀 용어집에 등록한다. 시드 용어: 정산 `settlement`, 발급 `issue`, 적립 `reward`,
  차감 `deduct`, 소멸 `expire`, 환불 `refund`, 상한 `cap`, 절사 `floor`.
- 제네릭 타입 파라미터는 관례(`T`, `E`, `K`, `V`, `R`) 밖 필요 시 `대문자+명사`(`REQUEST`) 허용.
- 테스트 네이밍(한글 `@DisplayName`·메서드명)은 testing.md를 따른다.
