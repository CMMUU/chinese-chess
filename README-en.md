# Chinese Chess

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

A cross-platform Chinese Chess (Xiangqi) desktop application built with JavaFX, supporting player-vs-player and player-vs-AI modes.

## Features

- **Multiple Game Modes**
  - Player vs Player (PvP)
  - Player vs AI (PvE)
  - Difficulty levels (Beginner, Easy, Medium, Hard, Master)

- **Complete Chinese Chess Rules**
  - All piece movement rules
  - King and General cannot face each other
  - Check and Checkmate detection

- **AI Engine**
  - Built-in Minimax algorithm AI
  - Support for UCI protocol external engines
  - Multiple difficulty levels

- **Game Management**
  - Undo function
  - Restart game
  - Save game records

## Tech Stack

- **Java** 21
- **JavaFX** 21.0.2
- **Maven** Build
- **SLF4J** Logging

## Requirements

- JDK 21 or higher
- Supports macOS / Windows / Linux

## Quick Start

### Build Project

```bash
# Clone project
git clone https://github.com/CMMUU/chinese-chess.git
cd chinese-chess

# Build
mvn clean package
```

### Run Application

```bash
# Run directly
mvn javafx:run

# Or run packaged JAR
java -jar target/chinese-chess-1.0.0.jar
```

### Generate Installer

```bash
# macOS DMG
jpackage --input target --main-jar chinese-chess-1.0.0.jar --name ChineseChess --type dmg --dest target/jpackage

# Windows EXE
jpackage --input target --main-jar chinese-chess-1.0.0.jar --name ChineseChess --type exe --dest target/jpackage

# Linux DEB
jpackage --input target --main-jar chinese-chess-1.0.0.jar --name ChineseChess --type deb --dest target/jpackage
```

## Project Structure

```
src/
├── main/
│   ├── java/cn/chinesechess/
│   │   ├── core/           # Core Engine
│   │   │   ├── Board.java           # Board
│   │   │   ├── Piece.java           # Piece
│   │   │   ├── Move.java            # Move
│   │   │   ├── GameState.java       # Game State
│   │   │   ├── GameEngine.java      # Game Engine
│   │   │   └── rules/               # Move Rules
│   │   │       ├── RookMoveRule.java     # Chariot
│   │   │       ├── KnightMoveRule.java   # Horse
│   │   │       ├── CannonMoveRule.java   # Cannon
│   │   │       ├── PawnMoveRule.java     # Soldier
│   │   │       ├── KingMoveRule.java     # General
│   │   │       ├── AdvisorMoveRule.java  # Advisor
│   │   │       └── ElephantMoveRule.java # Elephant
│   │   ├── ai/               # AI Engine
│   │   │   ├── MinimaxEngine.java      # Minimax Algorithm
│   │   │   ├── ChessEngine.java        # Engine Interface
│   │   │   └── EngineManager.java      # Engine Manager
│   │   ├── ui/               # UI Layer
│   │   │   ├── ChineseChessApp.java    # Main Application
│   │   │   ├── BoardView.java          # Board View
│   │   │   └── GameController.java     # Game Controller
│   │   └── data/             # Data Layer
│   │       ├── AppSettings.java       # App Settings
│   │       └── GameRecordManager.java  # Game Record
│   └── resources/
│       └── css/style.css      # Styles
└── test/                      # Unit Tests
```

## Documentation

Project documentation is in the `docs/` folder:

- [Core Code Guide & JavaFX Usage](docs/core-code-guide.md)
- [コアコード解説と JavaFX 活用](docs/core-code-guide-ja.md)
- [핵심 코드 설명 및 JavaFX 활용](docs/core-code-guide-ko.md)

## Contributing

Feel free to submit Issues and Pull Requests!

## License

MIT License
