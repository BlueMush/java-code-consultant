# 기능 분해 기준

"어디서 자를 것인가"에 대한 규칙. 코드를 새로 쓸 때와 리팩터링 신호를 판단할 때 적용한다.

## 메서드

- **한 메서드 = 한 추상화 수준.** "무엇을"과 "어떻게"를 한 몸에 섞지 않는다.
  이름 하나로 전체를 요약할 수 없으면 분해한다.

```java
// 나쁨: 검증 + 계산 + 저장이 한 수준에 섞임
public Receipt applyCoupons(Order order, List<Coupon> coupons) { /* 60줄 */ }

// 좋음: 최상위 메서드는 시나리오를 읽게 만든다
public Receipt applyCoupons(Order order, List<Coupon> coupons) {
    validateApplicable(order, coupons);
    long discount = totalDiscountWon(order, coupons);
    return issueReceipt(order, discount);
}
```

- **early return으로 중첩 제거.** guard clause를 위로 올리고 happy path를 평평하게.
  들여쓰기 3단계 이상은 분해 신호.
- 길이 자체보다 **요약 가능성**이 기준이지만, 30줄을 넘으면 일단 의심한다.
- boolean 파라미터로 동작이 갈리는 메서드는 두 메서드로 쪼갠다:
  `send(msg, true)` (X) → `sendNow(msg)` / `enqueue(msg)` (O).

## 분기 → 타입

- 같은 대상에 대한 `if/else`·switch 분기가 **여러 메서드에 반복**되면 타입 분기로 승격:
  sealed interface + 구현별 메서드 또는 pattern matching switch 한 곳으로 모은다.
- 분기 기준이 "무엇인가"(쿠폰 유형, 결제 수단)면 타입, "얼마인가"(임계값)면 그대로 조건문.

## 클래스

- **private 메서드 3개 이상이 같은 데이터 뭉치를 주고받으면** 그 뭉치가 클래스가 되고 싶은 것이다 — 파라미터 오브젝트 또는 도메인 객체로 추출.
- 생성자 의존성 5개 초과 = 책임 과다. 유스케이스별로 서비스를 쪼개거나 하위 정책 객체로 묶는다.
- "Util"·"Helper"·"Manager" 이름이 필요해지면 책임 정의가 실패한 것 — 데이터와 가장 가까운 도메인 객체로 로직을 옮긴다.
- 계산·정책 로직은 상태 없는 순수 객체(스프링 무의존)로 분리한다. 단위 테스트 대상 1순위.

## 패키지

- **도메인 기준으로 자른다**: `coupon/`, `order/`, `settlement/` — 기술 계층 기준(`controller/`, `service/`, `repository/` 최상위) 금지.
- 계층은 도메인 패키지 **안에서** 나눈다: `coupon/api/`, `coupon/application/`, `coupon/domain/`.
- 패키지 간 참조는 단방향 유지. 순환 참조가 생기면 공용 개념을 별도 패키지로 내리거나 이벤트로 결합을 끊는다.
- 다른 도메인 패키지의 내부(domain/repository)를 직접 참조하지 않는다 — 공개 서비스/이벤트를 통해서만.

## 리팩터링 판단 체크리스트

코드 리뷰·수정 시 아래에 해당하면 분해를 제안한다:

1. 메서드 이름에 `And`가 들어가거나 이름 짓기가 어렵다
2. 들여쓰기 3단계 이상 / guard 없이 else 사다리
3. 같은 switch/if 분기 구조가 2곳 이상 반복
4. private 메서드 그룹이 특정 필드 부분집합만 사용
5. 테스트 준비(given)가 10줄을 넘는다 — SUT가 너무 많이 안다는 뜻
6. 주석으로 구역을 나누고 있다 (`// ---- 검증 ----`) — 구역이 곧 메서드 경계

단, **동작 변경과 구조 변경을 한 커밋에 섞지 않는다.** 분해 리팩터링은 테스트 green 상태에서 별도 커밋으로.
