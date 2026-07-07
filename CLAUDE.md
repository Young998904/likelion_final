# BEJV21 — 백엔드 자바 심화 프로젝트 워크스페이스

> 이 저장소(BEJV21)는 **여러 개의 독립 프로젝트**를 담는 워크스페이스입니다.
> **기본 개발 환경·규칙은 모든 프로젝트가 공통**이며 이 파일에서 정의합니다.
> 각 프로젝트 폴더의 `CLAUDE.md`에는 **그 프로젝트 고유 내용(주제·기획안·기능)만** 둡니다.
> 프로젝트 작업 시 이 파일 + 해당 폴더 `CLAUDE.md`가 함께 로드됩니다.

## 폴더 구조
```
BEJV21/                     ← git 저장소 루트(워크스페이스)
├── CLAUDE.md               ← (이 파일) 공통 개발 규칙 + 워크스페이스 안내
├── .claude/skills/         ← 공통 스킬 6종 (아래 "스킬" 참고)
├── as-management/          ← 프로젝트 1: 통합 A/S 접수·처리 시스템 (완료)
├── project2/               ← 프로젝트 2 (예정)
├── project3/               ← 프로젝트 3 (예정)
├── (참여기업)...안내.pdf     ← 3개 프로젝트 공통 과제 안내 (gitignore)
└── 제출 결과물.png           ← 참고 자료 (gitignore)
```

---

# 공통 개발 규칙 (모든 프로젝트 필수 준수)

## 기술 스택 (확정 · 변경 금지)
- **Java 17 / Spring Boot 3.x** (Boot 4·Java 25로 올리지 않는다)
- 빌드: **Gradle**
- Spring Data JPA (**MyBatis 미사용**)
- DB: **개발/시연은 H2 파일 모드**(로컬 MariaDB 손상으로 대체). JPA라 DB 독립적 → 추후 MySQL 전환은 application.yml만 교체
- 프론트: Thymeleaf + 일부 jQuery/Ajax
- Spring Security (세션 기반 인증, 역할 기반 접근제어)

## 코딩 컨벤션 (필수)

### 주석 — 무조건 한글
- **모든 주석은 한글로 작성한다** (인라인 주석, 블록 주석, Javadoc 전부).
- 주석은 **'무엇'이 아니라 '왜'**를 설명한다. 코드로 자명한 내용을 중복 서술하지 않는다.
- 공개 Service / Controller 메서드에는 한글 Javadoc을 단다.

### UI 표기 — 기본 한글
- 화면에 보이는 모든 텍스트(라벨·버튼·메뉴·안내/오류 메시지·컬럼명)는 **기본 한글**로 작성한다.
- 단, **국내에서도 영어로 통용되는 관용어는 그대로 사용**한다. 억지 번역 금지.
  - 허용 예: `로그인`, `로그아웃`, `아이디`, `비밀번호`, `이메일`, `A/S`, `대시보드`
- 자연스러운 한국어 UI 관례를 우선한다.

### 코드 품질
- 계층 분리: `controller` / `service` / `repository` / `dto`. 엔티티를 화면·API에 직접 노출하지 않고 DTO를 사용한다.
- JPA: N+1 회피(fetch join·`@EntityGraph`), 트랜잭션 경계는 `service` 계층에 둔다.
- 비밀번호는 BCrypt로 암호화 저장한다.

### 식별자·코드 명명
- 클래스/변수/메서드명 등 **코드 식별자는 영어**(일반 관례)로 두고, 주석과 UI만 위 규칙을 따른다.

## 스킬 (모든 프로젝트 공통)
`.claude/skills/`에 6종 설치: `code-reviewer`, `spring-data-jpa`, `frontend-design`,
`theme-factory`, `doc-coauthoring`, `webapp-testing`.
- **BEJV21 루트에서 Claude Code를 실행하면** 3개 프로젝트 어디서든 이 스킬들이 잡힙니다
  (project2·3는 하위 폴더이므로 루트 기준 세션이면 자동 포함).
- `code-reviewer`·`spring-data-jpa`는 Boot 4/Java 25 기준이므로 **일반 가이드로만 참고**하고,
  최신 전용 문법 제안은 Boot 3.x/Java 17에 맞게 걸러 적용한다.

## 공통 작업 규칙
- 코드 변경 시 `main` 브랜치에 커밋·push (브랜치 분리하지 않음).

---

# 프로젝트별 안내
- **as-management** — 통합 A/S 접수·처리 시스템. 상세는 [as-management/CLAUDE.md](as-management/CLAUDE.md).
- **project2 / project3** — 새 프로젝트 시작 시 각 폴더에 전용 `CLAUDE.md`를 만들어
  **주제·기획안·기능 명세만** 정의합니다(기술 스택·컨벤션은 위 공통 규칙을 그대로 따름).
