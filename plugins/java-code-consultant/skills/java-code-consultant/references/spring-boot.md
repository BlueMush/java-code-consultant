# Spring Boot 3.5 관례

## 의존성 주입

- **생성자 주입만 사용.** 필드 `@Autowired`·setter 주입 금지.
- 생성자가 하나면 `@Autowired` 생략. 필드는 `private final`.
  (팀이 Lombok을 쓴다면 `@RequiredArgsConstructor`가 같은 효과 — 프로젝트의 기존 방식을 따른다.)
- 생성자 파라미터가 5개를 넘으면 주입이 아니라 **클래스 책임 과다** 신호 — 분해를 검토한다(decomposition.md).

```java
@Service
public class SettlementService {
    private final OrderRepository orderRepository;
    private final SettlementPolicy policy;

    public SettlementService(OrderRepository orderRepository, SettlementPolicy policy) {
        this.orderRepository = orderRepository;
        this.policy = policy;
    }
}
```

## 설정 — @ConfigurationProperties + record

`@Value` 산재 금지. 설정은 접두사 단위로 record에 바인딩한다.

```java
@ConfigurationProperties(prefix = "app.coupon")
public record CouponProperties(int maxStack, long dailyCapWon, Duration reserveTtl) { }
```

- `@EnableConfigurationProperties(CouponProperties.class)` 또는 `@ConfigurationPropertiesScan`으로 등록.
- 검증이 필요하면 compact constructor 또는 `@Validated` + Bean Validation.

## 계층 책임 (요약 — 상세는 layers.md)

- **Controller**: 요청 파싱/검증(`@Valid`) → UseCase 호출 → 응답 DTO 변환. 분기·계산 금지.
- **Service**: UseCase 구현, 오케스트레이션 + 트랜잭션 경계. 도메인 규칙 자체는 도메인 객체로.
- **도메인**: 규칙·계산·상태 전이. 스프링 어노테이션 없는 순수 자바 — 단위 테스트가 가장 쉬운 곳.
- 계층별 DTO(Request/Command/Result/Response)와 포트 규칙은 layers.md, 접미사는 naming.md.

## 예외 처리 — @RestControllerAdvice + ProblemDetail

```java
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(CouponNotFoundException.class)
    ProblemDetail handleNotFound(CouponNotFoundException e) {
        var pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
        pd.setProperty("couponId", e.couponId());
        return pd;
    }
}
```

- 도메인 예외는 의미 있는 커스텀 예외(unchecked)로 던지고 어드바이스에서 상태코드로 매핑.
- Controller/Service 안의 try-catch로 예외를 삼키지 않는다. 복구 로직이 있을 때만 catch.
- 스택트레이스·내부 식별자를 응답 body에 노출하지 않는다.

## HTTP 클라이언트

- 신규 코드는 **`RestClient`** (동기) 사용. `RestTemplate` 신규 도입 금지, WebClient는 리액티브 스택일 때만.
- 타임아웃(connect/read)을 반드시 명시 설정한다. 기본 무한대기 금지.
- 외부 API 호출부는 별도 클라이언트 클래스로 감싸 서비스에서 인터페이스로 주입(테스트에서 mock 경계).

## 트랜잭션

- `@Transactional`은 **Service의 public 메서드**에. Controller·Repository 계층엔 붙이지 않는다.
- 조회 전용은 `@Transactional(readOnly = true)`.
- **롤백은 unchecked만.** `RuntimeException`·`Error`는 롤백하지만 **checked `Exception`은 롤백하지 않고 커밋**된다(Spring 기본값). 도메인 예외를 unchecked로 던지는 규칙(예외 처리 절)이 이 함정의 1차 방어다. checked까지 롤백하려면 메서드에 `@Transactional(rollbackFor = Exception.class)`, 또는 전역으로 `@EnableTransactionManagement(rollbackOn = ALL_EXCEPTIONS)`(Spring 6.2+ = Boot 3.5.x 번들 6.2.x에서 사용 가능).
- 트랜잭션 안에서 외부 API 호출·메시지 발행 금지 — 커밋 후 처리(`@TransactionalEventListener`,
  기본 phase가 `AFTER_COMMIT`)로 분리. DB 커넥션을 외부 지연에 볼모로 잡히지 않게 한다.
- self-invocation(같은 클래스 내부 호출)엔 프록시가 안 걸린다 — 트랜잭션 경계가 필요한 메서드는 다른 빈으로 분리.

## JPA 운영 설정

- **`spring.jpa.open-in-view=false` 명시.** 기본값 true는 영속성 컨텍스트·DB 커넥션을 응답
  직렬화까지 붙잡는다 — API 서버에서 커넥션 고갈의 단골 원인. 끄고, 지연 로딩은 서비스
  계층(트랜잭션 안)에서 fetch join·DTO 프로젝션으로 해결한다.
- 연관관계 기본 LAZY. N+1은 테스트에서 쿼리 카운트로 검출.

## 기타

- 로깅은 SLF4J + 파라미터 바인딩(`log.info("주문 처리 {}", orderId)`), 문자열 결합 금지.
  고객 식별정보·결제 raw 데이터는 로그에 남기지 않는다(마스킹).
- 스케줄러/비동기(`@Scheduled`, `@Async`)에는 예외 로깅 핸들러를 반드시 달아 조용한 실패를 막는다.
- Bean Validation은 API 경계(요청 DTO)에서, 도메인 불변식은 도메인 생성자에서 — 이중 방어.
