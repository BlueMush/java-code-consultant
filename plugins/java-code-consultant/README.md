# bcc (Bluemush Coding Consultant)

Java 21 코드 작성규칙 플러그인. 자바 코드를 작성·리뷰·리팩터링할 때
Claude가 자동으로 규칙을 적용한다. 호출 접두어는 `bcc:`.

## 담긴 규칙

- **Java 21 모던 이디엄** — record, sealed + pattern matching, SequencedCollection, Optional/var 규칙, 불변 우선, virtual thread 주의점, preview 기능 금지, 금액 long/BigDecimal 표준
- **아키텍처 계층 규칙(아키텍처별 스킬)** — 포트&어댑터는 `bcc:hexagonal`, Uncle Bob 클린 아키텍처는 `bcc:clean`. 공통 계층 원칙(의존 방향·계층별 DTO·경계 강제)은 `bcc:conventions`에
- **네이밍 규칙** — 계층 접미사 표, 메서드 동사 사전, 단위 접미사(amountWon·rateBp), 축약 금지, 도메인 용어집
- **테스트 컨벤션** — JUnit5 + AssertJ + Mockito: assertThat 통일, given-when-then, @ParameterizedTest 경계값, 모킹 최소화, @MockitoBean(구 @MockBean 대체), 무효 테스트 함정(List.of(null))
- **Spring Boot 3.5 관례** — 생성자 주입, @ConfigurationProperties record, ProblemDetail 예외 처리, RestClient, 트랜잭션 경계, OSIV off
- **기능 분해 기준** — 메서드/클래스/패키지를 어디서 자를지, 리팩터링 신호 체크리스트

핵심 규칙은 [skills/conventions/SKILL.md](skills/conventions/SKILL.md),
상세는 `references/` 참조. 아키텍처 계층은 `skills/hexagonal/` · `skills/clean/`.

## 사용

- **자동 적용**: 자바 코드 관련 작업을 하면 스킬이 알아서 로드된다.
- **리뷰 커맨드**: `/bcc:review [경로·PR URL]` — 생략 시 git 변경분의 `*.java`를 수집해
  규약 준수 여부를 리뷰한다(수정 없이 보고만).

## 설치

```
/plugin marketplace add BlueMush/java-code-consultant
/plugin install bcc@java-code-consultant
```

## 규칙 수정

`skills/conventions/references/*.md`(또는 `skills/hexagonal`·`skills/clean`)를 수정하고 `plugin.json`의 `version`을 올려 커밋,
루트 `CHANGELOG.md`에 기록. 설치된 사용자는 `/plugin update`로 반영.
수정 후 `tests/smoke.sh`로 스킬 발동을 재검증한다.
