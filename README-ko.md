# 중국 장기

<p align="center">
  <img src="https://img.shields.io/badge/Java-21-blue" alt="Java">
  <img src="https://img.shields.io/badge/JavaFX-21.0.2-blue" alt="JavaFX">
  <img src="https://img.shields.io/badge/License-MIT-green" alt="License">
  <img src="https://github.com/CMMUU/chinese-chess/actions/workflows/build.yml/badge.svg" alt="Build">
</p>

<p align="center">
  <a href="README.md">中文</a> |
  <a href="README-en.md">English</a> |
  <a href="README-ja.md">日本語</a> |
  <a href="README-ko.md">한국어</a>
</p>

JavaFX로 개발한 크로스플랫폼 중국 장기(Xiangqi) 데스크톱 애플리케이션입니다. 플레이어 대 플레이어 및 플레이어 대 AI 모드를 지원합니다.

## 기능

- **다양한 게임 모드**
  - 플레이어 대 플레이어 (PvP)
  - 플레이어 대 AI (PvE)
  - 난이도 선택 (입문, 초급, 중급, 고급, 마스터)

- **완전한 중국 장기 규칙**
  - 모든 기물의 합법적 이동 규칙
  - 장수(將帥) 마주보는 것 금지
  - 체크 및 체크메이트 판정

- **AI 엔진**
  - 내장 Minimax 알고리즘 AI
  - UCI 프로토콜 외부 엔진 지원
  - 다중 난이도级别

- **게임 관리**
  - 무르기 기능
  - 게임 재시작
  - 기보 저장

## 기술 스택

- **Java** 21
- **JavaFX** 21.0.2
- **Maven** 빌드
- **SLF4J** 로깅 프레임워크

## 요구 사항

- JDK 21 이상
- macOS / Windows / Linux 지원

## 빠른 시작

### 프로젝트 빌드

```bash
# 클론
git clone https://github.com/CMMUU/chinese-chess.git
cd chinese-chess

# 빌드
mvn clean package
```

### 애플리케이션 실행

```bash
# 직접 실행
mvn javafx:run

# 또는 JAR 실행
java -jar target/chinese-chess-1.0.0.jar
```

### 인스톨러 생성

```bash
# macOS DMG
jpackage --input target --main-jar chinese-chess-1.0.0.jar --name ChineseChess --type dmg --dest target/jpackage

# Windows EXE
jpackage --input target --main-jar chinese-chess-1.0.0.jar --name ChineseChess --type exe --dest target/jpackage

# Linux DEB
jpackage --input target --main-jar chinese-chess-1.0.0.jar --name ChineseChess --type deb --dest target/jpackage
```

## 프로젝트 구조

```
src/
├── main/
│   ├── java/cn/chinesechess/
│   │   ├── core/           # 코어 엔진
│   │   │   ├── Board.java           # 판업
│   │   │   ├── Piece.java           # 기물
│   │   │   ├── Move.java            # 수순
│   │   │   ├── GameState.java       # 게임 상태
│   │   │   ├── GameEngine.java      # 게임 엔진
│   │   │   └── rules/               # 이동 규칙
│   │   │       ├── RookMoveRule.java     # 차
│   │   │       ├── KnightMoveRule.java   # 마
│   │   │       ├── CannonMoveRule.java   # 포
│   │   │       ├── PawnMoveRule.java     # 병
│   │   │       ├── KingMoveRule.java     # 장
│   │   │       ├── AdvisorMoveRule.java  # 사
│   │   │       └── ElephantMoveRule.java # 상
│   │   ├── ai/               # AI 엔진
│   │   │   ├── MinimaxEngine.java      # Minimax 알고리즘
│   │   │   ├── ChessEngine.java        # 엔진 인터페이스
│   │   │   └── EngineManager.java      # 엔진 관리
│   │   ├── ui/               # UI 계층
│   │   │   ├── ChineseChessApp.java    # 메인 앱
│   │   │   ├── BoardView.java          # 판독 뷰
│   │   │   └── GameController.java     # 게임 컨트롤러
│   │   └── data/             # 데이터 계층
│   │       ├── AppSettings.java       # 앱 설정
│   │       └── GameRecordManager.java  # 기보 관리
│   └── resources/
│       └── css/style.css      # 스타일
└── test/                      # 단위 테스트
```

## 문서

프로젝트 문서는 `docs/` 폴더에 있습니다:

- [핵심 코드 설명 및 JavaFX 활용](docs/core-code-guide-ko.md)
- [Core Code Guide & JavaFX Usage](docs/core-code-guide.md)
- [コアコード解説と JavaFX 活用](docs/core-code-guide-ja.md)

## 기여

Issue와 Pull Request를 기다립니다!

## 라이선스

MIT License
