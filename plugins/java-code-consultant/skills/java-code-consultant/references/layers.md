# 계층 규칙 — 클린 아키텍처 (Controller / UseCase / Service / OutPort / Adapter / DTO)

의존 방향은 항상 **바깥→안**: adapter → application → domain. 반대 방향 참조는 컴파일부터 막는다.

```
[adapter/in/web]      Controller ─────▶ UseCase(인터페이스)
[application/port/in]                        △ 구현
[application/service]                     Service ─────▶ OutPort(인터페이스)
[application/port/out]                                        △ 구현
[adapter/out/*]                                            Adapter (persistence, external api)
[domain]              Service·도메인이 사용하는 순수 모델 — 스프링·JPA 무의존
```

## 패키지 표준

```
coupon/
├── adapter/
│   ├── in/web/            CouponController, IssueCouponRequest, IssueCouponResponse
│   └── out/persistence/   CouponPersistenceAdapter, CouponJpaEntity, CouponJpaRepository, CouponMapper
├── application/
│   ├── port/in/           IssueCouponUseCase, IssueCouponCommand, IssueCouponResult
│   ├── port/out/          LoadCouponOutPort, SaveCouponOutPort
│   └── service/           IssueCouponService
└── domain/                Coupon, CouponPolicy
```

## Controller 규칙 (adapter/in/web)

- 의존은 **UseCase 인터페이스에만.** Service 구체 클래스·OutPort·Repository 직접 호출 금지.
- 하는 일은 세 가지뿐: ① `@Valid` Request 검증 ② Request→Command 변환 ③ Result→Response 변환.
  분기·계산·트랜잭션이 나타나면 application/domain으로 내린다.
- 엔티티·도메인 객체를 응답으로 반환 금지 — 항상 Response record. `Map<String, Object>` 응답 금지.
- URL은 명사 리소스 기준(`POST /coupons`, `POST /coupons/{id}/issue`) — URL에 동사(`/getCoupon`) 금지.
  생성은 201, 본문 없는 성공은 204로 구분.
- 컨트롤러 하나 = 리소스 하나. 비대해지면 유스케이스 단위로 분리해도 된다.

```java
@RestController
@RequestMapping("/coupons")
public class CouponController {
    private final IssueCouponUseCase issueCouponUseCase;

    public CouponController(IssueCouponUseCase issueCouponUseCase) {
        this.issueCouponUseCase = issueCouponUseCase;
    }

    @PostMapping("/{couponId}/issue")
    @ResponseStatus(HttpStatus.CREATED)
    public IssueCouponResponse issue(@PathVariable String couponId,
                                     @Valid @RequestBody IssueCouponRequest request) {
        IssueCouponResult result = issueCouponUseCase.issue(request.toCommand(couponId));
        return IssueCouponResponse.from(result);
    }
}
```

## UseCase 규칙 (application/port/in)

- 이름은 `동사+명사+UseCase`, **인터페이스당 메서드 1개**(유스케이스 1개). 메서드가 2개 이상
  모이면 인터페이스를 쪼갠다 — 컨트롤러가 필요한 것만 의존하게(ISP).
- 조회 전용 유스케이스는 `Get/Find+명사+Query` 이름으로 구분한다(변경 유스케이스와 섞지 않는다).
- 입력은 전용 **Command/Query record** — 프리미티브 나열 파라미터 금지. Command는 compact
  constructor에서 **입력 유효성**(null·형식·범위)을 스스로 검증한다. 비즈니스 규칙 판단은 도메인 몫.
- 반환은 **Result record 또는 도메인 값.** 웹 Response·JPA 엔티티 반환 금지.

```java
public interface IssueCouponUseCase {
    IssueCouponResult issue(IssueCouponCommand command);
}

public record IssueCouponCommand(String couponId, long memberId) {
    public IssueCouponCommand {
        Objects.requireNonNull(couponId, "couponId");
        if (memberId <= 0) throw new IllegalArgumentException("memberId: " + memberId);
    }
}
```

## Service 규칙 (application/service)

- UseCase 구현체. 이름은 `동사+명사+Service`(`IssueCouponService`) — **유스케이스당 서비스 하나**가
  기본. 한 서비스가 여러 UseCase를 구현하기 시작하면 책임 과다 신호.
- 클래스 가시성은 **배선 방식에 종속**한다(아래 "가시성·경계 강제" 절) — 어느 경우든 외부
  계층은 인터페이스(포트)로만 접근한다.
- 역할은 오케스트레이션만: OutPort 로드 → 도메인 로직 호출 → OutPort 저장 → Result 변환.
  비즈니스 규칙(할인 판정, 상태 전이)은 도메인 객체에 있어야 한다.
- 트랜잭션 경계 = 유스케이스 실행 단위. 서비스의 `@Transactional`(스프링 빈일 때) **또는**
  배선의 `TransactionTemplate` 래핑(프레임워크 무의존 서비스일 때) — "가시성·경계 강제" 절 참조.
- **Service가 다른 UseCase/Service를 호출하지 않는다**(호출 사슬 금지). 공통 로직은 도메인
  서비스나 별도 컴포넌트로 내려 양쪽이 쓰게 한다.

## OutPort 규칙 (application/port/out)

- 이름은 `동사+명사+OutPort`: `LoadCouponOutPort`, `SaveCouponOutPort`, `SendCouponPushOutPort`.
- **application이 정의(소유)하고 adapter가 구현**한다 — 의존성 역전의 축.
- 시그니처는 도메인 타입만. JPA 엔티티·외부 API DTO·`Pageable` 같은 기술 타입 노출 금지.
- 조회(Load)와 저장(Save)을 분리하고 메서드를 좁게 유지한다. 메서드 10개짜리
  "만능 Repository 포트"는 계층만 늘린 가짜 추상화다.
