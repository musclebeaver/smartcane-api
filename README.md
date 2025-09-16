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

## 📂 도메인별 문서
- [Device 도메인](docs/README_DEVICE.md)  
- [결제·정산 도메인](docs/README_PAYMENT.md)  
- [버스 안내 도메인](docs/README_BUS.md)  
- [인증/보안 도메인](docs/README_AUTH.md)
  
---
## 🚀 프로젝트 실행 방법


# 1. 레포 클론
- git clone https://github.com/musclebeaver/smartcane-api.git
- cd smartcane-api

# 2. 브랜치 생성 (작업 단위별)
git checkout -b feature/<기능명>

# 3. 애플리케이션 실행
./gradlew bootRun

---

# 💳 결제·버스정보 기능 사양

> 교통약자를 위한 **간편 교통결제 + 버스정류장 안내** 백엔드 서버 (Java 21 / Spring Boot / JPA / MySQL)

---

## 1) 프로젝트 개요
- **목표**: 스마트 지팡이 사용자(교통약자)를 위한 안전하고 쉬운 **승·하차 결제**와 **정류장/도착 안내**를 제공
- **핵심 구성**
  - 결제: NFC/QR 기반 태깅 → 승인/정산 → 대시보드
  - 버스 안내: 반경 내 정류장, 도착 정보, 접근성(저상버스 등) 강조
  - 운영: 요금 정책 엔진, 매일 배치 정산, 로그/모니터링
# 📡 BLE 오프라인 토큰 서비스 흐름

BLE를 이용해 네트워크가 불안정한 상황에서도 **저전력·근거리**로 결제 검증을 수행하는 서비스 구조입니다.  
오프라인 상태에서도 승·하차 태깅을 가능하게 하고, 온라인 복구 시 서버에서 최종 정산을 처리합니다.

---

## 🔹 주요 구성 요소
- **지팡이/폰(클라이언트)**: 사용자가 들고 다니는 기기(앱)
- **차량 단말(Validator)**: 버스에 설치된 검증기(리더기)
- **백엔드 API**: Spring Boot 서버
- **키/토큰 서비스**: 서버 내부에서 오프라인 토큰 발급·검증 담당
- **PG/정산 엔진**: 결제, 운임, 정산 처리

---

## 🛠 준비 단계 (1회/주기)
1. **키 배포**  
   - 서버: 서명용 개인키 보관  
   - 차량 단말: 공개키(JWKS) 사전 배포 (정기 키 롤링)
2. **앱 로그인/기기 바인딩**  
   - 앱은 JWT 인증 상태  
   - 기기 ID가 서버에 등록됨

---

## 📲 평상시 (온라인 시 토큰 미리 발급)
1. **오프라인 토큰 발급 요청**  
   - 앱 → 서버: `/api/offline-tokens/issue` 호출 (JWT 포함)  
   - 서버: 사용자ID·기기ID 확인 후 **1~5분 유효** 서명 토큰(JWS/CWT) 발급  
   - 토큰 정보:
     - `userId`, `deviceId`
     - `iat` / `nbf` / `exp`
     - `nonce` (랜덤 고유값)
     - `scope` (예: `tap:board`)
2. **앱 저장**  
   - 토큰 안전 저장, 만료 시 재발급

---

## 🚌 승차 (오프라인 검증)
1. **BLE 전송 (앱 → 차량 단말)**  
   - 근거리 BLE로 오프라인 토큰 전송
2. **토큰 검증 (차량 단말)**  
   - 공개키로 서명 검증  
   - `exp`(만료), `nbf`(유효 시작), `deviceId` 일치 여부 확인  
   - **리플레이 방지**: `nonce`를 짧은 TTL 캐시로 중복 차단
3. **임시 승인 / 거절**  
   - 유효 → “승차 OK” 표시, `TapEvent(BOARD)` 로컬 저장  
   - 무효 → 거절, 사유 TTS/진동 안내

---

## 🚏 하차 (오프라인 검증)
- 승차와 동일한 절차  
- `TapEvent(ALIGHT)` 로컬 저장

---

## 🔄 온라인 복구 후 (사후 동기화)
1. **버퍼 업로드 (차량 단말 → 서버)**  
   - `/api/taps/offline/sync` 호출  
   - 로컬 `TapEvent`와 **토큰 해시/식별값** 전송
2. **서버 최종 확정**  
   - 승차/하차 짝 매칭 → `Trip` 생성/갱신  
   - 운임 엔진 → 요금 계산 (farePreview → fareFinal)  
   - PG 청구·정산, `Settlement` 업데이트

---

## ⚠️ 실패/예외 처리
- **만료/서명 불일치**: 즉시 거절 → 앱에 “토큰 재발급 필요” 안내
- **리플레이 감지**: 같은 `nonce` 재사용 시 거절
- **단말 시계 오차**: ±5~10초 허용, NTP 동기화 필요
- **동기화 실패**: 재시도(백오프), 보관 기간 초과 시 규정에 따라 폐기

---

## 🔐 보안 포인트
- **짧은 TTL**(1~5분)
- **서명**: ECDSA ES256 권장
- **기기 바인딩**: `deviceId` 일치 필수
- **이중 방어**:
  - 단말: `nonce` 캐시
  - 서버: 토큰 해시 화이트/블랙리스트
- **키 롤링**: `kid` 버전 관리, 다중 공개키 보관

---

## 📡 서버 API 엔드포인트
- `POST /api/offline-tokens/issue` : 오프라인 토큰 발급 (JWT 인증 필요)
- `POST /api/taps/offline/sync` : 단말의 로컬 TapEvent 동기화

---

## 💾 저장 데이터
- **TapEvent**: `userId`, `deviceId`, `routeId`, `stopId`, `tapType`, `ts`, `authResult`
- **Trip**: 시작/종료 TapEvent 매칭, 요금, 상태(IN_PROGRESS/CONFIRMED)
- **Settlement**: 일 배치 정산 결과(매출/환불 등)
---

## 2) 아키텍처 요약
- **프런트**: React(PWA, 모바일 우선) + 관리자 콘솔(정산/요금/로그)
- **백엔드**: Spring Boot (Java 21) + JPA(MySQL) + Swagger(OpenAPI)
- **인증/보안**: OAuth2/JWT, 기기 바인딩(디바이스 토큰), TLS, 서명된 오프라인 토큰(옵션)
- **단말 연동**: 버스/지하철 단말 스캔(REST/gRPC/SDK), 혹은 지팡이 핸들에 NFC/QR 모듈

--

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
- **Device**(지팡이/폰)
- **PaymentMethod**
- **TapEvent**
- **Trip**
- **FareRule**
- **Stop/Route/RouteStop**
- **Settlement**
- **ArrivalCache**(옵션)

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

### 5.6 관리자
- `GET/POST/PUT/DELETE /api/admin/fare-rules`
- `GET/POST/PUT/DELETE /api/admin/routes`
- `GET/POST/PUT/DELETE /api/admin/stops`

---

## 6) 보안/규제 가이드
- 카드정보 저장 금지(토큰화/PG 보관)
- HTTPS/TLS 전송
- JWT 만료 단축 + 리프레시 토큰 화이트리스트
- 로그 마스킹, 접근로그 분리 보관

---


## 7) 버스 정보 프로바이더 전략
- **TAGO(국토부)**: 전국 정류장/노선 데이터 적재
- **서울시/지자체**: 도착/접근성 정보
- **ODsay(상용)**: 전국 통합 도착/길찾기
