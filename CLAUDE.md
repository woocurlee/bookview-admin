# CLAUDE.md

이 파일은 Claude Code (claude.ai/code)가 이 저장소에서 작업할 때 참고하는 가이드입니다.

## 빌드 및 개발 명령어

```bash
./gradlew build          # 빌드 + 테스트 + ktlint 검사
./gradlew bootRun        # 개발 서버 실행 (포트 8090, 아래 환경변수 필요)
./gradlew test           # 전체 테스트 실행
./gradlew ktlintCheck    # 린트 검사만 실행
./gradlew ktlintFormat   # 린트 자동 수정
```

**필수 환경변수**: `MONGO_URI`, `BOOKVIEW_BASE_URL`

**사전 요구사항**: Java 21, MongoDB `localhost:27017` 실행 중 (데이터베이스: `bookview`, bookview 본 서비스와 공유)

**포트**: 8090 (bookview 본 서비스는 8088)

## 아키텍처

Spring Boot 4.0 + Kotlin 어드민 앱. Thymeleaf 템플릿 + Tailwind CSS. bookview 본 서비스의 MongoDB를 읽기/쓰기 공유.

### 접근 제어

로그인 없이 **IP 허용 목록**으로만 접근 제어. Tailscale VPN(CGNAT 대역 `100.64.0.0/10`) 및 localhost만 허용, 그 외 전부 403. `AdminSecurityConfig`에서 CIDR 설정.

- `admin.allowed-cidr` 프로퍼티로 허용 IP 대역 지정
- CSRF 비활성화 (내부망 전용)
- 세션 사용 (어드민 편의)

### 도메인

bookview 본 서비스의 MongoDB 컬렉션(`users`, `reviews`, `comments`)을 직접 읽고 씀. 도메인 모델은 본 서비스와 동일한 구조를 유지해야 함.

- **소프트 삭제**: `status: Status` 필드(`ACTIVE`/`DELETED`) 토글로 콘텐츠 활성/비활성 처리
- 도메인 모델 변경 시 bookview 본 서비스와 스키마 일치 여부 반드시 확인

### 컨트롤러 패턴

- `ViewController`: Thymeleaf 페이지 렌더링. 목록 조회는 페이지네이션(`PageRequest`) 사용.
- `AdminApiController` (`/api/**`): 상태 토글 등 REST API. `StatusResponse` 반환.
- **컨트롤러는 Repository를 직접 주입받지 않는다.** 반드시 Service 레이어를 통해 데이터에 접근한다.

### 화면 구성

| 경로 | 템플릿 | 설명 |
|------|--------|------|
| `/` | `dashboard.html` | 통계 카드 4개 + 최근 가입/리뷰 |
| `/users` | `users.html` | 유저 목록 (검색, 페이지네이션, 상태 토글) |
| `/reviews` | `reviews.html` | 리뷰 목록 (검색, 페이지네이션, 상태 토글, 원문 링크) |
| `/comments` | `comments.html` | 댓글 목록 (검색, 페이지네이션, 상태 토글) |

- Thymeleaf 템플릿: `resources/templates/`, 공유 레이아웃은 `layout.html`
- JavaScript: `resources/static/js/admin.js`
- 스타일링: Tailwind CSS CDN

### bookview 본 서비스 연동

- `bookview.base-url` 프로퍼티로 본 서비스 URL 지정 (리뷰 원문 링크에 사용)
- 어드민은 본 서비스 API를 호출하지 않고 MongoDB를 직접 공유

## 작업 규칙

- **컬렉션 스키마 변경 금지**: 도메인 모델(User, Review, Comment 등) 필드 추가/삭제/수정은 반드시 먼저 물어보고 진행한다. bookview 본 서비스와 DB를 공유하기 때문에 스키마 변경은 양쪽 모두에 영향을 준다.
- **컨트롤러에는 비즈니스 로직을 넣지 않는다.** 컨트롤러는 요청을 받아 서비스를 호출하고 결과를 뷰에 전달하는 역할만 한다. 조건 분기·계산·도메인 규칙 등 모든 비즈니스 로직은 Service 레이어에 위치해야 한다.

## 컨벤션

- **커밋 메시지**: Gitmoji + Jira 티켓: `:sparkles: BKVW-8 : 설명`
- **브랜치 네이밍**: `feature/BKVW-{번호}`
- **Ktlint**: v1.4.1, 빌드 시 강제 적용 (무시 불가). 커밋 전 반드시 `./gradlew ktlintFormat` 실행.
- **언어**: 사용자 대면 문자열 및 주석은 한국어, 코드 식별자는 영어.
- **커밋 작성자**: `Co-Authored-By` 줄 포함하지 않음. 작성자는 git config 사용자만.
