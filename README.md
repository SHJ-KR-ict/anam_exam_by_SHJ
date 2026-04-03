# Ethereum Balance Checker with JWT Auth

스프링 부트(Spring Boot)와 Web3j를 활용하여 이더리움 세폴리아(Sepolia) 테스트넷의 잔액을 조회하고, JWT(JSON Web Token)를 이용해 안전한 무중단 인증 시스템을 구현한 과제입니다.

---

## 주요 기능 (Key Features)

* **이더리움 잔액 조회**: Web3j 라이브러리를 활용하여 입력한 지갑 주소의 Sepolia ETH 잔액을 실시간으로 조회합니다.
* **JWT 기반 무중단 인증**: 
  * 5분 만료의 `Access Token`과 1시간 만료의 `Refresh Token`을 활용한 더블 토큰 시스템입니다.
  * 엑세스 토큰 만료 시, 프론트엔드에서 자동으로 리프레시 토큰을 감지하여 재발급 요청을 보냅니다.
* **캐싱 시스템 적용**: `@Cacheable`을 활용하여 동일한 지갑 주소의 반복적인 조회 시, 블록체인 노드를 매번 찌르지 않고 메모리(RAM) 캐시에서 빠르게 데이터를 반환합니다.
* **세션스토리지 활용**: 브라우저나 탭을 닫으면 자동으로 토큰이 파기되도록 설계하여 보안성을 높였습니다.

---

## 기술 스택 (Tech Stack)

### Backend
* Java 21
* Spring Boot 3.x
* Spring Security
* Web3j (Ethereum Integration)
* JWT (JSON Web Token)

### Frontend
* HTML5 / CSS3
* JavaScript (ES6+)
* Axios (HTTP 통신)
---

## 설계 설명 (System Design)

### 시스템 아키텍처 & 시퀀스
이 프로젝트는 보안성과 성능을 동시에 고려하여 아래와 같은 흐름으로 설계되었습니다.

1. **인증 흐름 (Double Token Strategy)**
   * 사용자가 로그인하면 서버는 Access Token(5분)과 Refresh Token(1시간)을 발급합니다.
   * 클라이언트는 이를 브라우저의 `SessionStorage`에 안전하게 보관합니다.
   * Access Token이 만료되면 Axios 인터셉터(Interceptor)가 이를 감지하여, 백엔드로 Refresh Token을 보내 조용히 새로운 Access Token을 갱신받습니다.

2. **조회 및 캐싱 흐름 (Performance Optimization)**
   * 클라이언트가 지갑 주소로 잔액 조회를 요청합니다.
   * **[Cache-Hit]** 만약 10초 이내에 동일한 주소로 요청한 적이 있다면, 블록체인 노드를 찌르지 않고 메모리(RAM) 캐시에서 즉시 반환합니다.
   * **[Cache-Miss]** 캐시에 데이터가 없거나 10초가 지났다면, Web3j를 통해 세폴리아(Sepolia) RPC 노드와 직접 통신하여 최신 잔액을 가져온 후 캐시에 저장합니다.
   * *이더리움의 블록 생성 주기(약 12~15초)를 고려하여 캐시 만료 시간을 10초로 제한함으로써, 데이터 정합성을 유지하고 RPC 트래픽을 최적화했습니다.*

---

## 실행 방법 (How to Run)

### 1. 환경 변수 설정 가이드
이 프로젝트는 보안이 필요한 민감한 정보(JWT 비밀키, RPC URL 등)를 코드에 직접 노출하지 않고, 시스템 환경 변수를 통해 주입받도록 설계되어 있습니다.

프로젝트를 실행하기 전, IDE(STS/IntelliJ)의 **Run Configuration**이나 운영체제의 환경 변수에 아래 항목들을 등록해 주세요.

* **`ETH_RPC_URL`**: 이더리움 세폴리아 노드 주소
* **`JWT_SECRET_KEY`**: JWT 서명에 사용할 32자 이상의 비밀키(32자 넘지 않으면 서버 다운)

> **Tip:** 별도의 환경 변수를 등록하지 않으면 기본값(빈 값)으로 동작하므로, 정상적인 조회를 위해 꼭 등록해 주셔야 합니다.

### 2. Spring Cache 파라미터(#address) 인식 설정
컨트롤러나 서비스에서 `@Cacheable(value = "balanceCache", key = "#address")`처럼 파라미터 이름을 SpEL로 인식하게 하려면, Java 컴파일러가 매개변수 이름을 유지하도록 설정해야 합니다. 

설정이 되어있지 않으면 `address`라는 파라미터 이름을 찾지 못해 캐싱 시 에러가 발생할 수 있습니다.

#### STS (Eclipse) 설정 방법
1. 상단 메뉴의 **Window** ➡️ **Preferences**로 이동합니다.
2. 왼쪽 메뉴에서 **Java** ➡️ **Compiler**를 클릭합니다.
3. **Classfile Generation** 항목에 있는 **`Store information about method parameters (usable via reflection)`**에 체크를 해줍니다.
4. **Apply and Close**를 누른 뒤 프로젝트를 **Clean** 후 다시 빌드합니다.

### 3. 로컬호스트(Localhost) 접속 및 테스트
모든 환경 변수 설정과 컴파일러 세팅이 완료되었다면 프로젝트를 실행합니다. 서버가 정상적으로 구동되면 브라우저를 열고 아래 주소로 접속하여 테스트를 진행할 수 있습니다.

* **접속 주소:** `http://localhost:81`
* **테스트 방법:**
1. 화면에서 로그인을 진행하여 토큰을 발급받습니다.
2. 유효한 세폴리아 이더리움 지갑 주소를 입력하고 잔액 조회를 누릅니다.
3. 개발자 도구(F12)의 Application 탭 내 세션 스토리지에서 발급된 두 토큰이 정상적으로 저장되었는지 확인합니다.
4. 10초 이내에 연속으로 잔액 조회를 요청하여, 실제 RPC 통신 발생 시에만 콘솔에 `'RPC 노드 사용함'` 로그가 출력되는지 확인합니다.
5. 5분 후 재조회 시 콘솔에 `Access Token 5분 만료`가 출력되며, Refresh 토큰을 통해 끊김 없이 조회가 성공하는지 확인합니다.

#### 주요 설정값 안내 (`application.properties` 기준)
* `server.port=81`: 포트 번호 81번 사용
* `jwt.expiration=300000`: Access Token 만료 시간 (5분)
* `jwt.refreshexpiration=3600000`: Refresh Token 만료 시간 (1시간)
* `spring.cache.caffeine.spec`: 최대 1000개의 캐시 데이터를 10초 동안 보관

