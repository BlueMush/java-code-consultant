# Changelog

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
