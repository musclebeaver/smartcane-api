# 🦯 SmartCane API

스마트 지팡이 프로젝트 백엔드 (Spring Boot + Gradle + MySQL)  
React 프론트엔드와 연동하여, 스마트 지팡이 기능을 지원하는 API 서버입니다.

---

## 📌 개발 환경
- **Java**: 21
- **Spring Boot**: 3.5.4 (Gradle - Groovy)
- **Database**: MySQL 8.x
- **빌드 도구**: Gradle
- **IDE 권장**: IntelliJ IDEA

---

## 🚀 프로젝트 실행 방법

```bash
# 1. 레포 클론
git clone https://github.com/musclebeaver/smartcane-api.git
cd smartcane-api

# 2. 브랜치 생성 (작업 단위별)
git checkout -b feature/<기능명>

# 3. 애플리케이션 실행
./gradlew bootRun



# 스마트 지팡이 결제·버스정보 백엔드 (Spring Boot) – README

> 교통약자를 위한 **간편 교통결제 + 버스정류장 안내** 백엔드 서버 (Java 21 / Spring Boot / JPA / MySQL)

---

## 1) 프로젝트 개요

* **목표**: 스마트 지팡이 사용자(교통약자)를 위한 안전하고 쉬운 **승·하차 결제**와 **정류장/도착 안내**를 제공
* **핵심 구성**

  * 결제: NFC/QR 기반 태깅 → 승인/정산 → 대시보드
  * 버스 안내: 반경 내 정류장, 도착 정보, 접근성(저상버스 등) 강조
  * 운영: 요금 정책 엔진, 매일 배치 정산, 로그/모니터링

---

## 2) 아키텍처 요약

* **프런트**: React(PWA, 모바일 우선) + 관리자 콘솔(정산/요금/로그)
* **백엔드**: Spring Boot (Java 21) + JPA(MySQL) + Swagger(OpenAPI)
* **인증/보안**: OAuth2/JWT, 기기 바인딩(디바이스 토큰), TLS, 서명된 오프라인 토큰(옵션)
* **단말 연동**: 버스/지하철 단말 스캔(REST/gRPC/SDK), 혹은 지팡이 핸들에 NFC/QR 모듈

```
[SmartCane PWA]  <—HTTPS—>  [Spring Boot API]  <—>  [PG/결제]  [공공 Bus API]  [MySQL/Redis]
      |  Geolocation/TTS             | JPA/Batch      | 선불/후불      | TAGO/서울/ODsay  | 데이터/캐시
```

---

## 3) 기능 범위

### 3.1 결제/정산

* 결제수단 등록(토큰화/PG 보관), 승·하차 시 승인/정산, 운임 정책 적용, 환불/부분취소
* **오프라인 토큰**(선택): 단말 무통신 시 짧은 TTL + 서버 서명 검증 → 사후 동기화

### 3.2 버스정류장 안내

* 반경 내 정류장 검색, 정류장 상세/실시간 도착, 접근성 정보
* **프로바이더 전략**: 지역/상황에 따라 TAGO/서울/ODsay 등 중 하나로 도착정보 조회

### 3.3 관리자 콘솔(후순위)

* 요금정책 CRUD, 정산 실행/결과, 승차 로그 검색, 장애/캐시 모니터링

---

## 4) 최소 도메인 모델(요약)

* **User**(회원/보호자)
* **Device**(지팡이/폰): `id, type=CANEx|PHONE, serial, status, userId`
* **PaymentMethod**: `id, userId, type=CARD|WALLET, maskedPan, pgCustomerKey`
* **TapEvent**: `id, userId, deviceId, routeId, stopId, tapType(BOARD|ALIGHT), ts, authResult, farePreview`
* **Trip**: `id, userId, startTapId, endTapId, fareFinal, status`
* **FareRule**: `id, mode, baseFare, distanceStep, discountPolicy, timePolicy`
* **Stop/Route/RouteStop**: 정류장/노선/매핑
* **Settlement**: 일배치 결과 집계
* **ArrivalCache**(옵션): 짧은 TTL 실시간 도착 캐시

> **DB**: MySQL 8.x, 주요 테이블에 인덱스(특히 `TapEvent.ts`, `Stop(lat,lng)` 지리좌표 인덱스).

---

## 5) 공개 API 초안 (MVP)

### 5.1 인증

* `POST /api/auth/login`
* `POST /api/auth/register`
* `POST /api/auth/refresh`

### 5.2 결제수단

* `POST /api/payment-methods`
* `GET  /api/payment-methods`

### 5.3 승·하차(태그)

* `POST /api/tap`
  Body: `{ tapType: BOARD|ALIGHT, deviceId, routeId, stopId, token }`

### 5.4 여정/정산

* `GET  /api/trips`
* `POST /api/settlements/run`

### 5.5 버스정류장 안내(신규)

* `GET  /api/stops/nearby?lat=..&lng=..&radiusM=300&limit=20`
* `GET  /api/stops/{stopId}/arrivals?accessibleOnly=false`
* `GET  /api/stops/search?q=...`
* `GET  /api/routes/search?q=...`

### 5.6 관리자(요금·노선·정류장)

* `GET/POST/PUT/DELETE /api/admin/fare-rules`
* `GET/POST/PUT/DELETE /api/admin/routes`
* `GET/POST/PUT/DELETE /api/admin/stops`

---

## 6) 보안/규제 가이드

* **카드정보 저장 금지**: 토큰화, 민감정보는 PG가 보관
* **전송구간**: HTTPS/TLS 필수, JWT 만료 단축 + 리프레시 토큰 화이트리스트
* **오프라인 토큰**: 서버 서명(예: ECDSA) + 짧은 TTL(수분) + 단말 기기바인딩
* **로그**: PII/결제정보 마스킹, 접근로그 분리 보관

---

## 7) 버스 정보 프로바이더 전략

> 지역/커버리지/비용에 따라 선택. 공통 DTO로 어댑터화.

* **TAGO(국토부)**: 전국 정류장/노선 기초 데이터 적재(배치)
* **서울시/지자체**: 상세 도착/접근성(저상버스 등) 보강
* **ODsay(상용)**: 전국 통합 도착/길찾기(유료, 일원화 용이)

환경변수 예시:

```
BUS_PROVIDER=SEOUL|TAGO|ODsay
SEOUL_API_KEY=...
TAGO_API_KEY=...
ODsay_API_KEY=...
```

---

## 8) 실행/설정

### 8.1 필수 환경 변수

```
SPRING_PROFILES_ACTIVE=local
JWT_SECRET=change-me
PG_API_KEY=change-me
BUS_PROVIDER=SEOUL
SEOUL_API_KEY=xxxx
```

### 8.2 application.yml (샘플)

```yaml
server:
  port: 8081
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/smartcane?characterEncoding=utf8&serverTimezone=Asia/Seoul
    username: smartcane
    password: smartcane_pw
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        format_sql: true
logging:
  level:
    root: INFO
    org.springframework.security: DEBUG
