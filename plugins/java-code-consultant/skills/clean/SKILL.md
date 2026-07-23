---
name: clean
description: 클린 아키텍처(Uncle Bob) 계층 규칙 — entity/usecase/adapter/framework 4계층, 의존성 규칙, 입력·출력 경계(Boundary)·프레젠터, 경계 횡단 DTO, 가시성·경계 강제. Clean Architecture layer conventions.
when_to_use: 프로젝트가 Uncle Bob 클린 아키텍처(동심원 4계층 — Entities/Use Cases/Interface Adapters/Frameworks)일 때 계층·패키지 설계, 인터랙터/경계/프레젠터/게이트웨이 작성·리뷰 시 명시 호출(bcc:clean). 공통 Java 규칙(이디엄·네이밍·테스트·Spring·분해)은 bcc:conventions 스킬이 담당한다.
---

# 클린 아키텍처(Uncle Bob) 계층 규칙

> 이 스킬은 **클린 아키텍처 계층 구조**만 다룬다. 모던 이디엄·네이밍·테스트·Spring·기능 분해
> 등 공통 규칙은 `bcc:conventions` 스킬을 함께 적용한다(자바 코드 작업 시 자동 발동).
> 포트&어댑터(2면 대칭) 스타일이면 `bcc:hexagonal` 스킬을 쓴다 — 아래 "헥사고날과의 차이" 참조.

## 의존성 규칙 (The Dependency Rule)

소스 코드 의존성은 **오직 안쪽(고수준)으로만** 향한다. 안쪽 원은 바깥 원의 존재(이름·타입)를
전혀 몰라야 한다. 제어 흐름이 바깥으로 나가야 할 때는 **의존성 역전(출력 경계 인터페이스)**으로
방향을 뒤집는다.

```
        (바깥) Frameworks & Drivers   ← web, db, 스프링, JPA, 외부 API
              Interface Adapters      ← Controller, Presenter, Gateway 구현
              Use Cases               ← Interactor + 입력/출력 Boundary
        (안쪽) Entities               ← 엔터프라이즈 규칙(순수 도메인)
   의존성: 항상 바깥 → 안쪽으로만
```

## 4계층과 패키지 표준

```
order/
├── entity/          Order, OrderPolicy … 엔터프라이즈 비즈니스 규칙(프레임워크 무의존, 순수 자바)
├── usecase/
│   ├── port/in/     PlaceOrderInputBoundary(=UseCase), PlaceOrderCommand, PlaceOrderResult
│   ├── port/out/    LoadOrderGateway, SaveOrderGateway, OrderOutputBoundary(Presenter 계약)
│   └── PlaceOrderInteractor              애플리케이션 비즈니스 규칙(입력 경계 구현)
├── adapter/         OrderController, OrderPresenter, OrderJpaGateway, OrderJpaEntity, OrderMapper
└── infrastructure/  스프링 설정·web·db 배선(Frameworks & Drivers)
```

- **Entities**: 가장 안쪽. 특정 앱·프레임워크와 무관한 핵심 규칙. 스프링/JPA 애노테이션 금지.
- **Use Cases(Interactor)**: 엔티티를 오케스트레이션. 입력 경계(InputBoundary)를 구현하고,
  출력은 출력 경계(OutputBoundary)·게이트웨이(Gateway) 인터페이스에만 의존한다.
- **Interface Adapters**: Controller(입력 변환)·Presenter(출력 변환)·Gateway 구현(영속/외부).
  두 세계(유스케이스 ↔ 프레임워크) 사이 데이터 형식을 변환만 한다.
- **Frameworks & Drivers**: 스프링·DB·웹. 배선과 세부기술만. 규칙 없음.

## Interactor / 입력 경계 (usecase, port/in)

- 입력 경계 = `동사+명사+InputBoundary`(또는 팀 관례상 `...UseCase`) 인터페이스, **경계당 메서드 1개**(ISP).
- 구현체는 `...Interactor`. 엔티티를 조합해 애플리케이션 규칙을 수행하고, 결과는 **출력 경계로
  넘긴다(반환값 대신 Presenter 호출)** — 전통적 클린 아키텍처의 특징. 반환형 스타일을 쓸 거면
  그 사실을 팀 규약으로 고정한다(혼용 금지).
- 입력은 전용 **Command/Query record** — compact constructor에서 입력 유효성(null·형식·범위)을
  자체 검증. 비즈니스 규칙 판단은 엔티티 몫.

```java
public interface PlaceOrderInputBoundary {
    void place(PlaceOrderCommand command);   // 결과는 OutputBoundary로 전달
}

public record PlaceOrderCommand(long memberId, List<Long> lineItemIds) {
    public PlaceOrderCommand {
        if (memberId <= 0) throw new IllegalArgumentException("memberId: " + memberId);
        lineItemIds = List.copyOf(lineItemIds);   // 방어복사(불변)
    }
}
```

## 출력 경계 · Presenter (port/out, adapter)

- **OutputBoundary** 인터페이스는 usecase가 소유하고 **Presenter(어댑터)가 구현**한다 — 출력 방향의
  의존성 역전. 인터랙터는 화면·응답 형식을 모른다.
- Presenter는 유스케이스 출력 모델(ViewModel/Response)로 변환만 한다. 계산·분기 금지.
- 반환형(Result) 스타일을 택했다면 OutputBoundary 대신 Result record로 대체할 수 있으나, 한
  코드베이스에서 하나로 통일한다.

