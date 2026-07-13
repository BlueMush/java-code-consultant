# java-code-consultant

Java 21 코드 작성규칙 플러그인. 자바 코드를 작성·리뷰·리팩터링할 때
Claude가 자동으로 규칙을 적용한다.

## 담긴 규칙

- **Java 21 모던 이디엄** — record, sealed + pattern matching, SequencedCollection, Optional/var 규칙, 불변 우선, virtual thread 주의점, preview 기능 금지, 금액 long/BigDecimal 표준
- **테스트 컨벤션** — JUnit5 + AssertJ + Mockito: assertThat 통일, given-when-then, @ParameterizedTest 경계값, 모킹 최소화, @MockitoBean(구 @MockBean 대체), 무효 테스트 함정(List.of(null))
- **Spring Boot 3.5 관례** — 생성자 주입, @ConfigurationProperties record, 계층 책임, ProblemDetail 예외 처리, RestClient, 트랜잭션 경계, OSIV off
- **기능 분해 기준** — 메서드/클래스/패키지를 어디서 자를지, 리팩터링 신호 체크리스트

핵심 규칙은 [skills/java-code-consultant/SKILL.md](skills/java-code-consultant/SKILL.md),
상세는 `references/` 참조.

## 사용

- **자동 적용**: 자바 코드 관련 작업을 하면 스킬이 알아서 로드된다.
- **리뷰 커맨드**: `/java-code-consultant:review [경로]` — 경로 생략 시 git 변경분의
  `*.java`를 수집해 버그 가능성/스타일 구분으로 리뷰한다(수정 없이 보고만).

## 설치

```
/plugin marketplace add BlueMush/java-code-consultant
/plugin install java-code-consultant@java-code-consultant
```

## 규칙 수정

`skills/java-code-consultant/references/*.md`를 수정하고 `plugin.json`의 `version`을 올려 커밋,
루트 `CHANGELOG.md`에 기록. 설치된 사용자는 `/plugin update`로 반영.
수정 후 `tests/smoke.sh`로 스킬 발동을 재검증한다.
