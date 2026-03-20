# 中国象棋 (Chinese Chess)

<p align="center">
  <img src="https://img.shields.io/badge/Java-21-blue" alt="Java">
  <img src="https://img.shields.io/badge/JavaFX-21.0.2-blue" alt="JavaFX">
  <img src="https://img.shields.io/badge/License-MIT-green" alt="License">
  <img src="https://github.com/CMMUU/chinese-chess/actions/workflows/build.yml/badge.svg" alt="Build">
</p>

一个跨平台的中国象棋桌面应用，采用 JavaFX 开发，支持人人对战、人机对战等多种模式。

## 功能特性

- **多种游戏模式**
  - 人人对战（双人对弈）
  - 人机对战（AI 智能对手）
  - 难度级别选择（入门、初级、中级、高级、大师）

- **完整的象棋规则**
  - 全部棋子的合法移动规则
  - 将帅不能对面检测
  - 将军/将死判定

- **AI 引擎**
  - 内置 Minimax 算法 AI
  - 支持 UCI 协议外部引擎
  - 多级难度选择

- **棋局管理**
  - 悔棋功能
  - 重新开始
  - 棋局记录保存

## 技术栈

- **Java** 21
- **JavaFX** 21.0.2
- **Maven** 构建
- **SLF4J** 日志框架

## 环境要求

- JDK 21 或更高版本
- 支持 macOS / Windows / Linux

## 快速开始

### 编译项目

```bash
# 克隆项目
git clone https://github.com/CMMUU/chinese-chess.git
cd chinese-chess

# 编译
mvn clean package
```

### 运行应用

```bash
# 直接运行
mvn javafx:run

# 或运行打包后的 JAR
java -jar target/chinese-chess-1.0.0.jar
```

### 生成安装包

```bash
# macOS DMG
jpackage --input target --main-jar chinese-chess-1.0.0.jar --name ChineseChess --type dmg --dest target/jpackage

# Windows EXE
jpackage --input target --main-jar chinese-chess-1.0.0.jar --name ChineseChess --type exe --dest target/jpackage

# Linux DEB
jpackage --input target --main-jar chinese-chess-1.0.0.jar --name ChineseChess --type deb --dest target/jpackage
```

## 项目结构

```
src/
├── main/
│   ├── java/cn/chinesechess/
│   │   ├── core/           # 核心引擎
│   │   │   ├── Board.java           # 棋盘
│   │   │   ├── Piece.java           # 棋子
│   │   │   ├── Move.java            # 走法
│   │   │   ├── GameState.java       # 游戏状态
│   │   │   ├── GameEngine.java      # 游戏引擎
│   │   │   └── rules/               # 走法规则
│   │   │       ├── RookMoveRule.java     # 车
│   │   │       ├── KnightMoveRule.java   # 马
│   │   │       ├── CannonMoveRule.java   # 炮
│   │   │       ├── PawnMoveRule.java     # 兵
│   │   │       ├── KingMoveRule.java     # 将
│   │   │       ├── AdvisorMoveRule.java  # 士
│   │   │       └── ElephantMoveRule.java # 象
│   │   ├── ai/               # AI 引擎
│   │   │   ├── MinimaxEngine.java      # Minimax 算法
│   │   │   ├── ChessEngine.java        # 引擎接口
│   │   │   └── EngineManager.java      # 引擎管理
│   │   ├── ui/               # 界面层
│   │   │   ├── ChineseChessApp.java    # 主应用
│   │   │   ├── BoardView.java          # 棋盘视图
│   │   │   └── GameController.java     # 游戏控制器
│   │   └── data/             # 数据层
│   │       ├── AppSettings.java       # 应用设置
│   │       └── GameRecordManager.java  # 棋局记录
│   └── resources/
│       └── css/style.css      # 样式文件
└── test/                      # 单元测试
```

## 版本发布

使用 Git Tag 进行版本发布：

```bash
# 创建版本标签
git tag v1.0.0

# 推送到远程触发构建
git push origin v1.0.0
```

推送后 GitHub Actions 会自动：
1. 在 macOS、Windows、Linux 三个平台构建
2. 生成对应的安装包（.dmg、.exe、.deb）
3. 自动创建草稿 Release

## 下载安装

从 [Releases](https://github.com/CMMUU/chinese-chess/releases) 页面下载对应平台的安装包：

- **macOS**: `.dmg` 安装包
- **Windows**: `.exe` 安装包
- **Linux**: `.deb` 安装包

## 参与贡献

欢迎提交 Issue 和 Pull Request！

## 许可证

MIT License
