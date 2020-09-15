# SKT 예판시스템 : Tshop


### 서비스 시나리오

  ● 기능적 요구사항

  1. 고객이 집에서 휴대폰을 예약주문 한다. [예약신청]
  2. 해당 상품재고 여부를 확인하여 예약가능/불가능 여부를 알려준다 [예약불가]
  3. 예약가능 시 상품재고가 감소하고 자동으로 예약정보가 배정관리시스템으로 전달된다.
  4. 배정관리 시스템에서는 AI가 고객 위치 기준, 거리를 고려하여 최적의 대리점을 배정한다.
  5. 대리점 접수 및 할당이 완료되면 [예약접수완료] 상태를 주문관리에 전달한다. -> 이 후 배송 or 가까운대리점 방문으로 개통처리한다.
  6. 고객은 수령 이전에 예약주문을 취소할 수 있다. [예약취소]
  7. 예약취소가 접수되면 배정된 주문데이터를 삭제하고, 해당 상품재고를 원복한다.
  8. 정상 취소처리가 되면 [예약취소완료] 상태가 된다.

  ● 비기능적 요구사항

  1. 트랜잭션

  ● 상품재고가 없는 경우 예약은 접수처리가 되지 않는다. Sync 호출(Req/Res)
  
  2. 장애격리

  ● 배정관리 서비스가 되지않더라도 예약접수는 정상적으로 처리가 되어야한다. Async (event-driven)
  ●   Circuit breaker, fallback 처리 필요


### Event Storming 결과
  
http://www.msaez.io/#/storming/pgdJbGn4NPYfnMHR9xnCF72Qi1h1/every/94074311dd5c4ead0bc1936dd945e6cf/-MGqrwsnJeQJI0OZPGrm


### 이벤트도출

![event_1](https://user-images.githubusercontent.com/45332921/93048866-cfcdf800-f69a-11ea-9cd4-11519e9a8316.jpg)

### 부적격 이벤트 탈락

![event_2](https://user-images.githubusercontent.com/45332921/93048891-e3795e80-f69a-11ea-8c4b-2cefe99b1131.jpg)

### 액터, 커맨드 부착하여 읽기 좋게
![캡처2111](https://user-images.githubusercontent.com/31124658/93170244-cf4c6480-f761-11ea-9083-d181a1aeab43.JPG)

### 바운디드 컨텍스트로 묶기
![캡처2112](https://user-images.githubusercontent.com/31124658/93170240-ceb3ce00-f761-11ea-95fc-f25012045f20.JPG)
- app의 reservation, assignment, product 그와 연결된 command 와 event 들에 의하여 트랜잭션이 유지되어야 하는 단위로 그들 끼리 묶어줌

### 폴리시 부착 (괄호는 수행주체, 폴리시 부착을 둘째단계에서 해놔도 상관 없음. 전체 연계가 초기에 드러남)
![캡처2113](https://user-images.githubusercontent.com/31124658/93170245-cf4c6480-f761-11ea-95d9-7a66d79179e1.JPG)

### 폴리시의 이동과 컨텍스트 매핑 (점선은 Pub/Sub, 실선은 Req/Resp)
![캡처2114](https://user-images.githubusercontent.com/31124658/93170238-cd82a100-f761-11ea-90c5-627dde4c3b85.JPG)

### 뷰모델 추가
![캡처2115](https://user-images.githubusercontent.com/31124658/93170232-cc517400-f761-11ea-92c3-350a5ccf807a.JPG)

### 완성된 1차 모형
![캡처112](https://user-images.githubusercontent.com/31124658/93170184-afb53c00-f761-11ea-88cb-53742d03f553.JPG)

    - View Model 추가중
    
### 1차 모형에 대한 기능적/비기능적 요구사항을 커버하는지 검증
    - 고객이 상품을 선택하여 주문한다. (OK)
    - 주문하면 상품재고를 판단하여 예약신청 or 예약불가 처리한다. (OK)
    - 예약신청이 되면 재고가 변경되고, 대리점선택되어 예약이 접수된다. (OK)
    - 고객이 예약을 취소한다. (OK)
    - 예약이 취소되면 대리점배정이 취소되고, 재고가 변경된다. (OK)
    
# 구현:

분석/설계 단계에서 도출된 헥사고날 아키텍처에 따라, 각 BC별로 대변되는 마이크로 서비스들을 스프링부트로 구현하였다. 구현한 각 서비스를 로컬에서 실행하는 방법은 아래와 같다 (각자의 포트넘버는 8081 ~ 808n 이다)

```
cd reservation
mvn spring-boot:run

cd assignment
mvn spring-boot:run 

cd product
mvn spring-boot:run  

cd gateway
mvn spring-boot:run  

## DDD 의 적용
- 각 서비스내에 도출된 핵심 객체를 Entity 로 선언
  - 예약
  - 배정
  - 상품

## 적용 후 REST API 의 테스트
```
# 예약처리
http POST localhost:8088/reservations productId=1

# 취소처리
http PATCH localhost:8088/reservations/8 status="예약취소"

# 에약정보 확인
http localhost:8088/reservations  

```

## SAGA 패턴

## CQRS

## 동기식 호출 과 Fallback 처리

예약과 재고확인/재고변경 호출은 동기식 트랜잭션으로 처리 

## 비동기식 호출

## 폴리글랏 

## 서킷 브레이킹
- 부하테스터 siege 툴을 통한 서킷 브레이커 동작 확인:
- 동시사용자 2명
- 5초 동안 실시
![캡처112](https://user-images.githubusercontent.com/31124658/93170184-afb53c00-f761-11ea-88cb-53742d03f553.JPG)
- Retry 의 설정 (istio)
- Availability 가 높아진 것을 확인 (siege)
