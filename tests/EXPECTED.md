# 스모크 테스트 기대 결과

`fixtures/`의 두 파일에는 규칙 위반이 의도적으로 심어져 있다. 스킬이 정상 발동하면
리뷰가 아래 위반을 잡아야 한다. `smoke.sh`는 이 중 핵심 신호를 키워드로 검사한다.

## PointService.java (심은 위반 6개)

| # | 위반 | 근거 규칙 |
|---|---|---|
| 1 | 금액·적립률을 `double`로 계산 | SKILL 5, java21-idioms "금액 처리" |
| 2 | `Optional` 반환 메서드가 `null` 반환 | SKILL 3, java21-idioms "Optional" |
| 3 | 필드 `@Autowired` 주입 | SKILL 10, spring-boot "의존성 주입" |
| 4 | `Optional`을 파라미터로 사용 | java21-idioms "Optional" |
| 5 | `isPresent()` + `get()` 조합 | java21-idioms "Optional" |
| 6 | 상한 `10000` 매직 넘버 | (부차) 상수 추출 권장 |

## PointServiceTest.java (심은 위반 5개)

| # | 위반 | 근거 규칙 |
|---|---|---|
| 1 | `assertEquals`/`assertThrows` (JUnit assertion) | SKILL 7, testing "Assertion" |
| 2 | `List.of(null)` — SUT 호출 전 NPE, 무효 테스트 | testing "예외 검증" |
| 3 | `@MockBean` (deprecated) | SKILL 9, testing "슬라이스 vs 통합" |
| 4 | 단위 테스트에 `@SpringBootTest` | testing "슬라이스 vs 통합" |
| 5 | `testCalculate` 같은 무의미한 이름, `@DisplayName` 없음 | SKILL 8, testing "기본 구조" |

주의: 리뷰 출력이 위 표현과 정확히 일치할 필요는 없다 — 같은 문제를 지적하면 통과.
