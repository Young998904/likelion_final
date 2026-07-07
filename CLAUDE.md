# 통합 A/S 접수 및 처리 시스템 (as-management) — 저장소 규칙

> 이 저장소(likelion_final)는 **as-management 프로젝트**를 담습니다. (완료·제출)
> 이 파일은 공통 개발 규칙을, [as-management/CLAUDE.md](as-management/CLAUDE.md)는 A/S 고유 규칙을 정의합니다.

## 다른 프로젝트는 별도 저장소
심화 프로젝트 2·3은 **각자 독립된 GitHub 저장소**로 관리합니다(이 저장소 하위에 두지 않음).
- **스킬**은 `~/.claude/skills/`(사용자 전역)에 있어, 어느 저장소를 열든 6종
  (`code-reviewer`, `spring-data-jpa`, `frontend-design`, `theme-factory`, `doc-coauthoring`, `webapp-testing`)이
  자동 적용됩니다. 저장소별 설치 불필요.
- 새 프로젝트를 시작할 때 아래 **공통 개발 규칙**을 그 저장소의 `CLAUDE.md`로 복사해 재사용하고,
  주제·기획안·기능 명세만 프로젝트에 맞게 채웁니다.

---

# 공통 개발 규칙 (모든 프로젝트 공통 · 새 저장소에 그대로 재사용)

## 기술 스택 (확정 · 변경 금지)
- **Java 17 / Spring Boot 3.x** (Boot 4·Java 25로 올리지 않는다)
- 빌드: **Gradle**
- Spring Data JPA (**MyBatis 미사용**)
- DB: **개발/시연은 H2 파일 모드**. JPA라 DB 독립적 → 추후 MySQL 전환은 application.yml만 교체
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

## 스킬 참고 주의
- `code-reviewer`·`spring-data-jpa` 스킬은 Boot 4/Java 25 기준이므로 **일반 가이드로만 참고**하고,
  최신 전용 문법 제안은 Boot 3.x/Java 17에 맞게 걸러 적용한다.

## 공통 작업 규칙
- 코드 변경 시 각 저장소의 `main` 브랜치에 커밋·push (브랜치 분리하지 않음).
