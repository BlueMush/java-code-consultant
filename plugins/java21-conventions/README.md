# java21-conventions

Java 21 코드 작성규칙 플러그인. `*.java` 파일을 작성·리뷰·리팩터링할 때
Claude가 자동으로 규칙을 적용한다.

## 담긴 규칙

- **Java 21 모던 이디엄** — record, sealed + pattern matching, Optional/var 규칙, 불변 우선, virtual thread 주의점, 금액 long/BigDecimal 표준
- **테스트 컨벤션** — JUnit5 + AssertJ + Mockito: assertThat 통일, given-when-then, @ParameterizedTest 경계값, 모킹 최소화
- **Spring Boot 3.5 관례** — 생성자 주입, @ConfigurationProperties record, 계층 책임, ProblemDetail 예외 처리, RestClient, 트랜잭션 경계
- **기능 분해 기준** — 메서드/클래스/패키지를 어디서 자를지, 리팩터링 신호 체크리스트

핵심 규칙은 [skills/java21-conventions/SKILL.md](skills/java21-conventions/SKILL.md),
상세는 `references/` 참조.

## 설치

```
/plugin marketplace add <java-code-consultant repo>
/plugin install java21-conventions@java-code-consultant
```

## 규칙 수정

`skills/java21-conventions/references/*.md`를 수정하고 `plugin.json`의 `version`을 올려 커밋.
설치된 사용자는 `/plugin update`로 반영.
