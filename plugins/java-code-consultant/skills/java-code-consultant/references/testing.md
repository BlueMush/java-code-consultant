# 테스트 컨벤션 — JUnit 5 + AssertJ + Mockito

## 기본 구조

```java
class CouponDiscountCalculatorTest {

    private final CouponDiscountCalculator calculator = new CouponDiscountCalculator();

    @Nested
    @DisplayName("정액 쿠폰")
    class FixedCoupon {

        @Test
        @DisplayName("할인액이 상품 금액을 넘으면 상품 금액까지만 할인한다")
        void 상한_클램프() {
            // given
            var coupon = new FixedCoupon("C1", 5_000L);

            // when
            long discount = calculator.discountOf(coupon, 3_000L);

            // then
            assertThat(discount).isEqualTo(3_000L);
        }
    }
}
```

- 테스트 클래스는 SUT와 같은 패키지, 이름은 `{SUT}Test`.
- 시나리오 그룹은 `@Nested` + 한글 `@DisplayName`. 메서드 이름도 한글 허용.
- given–when–then 주석 구분. 준비가 1줄이면 주석 생략 가능.

## Assertion — AssertJ 통일

- `assertThat` 계열만 사용. `assertEquals`/`assertTrue`/`assertNull` 금지.
- 구체적 assertion 우선: `isEqualTo`보다 컬렉션엔 `containsExactly`,
  문자열엔 `startsWith`/`contains`, 객체엔 `usingRecursiveComparison` 또는
  `extracting("field1", "field2").containsExactly(...)`.
- 한 테스트에 관심사 하나. assertion이 5개 넘으면 테스트 분리 신호.
  연관 필드 묶음 검증은 `assertThat(order).extracting(...)` 한 방으로.

## 예외 검증

```java
assertThatThrownBy(() -> calculator.discountOf(coupon, -1L))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("금액");
```

- `assertThrows`(JUnit) 대신 `assertThatThrownBy`/`assertThatIllegalArgumentException`.
- 예외 **타입만이 아니라 메시지 일부**까지 검증해 엉뚱한 지점의 동일 타입 예외를 걸러낸다.
- **예외가 SUT에서 나는지 확인한다.** 준비 코드가 먼저 예외를 던지면 통과처럼 보여도 무효
  테스트다. 대표 함정: `List.of(null)`은 SUT 호출 전에 리스트 생성 단계에서 NPE를 던진다 —
  null 원소 케이스는 `Collections.singletonList(null)` 또는 `Arrays.asList((T) null)`로 만든다.
  람다 안에는 **SUT 호출 한 줄만** 넣고 준비 코드는 밖으로 뺀다.

## 경계값 — @ParameterizedTest

경계값·동치 클래스는 파라미터라이즈드로 표 형태를 유지한다.

```java
@ParameterizedTest(name = "가격 {0}원, 정률 {1}bp → 할인 {2}원")
@CsvSource({
        "10000, 1000, 1000",   // 10%
        "10000,    0,    0",   // 0%
        "    1, 1000,    0",   // 절사 경계
        "    0, 1000,    0",   // 가격 0
})
void 정률_할인_경계(long price, int rateBp, long expected) {
    assertThat(calculator.rateDiscount(price, rateBp)).isEqualTo(expected);
}
```

- 금액 로직은 최소 다음 케이스를 강제: 0원, 1원(절사 경계), 상한 일치, 상한 초과, 음수 입력(예외).
- `@CsvSource`로 표현이 안 되는 복합 객체는 `@MethodSource`.

## Mockito — 모킹 최소화

- **mock 대상은 외부 I/O 경계만**: repository, 외부 API 클라이언트, 시계(Clock), 메시지 발행.
- 값 객체·도메인 로직·계산기는 절대 mock 하지 않는다 — 실물을 만든다.
- `@ExtendWith(MockitoExtension.class)` + `@Mock`/`@InjectMocks`. 수동 `Mockito.mock()`은 지역적 필요 시에만.
- stubbing은 테스트가 실제로 쓰는 것만(strict stubbing이 기본이므로 불필요 stub은 실패한다 — 억지로 `lenient()` 붙이지 말고 stub을 지운다).
- 스터빙 스타일은 한 코드베이스에서 하나로: `when().thenReturn()` 또는 BDDMockito
  `given().willReturn()` 중 팀이 정한 쪽만. 한 파일 안 혼용 금지.
- 상호작용 검증(`verify`)은 **부수효과가 계약인 경우**(발행, 저장 호출)만. 반환값으로 검증
  가능한 것을 verify로 중복 검증하지 않는다.
- 시간 의존 로직은 `Clock`을 주입받게 만들고 테스트에선 `Clock.fixed(...)`.

## 테스트 더미 데이터

- 매직값 대신 의미가 드러나는 빌더/팩토리 메서드: `주문(가격 = 10_000L)` 스타일의 정적 팩토리를 테스트 픽스처로.
- 주민번호·카드번호 형태의 실제로 유효해 보이는 값 생성 금지 — 더미임이 명백한 표기 사용.

## 슬라이스 vs 통합

- 순수 로직은 스프링 컨텍스트 없이 순수 단위 테스트(가장 빠름, 기본값).
- 웹 계층은 `@WebMvcTest`, JPA 매핑·쿼리는 `@DataJpaTest`, 전체 와이어링 확인만 `@SpringBootTest`.
- `@SpringBootTest`를 단위 테스트 대용으로 쓰지 않는다(느림 + 실패 원인 흐려짐).
- 슬라이스에서 빈 대체는 **`@MockitoBean`/`@MockitoSpyBean`** (Spring Framework 6.2 /
  Boot 3.4+). 구 `@MockBean`/`@SpyBean`은 deprecated — 신규 코드 사용 금지, 리뷰 시 교체 지적.
