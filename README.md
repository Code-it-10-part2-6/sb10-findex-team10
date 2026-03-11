# Findex

가볍고 빠른 외부 API 연동 금융 분석 도구

## 프로젝트 소개
Findex는 금융 지수 데이터를 한눈에 확인할 수 있는 대시보드 서비스입니다.  
Open API를 활용하여 지수 정보와 지수 데이터를 연동하고, 이를 기반으로 조회/관리/분석 기능을 제공합니다.

---

## 기술 스택
- Java 17
- Spring Boot 3.5.11
- Spring Data JPA
- PostgreSQL
- springdoc-openapi
- MapStruct
- Railway

---

## 브랜치 전략
- `main` : 최종 배포 및 안정 버전
- `develop` : 기능 통합 브랜치
- `feature/*` : 기능 개발 브랜치

### 작업 방식
1. `develop` 브랜치에서 최신 코드 pull
2. 개인 `feature/*` 브랜치 생성
3. 기능 개발 후 commit / push
4. GitHub에서 `develop` 대상으로 PR 생성
5. 리뷰 후 merge

### 브랜치 예시
- `feature/index-info`
- `feature/index-data`
- `feature/openapi-sync`
- `feature/auto-sync`
- `feature/dashboard`

---

## 커밋 메시지 규칙
예시:
- `feat: 지수 정보 목록 조회 API 추가`
- `fix: 지수 데이터 수정 오류 해결`
- `docs: README 실행 방법 추가`
- `refactor: 공통 응답 구조 정리`
- `chore: 초기 프로젝트 세팅`

권장 prefix:
- `feat`
- `fix`
- `docs`
- `refactor`
- `test`
- `chore`

---

## 프로젝트 구조