```

### 8.3 빌드 & 실행

```
./mvnw clean package -DskipTests
java -jar target/smartcane-api.jar
```

---

## 9) Swagger/OpenAPI

* 런타임 접속: `http://localhost:8081/swagger-ui/index.html`
* 스펙: `http://localhost:8081/v3/api-docs`

---

## 10) 패키지 구조(제안)

```
site.musclebeaver.smartcane
 ├─ config        # 보안/JWT/Swagger/PG/Provider 설정
 ├─ domain        # JPA 엔티티(User, Device, PaymentMethod, TapEvent, Trip, Stop, Route...)
 ├─ dto           # 요청/응답 DTO
 ├─ repository    # Spring Data JPA 리포지토리
 ├─ service       # 비즈 로직 (결제, 운임, 정산, 버스도착, 지오검색)
 ├─ web           # 컨트롤러(REST)
 ├─ batch         # 일배치(정류장 적재, 정산)
 └─ integration
     └─ bus       # ArrivalProvider 어댑터(Seoul/Tago/ODsay)
```

---

## 11) API 사용 예시

### 11.1 승차 태그

```bash
curl -X POST http://localhost:8081/api/tap \
 -H 'Authorization: Bearer <JWT>' \
 -H 'Content-Type: application/json' \
 -d '{
   "tapType": "BOARD",
   "deviceId": "CANEX-001",
   "routeId": "1002",
   "stopId": "S12345",
   "token": "offline-or-online-token"
 }'
```

### 11.2 근처 정류장

```bash
curl "http://localhost:8081/api/stops/nearby?lat=37.5665&lng=126.9780&radiusM=350&limit=20"
```

### 11.3 정류장 도착정보

```bash
curl "http://localhost:8081/api/stops/S12345/arrivals?accessibleOnly=true"
```

---

## 12) 운임 정책(개요)

* **Base + Distance Step**: 기본요금 + 거리단계
* **할인/시간 정책**: 보호자/교통약자 할인, 시간대/요일별 요금, 환승/최대요금 캡
* 서버는 `FareRule` 테이블을 규칙 엔진 형태로 로딩하여 `TapEvent`/`Trip`에 적용

---

## 13) 정산(개요)

* **일배치**: 당일 `Trip` 집계 → PG 청구/정산 → `Settlement` 기록
* **대시보드**: 일별/노선별/사용자별 매출, 환불/부분취소 처리 플로우 제공

---

## 14) 접근성

* 큰 UI, 고대비/포커스, TTS(한줄 요약), 햅틱(모바일), 라이브 리전 알림
* 저상버스·혼잡도 등 접근성 관련 필드 최상단 표시

---

## 15) 로드맵(MVP → 확장)

1. **MVP**

   * /api/tap, 결제수단 CRUD, `/api/stops/nearby`, `/api/stops/{id}/arrivals`
   * TAGO 배치로 정류장/노선 적재, 서울/ODsay 중 1종 어댑터 완성
2. **운영**

   * 관리자 콘솔(요금, 정산, 로그), 모니터링/알림, Redis 캐시
3. **확장**

   * BLE 오프라인 토큰, 장애 폴백, 다도시/다프로바이더 혼합, 경로안내

---

## 16) 기여 가이드(간단)

* 브랜치 전략: `main`(배포) / `develop`(통합) / `feature/*`
* 커밋 컨벤션: `feat:`, `fix:`, `docs:`, `chore:`, `refactor:`
* PR 체크리스트: 테스트 통과, Swagger 반영, 보안 키 제외

---

## 17) 라이선스

* 내부 프로젝트(사내 전용). 외부 배포 시 추후 명시.

---

### 부록: 빠른 체크리스트

* [ ] 스프링 부트 프로젝트 부트스트랩
* [ ] JPA 엔티티(User/Device/PaymentMethod/TapEvent/Trip/Stop/Route)
* [ ] Swagger 설정
* [ ] `/api/tap` 컨트롤러/서비스/테스트
* [ ] `/api/stops/nearby`, `/api/stops/{id}/arrivals`
* [ ] TAGO 배치 or 서울/ODsay 어댑터 1종
* [ ] application.yml/ENV 정리
* [ ] 운영 로그/마스킹/보안 점검
