# Changelog

## 0.3.2 — 2026-07-21

- `references/layers.md` "가시성(접근제한자)·경계 강제" 절 신규 — 계층별 기본 가시성(포트·record
  =public, 구현·어댑터=은닉), Service 가시성은 배선 방식에 종속. **철칙: 접근제한자 은닉 또는
  아키텍처 테스트 중 하나로 경계를 강제하지 않은 `public` 구현 = 위반.** 무의존 서비스 +
  `TransactionTemplate` 배선 tx 경계 패턴 명시. 기존 "Service 가능하면 package-private" /
  "트랜잭션 경계=Service public 메서드" 문구를 배선 종속으로 정정.
- `references/layers.md` OutPort 반환 규칙 보강 — 단건 부재는 `Optional`(또는 `get`+예외),
  `null`·원시 센티넬(0/-1)로 부재를 숨기지 않는다. 반환 타입 자체가 `Optional`이어야 하며
  구현이 `.orElse(null)`로 무너뜨리지 않게(인증·금액 핫패스 오판 방지).
- `SKILL.md` 리뷰 절 — 스켈레톤/인터페이스 우선 PR에서 "구현 미완"은 위반 아님, 계약(반환
  타입·record 불변·네이밍·포트 도메인타입)만 지적.
- `commands/review.md` — `/review` 인자가 GitHub PR URL/번호면 `gh`로 변경 `.java` 수집·codegen
  제외·head SHA 실줄번호 인용·대량 PR 클러스터 분할.
- 근거: 실 PR(헥사고날 member 모듈) 리뷰에서 드러난 스킬-실코드 갭.

## 0.3.1 — 2026-07-16

- `references/spring-boot.md` 트랜잭션 절 보강 — checked `Exception`은 기본 롤백되지 않고 커밋되는
  함정 + `rollbackFor`/`@EnableTransactionManagement(rollbackOn = ALL_EXCEPTIONS)`(Spring 6.2+,
  Boot 3.5.x 번들) 대응 명시.
- `references/testing.md` 슬라이스 절 보강 — 한 테스트에 여러 `@…Test` 슬라이스 조합 미지원
  (하나 선택 + `@AutoConfigure…` 개별 추가), JUnit 5에서 `@ExtendWith(SpringExtension.class)` 불필요.
- `references/testing.md` "트랜잭션 테스트 함정" 절 신규 — `@Transactional` 테스트 자동 롤백이
  `RANDOM_PORT`/`DEFINED_PORT` 통합 테스트에서는 서버가 별도 트랜잭션이라 롤백되지 않음 → 명시 정리.
- 근거: deep-research 3회 교차검증(docs.spring.io 1차 출처). 상세는 리서치 보고서 참조.

## 0.3.0 — 2026-07-14

- `references/layers.md` 추가 — 클린 아키텍처 계층별 세부 규칙: Controller / UseCase(인바운드
  포트, 인터페이스당 메서드 1개) / Service(package-private, 유스케이스당 하나) / OutPort(Load·Save
  분리, 도메인 타입만) / Adapter(JPA 엔티티 반출 금지) / DTO(계층별 접미사, 계층 간 재사용 금지).
- `references/naming.md` 추가 — 클래스 접미사 표, 메서드 동사 사전(find/get/load/issue...),
  단위 접미사(amountWon·rateBp), 축약 금지 목록, 도메인 용어집 시드, Impl·Util 금지.
- SKILL.md 핵심 규칙 13→15개: 의존 방향(안쪽으로만)·계층별 DTO 변환, 계층·단위 네이밍 추가.
- 기존 파일 정합: spring-boot.md 계층 책임을 layers.md 참조 요약으로, decomposition.md 패키지
  예시를 layers.md 표준으로 통일.
- 스모크 확장: PointController.java 픽스처(계층·네이밍 위반 7종) + 검사 항목 8→11개.

## 0.2.0 — 2026-07-14

- `paths` frontmatter 제거: 신규 .java 파일 생성 요청에서 스킬이 로드되지 않을 수 있는 위험 제거.
  대신 `when_to_use`(한/영 트리거 문구)로 발동 강화.
- `/java-code-consultant:review` 커맨드 추가 — git 변경분 자동 수집 리뷰(보고 전용).
- 규칙 추가: SequencedCollection(getFirst/getLast), preview 기능 금지(String Template은 JDK 23에서
  제거), enum vs sealed 기준, `List.of` null 불허 주의, 무효 테스트 함정(SUT 밖 예외 / `List.of(null)`),
  `@MockitoBean`(구 `@MockBean` deprecated), 스터빙 스타일 혼용 금지, OSIV off + LAZY 기본,
  Lombok `@RequiredArgsConstructor` 허용 각주.
- 정확성 수정: `@TransactionalEventListener` phase 표기(기본이 AFTER_COMMIT).
- SKILL.md 리뷰 시 동작 강화: 근거 규칙·`파일:줄` 인용 의무, 레거시 일관성(변경 범위 밖 리라이트 금지) 규칙.
- `tests/` 추가: 위반 심은 픽스처 2종 + 기대 결과(EXPECTED.md) + 자동 스모크(`tests/smoke.sh`).
- plugin.json: repository 필드 추가.

## 0.1.0 — 2026-07-14

- 최초 버전: Java 21 모던 이디엄 / 테스트 컨벤션 / Spring Boot 3.5 관례 / 기능 분해 기준 스킬.
