---
name: java21-conventions
description: Java 21 코드 작성규칙. Java 코드를 작성·리뷰·리팩터링할 때 적용 — 모던 이디엄(record/sealed/pattern matching), JUnit5+AssertJ+Mockito 테스트 컨벤션, Spring Boot 3.5 관례, 기능 분해 기준.
paths: "**/*.java"
---

# Java 21 코드 작성규칙

Java 코드를 **작성·수정·리뷰**할 때 아래 규칙을 적용한다. 각 영역의 상세 규칙과 예시는
`references/` 파일에 있다 — 해당 영역 작업을 시작하기 전에 반드시 읽는다:

| 작업 내용 | 먼저 읽을 파일 |
|---|---|
| 클래스·도메인 모델·비즈니스 로직 작성 | [references/java21-idioms.md](references/java21-idioms.md) |
| 테스트 작성·수정 | [references/testing.md](references/testing.md) |
| Controller/Service/설정 등 Spring 코드 | [references/spring-boot.md](references/spring-boot.md) |
| 메서드·클래스·패키지 구조 설계, 리팩터링 | [references/decomposition.md](references/decomposition.md) |

## 핵심 규칙 (항상 적용)

### 언어
1. **불변 우선.** 데이터 캐리어는 record, 컬렉션은 `List.copyOf` 등 불변 뷰로 노출. setter 기본 금지.
2. **타입 분기는 sealed + pattern matching switch.** `instanceof` 체인·`else` 부재 완전성 검사를 컴파일러에 맡긴다.
3. **`Optional`은 반환 타입 전용.** 필드·파라미터·컬렉션 원소에 쓰지 않는다. `null` 반환 금지.
4. **`var`는 우변에 타입이 명백할 때만.** 메서드 호출 결과처럼 타입이 안 보이면 명시 선언.
5. **금액은 `long`(원 단위) 또는 `BigDecimal`.** `double`/`float` 금액 표현은 절대 금지 — 발견 시 즉시 지적하고 반올림 정책(`RoundingMode`)을 명시하게 한다.

### 테스트
6. **AssertJ `assertThat` 통일.** `assertEquals`·`assertTrue` 등 JUnit assertion 금지.
7. **given–when–then 구조 + 한글 `@DisplayName`.** 경계값은 `@ParameterizedTest`로.
8. **모킹 최소화.** 값 객체·순수 로직은 실물 사용, mock은 외부 I/O 경계에만.

### Spring
9. **생성자 주입만.** 필드 `@Autowired` 금지. 단일 생성자면 어노테이션 생략.
10. **Controller는 얇게.** 도메인 로직은 도메인 객체/서비스로. 예외는 `@RestControllerAdvice` + `ProblemDetail`.

### 구조
11. **메서드는 한 추상화 수준.** 이름 하나로 요약되지 않으면 분해. 중첩은 early return으로 제거.
12. **패키지는 도메인 기준**(주문/정산/쿠폰...), 기술 계층(controller/service/repository) 기준이 아니다.

## 리뷰 시 동작

기존 코드를 리뷰할 때는 위 규칙 위반을 지적하되, **동작 변경 없는 스타일 지적과 버그 가능성이
있는 위반(금액 부동소수점, null 반환, 가변 컬렉션 노출)을 구분**해서 후자를 우선 보고한다.
