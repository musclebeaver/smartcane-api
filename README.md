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
- **목표**: 스마트 지팡이 사용자(교통약자)를 위한 안전하고 쉬운 **승·하차 결제**와 **정류장/도착 안내**를 제공
- **핵심 구성**
  - 결제: NFC/QR 기반 태깅 → 승인/정산 → 대시보드
  - 버스 안내: 반경 내 정류장, 도착 정보, 접근성(저상버스 등) 강조
  - 운영: 요금 정책 엔진, 매일 배치 정산, 로그/모니터링

---

## 2) 아키텍처 요약
- **프런트**: React(PWA, 모바일 우선) + 관리자 콘솔(정산/요금/로그)
- **백엔드**: Spring Boot (Java 21) + JPA(MySQL) + Swagger(OpenAPI)
- **인증/보안**: OAuth2/JWT, 기기 바인딩(디바이스 토큰), TLS, 서명된 오프라인 토큰(옵션)
- **단말 연동**: 버스/지하철 단말 스캔(REST/gRPC/SDK), 혹은 지팡이 핸들에 NFC/QR 모듈

[SmartCane PWA] <—HTTPS—> [Spring Boot API] <—> [PG/결제] [공공 Bus API] [MySQL/Redis]
| Geolocation/TTS | JPA/Batch | 선불/후불 | TAGO/서울/ODsay | 데이터/캐시

markdown
복사
편집

---

## 3) 기능 범위
### 3.1 결제/정산
- 결제수단 등록(토큰화/PG 보관), 승·하차 시 승인/정산, 운임 정책 적용, 환불/부분취소
- **오프라인 토큰**(선택): 단말 무통신 시 짧은 TTL + 서버 서명 검증 → 사후 동기화

### 3.2 버스정류장 안내
- 반경 내 정류장 검색, 정류장 상세/실시간 도착, 접근성 정보
- **프로바이더 전략**: 지역/상황에 따라 TAGO/서울/ODsay 등 중 하나로 도착정보 조회

### 3.3 관리자 콘솔(후순위)
- 요금정책 CRUD, 정산 실행/결과, 승차 로그 검색, 장애/캐시 모니터링

---

## 4) 최소 도메인 모델(요약)
- **User**(회원/보호자)
- **Device**(지팡이/폰): `id, type=CANEx|PHONE, serial, status, userId`
- **PaymentMethod**: `id, userId, type=CARD|WALLET, maskedPan, pgCustomerKey`
- **TapEvent**: `id, userId, deviceId, routeId, stopId, tapType(BOARD|ALIGHT), ts, authResult, farePreview`
- **Trip**: `id, userId, startTapId, endTapId, fareFinal, status`
- **FareRule**: `id, mode, baseFare, distanceStep, discountPolicy, timePolicy`
- **Stop/Route/RouteStop**: 정류장/노선/매핑
- **Settlement**: 일배치 결과 집계
- **ArrivalCache**(옵션): 짧은 TTL 실시간 도착 캐시

> **DB**: MySQL 8.x, 주요 테이블에 인덱스(특히 `TapEvent.ts`, `Stop(lat,lng)` 지리좌표 인덱스).

---

## 5) 공개 API 초안 (MVP)
### 5.1 인증
- `POST /api/auth/login`
- `POST /api/auth/register`
- `POST /api/auth/refresh`

### 5.2 결제수단
- `POST /api/payment-methods`
- `GET  /api/payment-methods`

### 5.3 승·하차(태그)
- `POST /api/tap`  
  Body: `{ tapType: BOARD|ALIGHT, deviceId, routeId, stopId, token }`

### 5.4 여정/정산
- `GET  /api/trips`
- `POST /api/settlements/run`

### 5.5 버스정류장 안내(신규)
- `GET  /api/stops/nearby?lat=..&lng=..&radiusM=300&limit=20`
- `GET  /api/stops/{stopId}/arrivals?accessibleOnly=false`
- `GET  /api/stops/search?q=...`
- `GET  /api/routes/search?q=...`

### 5.6 관리자(요금·노선·정류장)
- `GET/POST/PUT/DELETE /api/admin/fare-rules`
- `GET/POST/PUT/DELETE /api/admin/routes`
- `GET/POST/PUT/DELETE /api/admin/stops`

---

## 6) 보안/규제 가이드
- **카드정보 저장 금지**: 토큰화, 민감정보는 PG가 보관
- **전송구간**: HTTPS/TLS 필수, JWT 만료 단축 + 리프레시 토큰 화이트리스트
- **오프라인 토큰**: 서버 서명(예: ECDSA) + 짧은 TTL(수분) + 단말 기기바인딩
- **로그**: PII/결제정보 마스킹, 접근로그 분리 보관

---

## 7) 버스 정보 프로바이더 전략
> 지역/커버리지/비용에 따라 선택. 공통 DTO로 어댑터화.
- **TAGO(국토부)**: 전국 정류장/노선 기초 데이터 적재(배치)
- **서울시/지자체**: 상세 도착/접근성(저상버스 등) 보강
- **ODsay(상용)**: 전국 통합 도착/길찾기(유료, 일원화 용이)

환경변수 예시:
