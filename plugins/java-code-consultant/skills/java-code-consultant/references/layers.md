# 계층 원칙 (아키텍처 무관 개요)

> 구체 패키지 구조·클래스 분리·가시성 규칙은 프로젝트 아키텍처에 맞춰 **아키텍처 스킬을 명시
> 호출**한다: 포트&어댑터면 `java-code-consultant:hexagonal`, Uncle Bob 클린 아키텍처면
> `java-code-consultant:clean`. 이 파일은 두 방식에 공통인 원칙만 담는다.

## 공통 원칙

- **의존 방향은 안쪽으로만.** 바깥(web/db/framework) → 안쪽(application → domain). 도메인은
  스프링·JPA를 모른다. 바깥으로 나갈 땐 인터페이스(포트/경계)로 의존성을 역전한다.
- **Controller/입력 어댑터는 얇게.** ① 입력 검증(`@Valid`) ② 입력→Command 변환 ③ 출력 변환.
  분기·계산·트랜잭션이 보이면 안쪽으로 내린다.
- **계층별 DTO, 재사용 금지.** Request → Command/Query → Result → Response. 엔티티·도메인
  객체·JPA 엔티티를 API로 노출하지 않는다. 변환은 바깥 계층이 소유한다.
- **유스케이스 입력은 자기검증 record.** Command/Query는 compact constructor에서 null·형식·범위를
  검증한다. 비즈니스 규칙 판단은 도메인 몫.
- **포트/게이트웨이 반환**: 단건 부재는 `Optional`(또는 `get`+예외), `null`·원시 센티넬 금지.
  목록은 빈 리스트(never null).
- **경계는 강제한다.** 접근제한자(package-private) 또는 아키텍처 테스트 중 하나로 계층 우회
  호출을 막는다 — 경계 강제 장치 없는 `public` 구현은 위반이다.
- **트랜잭션 경계 = 유스케이스 실행 단위.** 스프링 빈이면 `@Transactional`, 프레임워크 무의존
  서비스면 배선의 `TransactionTemplate`. checked 예외는 롤백되지 않는다(spring-boot.md 참조).

구조를 실제로 잡거나 리뷰할 때는 위 아키텍처 스킬에서 패키지 트리·클래스 접미사·가시성 표를 확인한다.