## Gateway (port/out, adapter)

- 영속·외부 연동은 `...Gateway` 인터페이스(usecase 소유) + 어댑터 구현(의존성 역전).
- 시그니처는 **엔티티/도메인 타입만**. JPA 엔티티·외부 DTO·`Pageable` 등 기술 타입 노출 금지.
- 반환 규칙: **단건 부재는 `Optional`(또는 get+예외) — `null`·원시 센티넬 금지.** 목록은 빈 리스트.
- 도메인 엔티티와 JPA 엔티티는 **분리**하고 `...Mapper`로 변환(엔티티가 JPA에 오염되지 않게).

## DTO / 경계 횡단 (Crossing Boundaries)

경계를 넘는 데이터는 **단순 데이터 구조(record)** 로만 전달한다. 엔티티나 프레임워크 객체를
경계 밖으로 그대로 넘기지 않는다 — 계층마다 자기 DTO를 갖는다.

| 계층 | 타입 | 비고 |
|---|---|---|
| 웹(어댑터) | `XxxRequest` / `XxxResponse`(ViewModel) | Request에 Bean Validation |
| 유스케이스 | `XxxCommand` / `XxxQuery` / `XxxResult` | Command는 자기 검증 |
| 영속(어댑터) | `XxxJpaEntity` | 어댑터 밖 반출 금지 |

- 변환 소유: Controller가 Request→Command, Presenter가 출력→Response, Gateway/Mapper가 도메인↔JPA.
  안쪽(엔티티·인터랙터)은 바깥 DTO의 존재를 모른다.

## 가시성(접근제한자) · 경계 강제

계층 경계는 **컴파일러(접근제한자) 또는 아키텍처 테스트로 반드시 강제**한다. 둘 중 아무것도
없이 구현 클래스가 `public`이면 경계를 우회한 직접 호출을 막을 수 없으므로 위반이다.

| 요소 | 기본 가시성 |
|---|---|
| InputBoundary·OutputBoundary·Gateway 인터페이스 | `public` — 계층의 공개 계약 |
| Command·Query·Result·Response record | `public` — 경계를 넘는 데이터 구조 |
| Interactor·Presenter·Gateway 구현 | **배선 방식에 종속**(아래) |
| Controller·JpaEntity 등 어댑터 세부 | package-private + 경계로만 노출 |
| Entity(도메인) | 모듈 내 `public`, 내부 필드·보조 타입은 `private`/`final` |

**구현체 가시성은 배선이 결정한다:**
- 중앙 `@Configuration`이 `new XxxInteractor(...)`로 배선 → 구현체 `public` 불가피. 이때 경계는
  **아키텍처 테스트(ArchUnit·소스 정적 스캔)로 강제**한다.
- 패키지별 `@Configuration`, 또는 커스텀 스테레오타입 + 컴포넌트 스캔 → 구현체 **package-private 가능**.
- **철칙: 둘 중 하나(접근제한자 은닉 or 아키텍처 테스트)는 반드시 갖춘다.**

**권장 기본 배선: 스테레오타입(`@UseCase` 등) + 컴포넌트 스캔 + package-private 구현.** 배선
보일러플레이트 없이 인터랙터를 숨겨 컴파일러가 경계를 강제한다. Spring이 리플렉션으로 생성하므로
package-private도 문제없고, `@Transactional`은 인터페이스(JDK) 프록시로 걸린다. 중앙
`@Configuration`+`new`는 완전 프레임워크 무의존이 목표일 때의 **대안**이며, 그 경우 구현체가
`public`이 되어 아키텍처 테스트가 필수다.

```java
@UseCase
class PlaceOrderInteractor implements PlaceOrderInputBoundary {   // package-private → 은닉
    PlaceOrderInteractor(LoadOrderGateway load, SaveOrderGateway save) { ... }  // 생성자 주입
    @Override @Transactional
    public void place(PlaceOrderCommand c) { ... }               // 구현 메서드는 public(오버라이드 규칙)
    private void applyPolicy(...) { ... }                        // 헬퍼는 private
}
```

**트랜잭션 경계:** 인터랙터가 스프링 빈이면 `@Transactional`, 프레임워크 무의존 인터랙터를 중앙
배선하면 배선에서 `TransactionTemplate`로 감싼다. 리뷰 시 `@Transactional` 부재를 곧바로 원자성
위반으로 보지 말고 배선의 tx 래핑을 먼저 확인한다 — 어느 쪽도 없을 때만 위반.

## 헥사고날과의 차이 (선택 기준)

- **헥사고날(ports & adapters)**: 좌/우 대칭의 포트(in/out)와 어댑터. 도메인=application.domain.
  단순·실용적. 대부분의 Spring 프로젝트에 충분.
- **클린(동심원 4계층)**: **Entities와 Use Cases를 명시적으로 분리**하고, 출력에 **Presenter/
  OutputBoundary**를 두는 것이 특징. 엔터프라이즈 규칙(entity)이 여러 앱에서 재사용되거나,
  출력 표현이 복잡해 프레젠테이션을 경계로 뽑을 이득이 있을 때 값을 한다.
- 실무에선 둘이 거의 수렴한다 — 팀이 하나를 골라 **일관**되게 쓰는 것이 핵심. 섞지 않는다.