- 반환 규칙: **단건 부재는 `Optional`(또는 `get`+예외) — `null`·원시 센티넬(0/-1)로 부재를
  숨기지 않는다.** 목록은 빈 리스트(never null). 부재 가능성을 시그니처에 드러내 호출자가
  반드시 처리하게 만든다. 특히 **반환 타입 자체가 `Optional`**이어야 하며, 구현이
  `.orElse(null)`이나 센티넬(예: 없으면 `0`)로 무너뜨리게 두지 않는다 — 정상값과 부재값이
  충돌하면(예: 실제 `0`과 "없음") 인증·금액 핫패스에서 오판을 만든다. `find`(Optional) 대
  `get`(예외) 구분은 naming.md의 동사 사전을 따른다.

```java
public interface LoadCouponOutPort {
    Optional<Coupon> loadById(String couponId);
}
```

## Adapter 규칙 (adapter/out/*)

- persistence: `XxxPersistenceAdapter`가 관련 OutPort 여러 개를 구현해도 된다(포트는 좁게,
  구현은 모아서). **JPA 엔티티(`XxxJpaEntity`)는 adapter 밖으로 절대 내보내지 않는다** —
  도메인↔엔티티 변환 매퍼(`XxxMapper`)를 함께 둔다.
- external api: `XxxApiAdapter` + RestClient, 타임아웃 명시(spring-boot.md HTTP 클라이언트 절).
- 어댑터에 비즈니스 로직 금지 — 변환과 I/O만. if 분기가 늘면 로직이 새고 있다는 신호.

## DTO 규칙 (계층별 접미사)

모든 DTO는 record. **계층마다 자기 DTO를 갖고, 계층을 넘어 재사용하지 않는다.**

| 계층 | 타입 | 소속 패키지 | 비고 |
|---|---|---|---|
| 웹 in | `XxxRequest` / `XxxResponse` | adapter/in/web | Request에 Bean Validation |
| 유스케이스 | `XxxCommand` / `XxxQuery` / `XxxResult` | application/port/in | Command는 자기 검증 |
| 영속 | `XxxJpaEntity` | adapter/out/persistence | adapter 밖 반출 금지 |

- 금지 패턴: Request를 Service까지 흘려보내기, JpaEntity를 application/웹으로 올리기,
  Response를 유스케이스가 반환하기. 변환 코드 몇 줄은 계층 보호의 값싼 보험이다.
- 변환 메서드 위치 규칙: 바깥 계층이 변환을 소유한다 — `request.toCommand(...)`(인스턴스),
  `Response.from(result)`(정적 팩토리). 안쪽 계층은 바깥 DTO의 존재를 모른다.

## 가시성(접근제한자) · 경계 강제

계층 경계는 **컴파일러(접근제한자) 또는 아키텍처 테스트로 반드시 강제**한다. 둘 중 아무것도
없이 구현 클래스가 `public`이면 포트를 우회한 직접 호출을 막을 수 없으므로 위반이다.

| 요소 | 기본 가시성 |
|---|---|
| UseCase·OutPort 인터페이스(포트) | `public` — 헥사곤의 공개 API |
| Command·Query·Result·Response·View record | `public` — 포트 계약으로 계층 경계를 넘음 |
| Service(UseCase 구현) | **배선 방식에 종속**(아래) |
| Adapter(`adapter/out/*`, `adapter/in/web`) | package-private + 포트로만 노출 |
| 도메인 모델 | 모듈 내 `public`, 내부 필드·보조 타입은 `private`/`final` |

**Service 가시성은 배선이 결정한다:**
- 중앙 `@Configuration`이 `new XxxService(...)`로 배선하는 경우 → 서비스는 `public` 불가피.
  이때 계층 경계는 **아키텍처 테스트(ArchUnit·소스 정적 스캔)로 강제**한다.
- 패키지별 `@Configuration`, 또는 `@UseCase`/`@WebAdapter` 같은 스테레오타입 + 컴포넌트 스캔
  → 서비스를 **package-private으로 둘 수 있다**(호출자는 포트로만 접근).
- **철칙: 둘 중 하나(접근제한자 은닉 or 아키텍처 테스트)는 반드시 갖춘다.** `public` 구현 +
  경계 강제 장치 없음 = 위반.

**트랜잭션 경계와의 연결:** 프레임워크 무의존 서비스를 중앙 배선하면, tx 경계를 서비스의
`@Transactional`이 아니라 배선에서 `TransactionTemplate`로 유스케이스 호출을 감싸 둔다.
리뷰 시 서비스에 `@Transactional`이 없다고 곧바로 원자성 위반으로 보지 말고, **배선의 tx
래핑(또는 서비스 `@Transactional`) 존재를 먼저 확인**한다 — 어느 쪽도 없을 때만 위반이다.

```java
// 중앙 배선 예: 무의존 서비스 + TransactionTemplate 로 tx 경계 부여
@Bean
IssueCouponUseCase issueCouponUseCase(LoadCouponOutPort load, SaveCouponOutPort save,
                                      TransactionTemplate tx) {
    var service = new IssueCouponService(load, save);          // 서비스는 스프링 무의존
    return cmd -> tx.execute(status -> service.issue(cmd));    // 배선이 tx 경계를 부여
}
```
