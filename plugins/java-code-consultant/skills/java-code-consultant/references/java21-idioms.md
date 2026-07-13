# Java 21 모던 이디엄

## record — 데이터 캐리어의 기본형

DTO·값 객체·이벤트·커맨드는 record로 작성한다. 검증은 compact constructor에서.

```java
public record CouponDiscount(String couponId, long amountWon) {
    public CouponDiscount {
        Objects.requireNonNull(couponId, "couponId");
        if (amountWon < 0) {
            throw new IllegalArgumentException("할인액은 음수일 수 없다: " + amountWon);
        }
    }
}
```

- 파생 값은 인스턴스 메서드로 추가한다. 필드를 늘리지 않는다.
- JPA 엔티티는 record가 될 수 없다 — 엔티티는 클래스로 두되, API 경계에서는 record DTO로 변환해 노출한다.
- record에 setter 성격의 `withX` 가 3개 이상 필요해지면 record가 아니라 빌더를 가진 클래스가 맞는지 재검토.

## sealed + pattern matching switch — 타입 분기

케이스가 닫혀 있는 분기(쿠폰 유형, 결제 수단, 상태)는 sealed 계층으로 모델링하고
switch 식으로 분기한다. `default`를 두지 않아야 케이스 추가 시 컴파일 에러로 누락을 잡는다.

```java
public sealed interface Coupon permits FixedCoupon, RateCoupon { }

long discountOf(Coupon coupon, long price) {
    return switch (coupon) {
        case FixedCoupon f -> Math.min(f.amountWon(), price);
        case RateCoupon r  -> price * r.rateBp() / 10_000;
    };
}
```

- `instanceof` 체인이 2개 이상이면 sealed+switch 전환 신호.
- record 패턴으로 분해 가능: `case RateCoupon(var id, int rateBp) -> ...`
- 문자열/enum 분기도 switch **식**(expression) 우선 — 문장형 switch + break 금지.

## Optional

- **반환 타입 전용.** "없을 수 있음"을 시그니처로 알릴 때만 쓴다.
- 필드·메서드 파라미터·컬렉션 원소로 금지. 파라미터가 선택적이면 오버로드나 별도 메서드.
- `opt.isPresent()` + `opt.get()` 조합 금지 — `map`/`filter`/`orElseThrow`/`ifPresent`로.
- 컬렉션 반환은 Optional로 감싸지 않는다. 빈 리스트를 반환한다.

## var

- 우변만 보고 타입을 알 수 있을 때만: `var items = new ArrayList<OrderLine>();` (O),
  `var result = service.process(req);` (X — 타입 명시).
- 스트림 중간 변수·다이아몬드 연산자와 함께 쓸 때 유용. 공개 API 시그니처와는 무관(로컬 전용).

## 불변 우선

- 내부 컬렉션을 그대로 반환하지 않는다: `return List.copyOf(items);`
- record 생성자에서도 방어 복사: `this.items = List.copyOf(items);`
- `Collections.unmodifiableList`보다 `List.copyOf` 우선(원본 변경에도 안전).
- 상수 컬렉션은 `List.of` / `Map.of`.

## Stream vs for

- 변환·필터·집계의 **직선 파이프라인**이면 Stream.
- 인덱스 필요, 중간 상태 변경, 예외 던지는 로직, 3단계 넘는 중첩이면 for가 낫다.
- Stream 안에서 side effect(`forEach`로 외부 컬렉션 채우기) 금지 — `collect`/`toList()`로.
- `toList()` (Java 16+) 를 `collect(Collectors.toList())`보다 우선. 단 반환값은 불변임에 유의.

## Text block

3줄 이상 문자열(SQL, JSON 예시, 템플릿)은 text block. 들여쓰기는 닫는 `"""` 위치로 제어.

```java
String query = """
        select o.id, o.amount_won
        from orders o
        where o.status = :status
        """;
```

## Virtual thread 주의점

- Spring Boot 3.5에선 `spring.threads.virtual.enabled=true`로 톰캣 가상 스레드 사용 가능.
- **`synchronized` 블록 안에서 블로킹 I/O를 하면 캐리어 스레드가 핀닝**된다 —
  가상 스레드 경로의 락은 `ReentrantLock`으로. (JDK 21 기준. JDK 24+에서 완화되나 21에선 유효)
- `ThreadLocal` 남용 금지 — 가상 스레드는 수만 개 생길 수 있어 메모리 폭증. Scoped value 도입 전까지는 명시적 파라미터 전달 우선.
- 가상 스레드 풀링 금지(풀링 자체가 반패턴) — `Executors.newVirtualThreadPerTaskExecutor()`.

## 금액 처리 (회사 표준)

- 원화 금액은 **`long` 원 단위**가 기본. 비율 계산 등 소수 연산이 불가피하면 `BigDecimal` +
  `RoundingMode` 명시. `double`/`float` 금액은 코드리뷰 reject 사유.
- 비율은 basis point(`int rateBp`, 10000 = 100%) 같은 정수 표현 우선.
- 나눗셈·비율 적용 지점마다 절사/반올림/올림 정책을 주석이 아니라 **메서드 이름**에 드러낸다:
  `discountFloorWon(...)`.
