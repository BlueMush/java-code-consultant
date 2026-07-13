---
name: java-code-consultant
description: Java 21 코드 작성규칙. Java 코드를 작성·리뷰·리팩터링할 때 적용 — 모던 이디엄(record/sealed/pattern matching), JUnit5+AssertJ+Mockito 테스트 컨벤션, Spring Boot 3.5 관례, 기능 분해 기준.
when_to_use: 자바/Spring 코드 작성, .java 파일 생성·수정, 테스트 작성, 코드 리뷰, 리팩터링 요청 시. Use when writing, reviewing, or refactoring any Java code (Java 21, Spring Boot 3.5, JUnit 5 + AssertJ + Mockito).
---

# Java 21 코드 작성규칙

Java 코드를 **작성·수정·리뷰**할 때 아래 규칙을 적용한다. 각 영역의 상세 규칙과 예시는
`references/` 파일에 있다 — 해당 영역 작업을 시작하기 전에 반드시 읽는다:

| 작업 내용 | 먼저 읽을 파일 |
|---|---|
| 클래스·도메인 모델·비즈니스 로직 작성 | [references/java21-idioms.md](references/java21-idioms.md) |
| 테스트 작성·수정 | [references/testing.md](references/testing.md) |
| Spring 설정·트랜잭션·예외 처리·HTTP 클라이언트 | [references/spring-boot.md](references/spring-boot.md) |
| Controller/UseCase/Service/OutPort/Adapter/DTO 작성 | [references/layers.md](references/layers.md) |
| 클래스·메서드·변수 이름 짓기 | [references/naming.md](references/naming.md) |
| 메서드·클래스·패키지 구조 설계, 리팩터링 | [references/decomposition.md](references/decomposition.md) |

## 핵심 규칙 (항상 적용)

### 언어
1. **불변 우선.** 데이터 캐리어는 record, 컬렉션은 `List.copyOf` 등 불변 뷰로 노출. setter 기본 금지.
2. **타입 분기는 sealed + pattern matching switch.** `instanceof` 체인·`else` 부재 완전성 검사를 컴파일러에 맡긴다.
3. **`Optional`은 반환 타입 전용.** 필드·파라미터·컬렉션 원소에 쓰지 않는다. `null` 반환 금지.
4. **`var`는 우변에 타입이 명백할 때만.** 메서드 호출 결과처럼 타입이 안 보이면 명시 선언.
5. **금액은 `long`(원 단위) 또는 `BigDecimal`.** `double`/`float` 금액 표현은 절대 금지 — 발견 시 즉시 지적하고 반올림 정책(`RoundingMode`)을 명시하게 한다.
6. **preview 기능 금지.** `--enable-preview` 프로덕션 사용 불가 (String Template은 JDK 23에서 제거됨).

### 테스트
7. **AssertJ `assertThat` 통일.** `assertEquals`·`assertTrue` 등 JUnit assertion 금지.
8. **given–when–then 구조 + 한글 `@DisplayName`.** 경계값은 `@ParameterizedTest`로.
9. **모킹 최소화.** 값 객체·순수 로직은 실물 사용, mock은 외부 I/O 경계에만. 빈 대체는 `@MockitoBean`(구 `@MockBean`은 deprecated).

### Spring · 계층
10. **생성자 주입만.** 필드 `@Autowired` 금지. 단일 생성자면 어노테이션 생략.
11. **Controller는 얇게.** 검증→Command 변환→Response 변환만. 예외는 `@RestControllerAdvice` + `ProblemDetail`.
12. **의존은 안쪽으로만(클린 아키텍처).** Controller는 UseCase 인터페이스에, Service는 OutPort
    인터페이스에 의존. 엔티티·도메인 객체를 API로 노출하지 않고 계층별 DTO
    (Request→Command→Result→Response)로 변환한다.

### 구조 · 네이밍
13. **메서드는 한 추상화 수준.** 이름 하나로 요약되지 않으면 분해. 중첩은 early return으로 제거.
14. **패키지는 도메인 기준**(주문/정산/쿠폰...), 그 안에서 adapter/application/domain 분리.
15. **이름에 계층과 단위가 드러나게.** 클래스는 계층 접미사(`UseCase`/`Service`/`OutPort`/
    `Adapter`/`Request`...), 금액·비율 변수는 단위 접미사(`amountWon`, `rateBp`). URL에 동사 금지.

## 리뷰 시 동작

- **버그 가능성 위반**(금액 부동소수점, null 반환, 가변 컬렉션 노출, SUT 밖에서 예외가 나는
  무효 테스트)과 **스타일 위반**을 구분하고, 전자를 먼저 보고한다.
- 각 지적에는 근거 규칙(references 파일·항목)과 `파일:줄` 위치를 함께 인용한다.
- 리뷰만 요청받았으면 코드를 고치지 않는다. 수정은 명시적 요청이 있을 때만.

## 기존 코드 수정 시 (레거시 일관성)

새로 작성·수정하는 부분에는 규칙을 적용하되, **변경 범위 밖 코드를 규칙에 맞추려고 파일
전체를 리라이트하지 않는다** — 범위 밖 위반은 지적만 남긴다. 주변 코드와 규칙이 정면충돌하는
경우(예: 파일 전체가 필드 주입) 그 파일 안에서는 기존 스타일을 따르고 위반 사실을 보고한다.
