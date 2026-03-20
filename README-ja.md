# 中国象棋

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

JavaFX で開発したクロスプラットフォーム的中国象棋（ Xiangqi ）デスクトップアプリケーションです。プレイヤー対プレイヤーとプレイヤー対AIのモードをサポートしています。

## 機能

- **複数のゲームモード**
  - プレイヤー対プレイヤー（PvP）
  - プレイヤー対AI（PvE）
  - 難易度レベル（入門、簡単、中級、上級、マスター）

- **完全な中国象棋ルール**
  - 全駒の合法移動ルール
  - 將・帥の対面禁止
  - 王手・詰み判定

- **AIエンジン**
  - 内蔵 Minimax アルゴリズム AI
  - UCI プロトコル外部エンジンサポート
  - 複数の難易度レベル

- **ゲーム管理**
  - 悔み機能
  - ゲーム再開
  - 棋譜保存

## 技術スタック

- **Java** 21
- **JavaFX** 21.0.2
- **Maven** ビルド
- **SLF4J** ログフレームワーク

## 動作環境

- JDK 21 以上
- macOS / Windows / Linux 対応

## クイックスタート

### プロジェクトをビルド

```bash
# クローン
git clone https://github.com/CMMUU/chinese-chess.git
cd chinese-chess

# ビルド
mvn clean package
```

### アプリケーションを実行

```bash
# 直接実行
mvn javafx:run

# または JAR を実行
java -jar target/chinese-chess-1.0.0.jar
```

### インストーラーを生成

```bash
# macOS DMG
jpackage --input target --main-jar chinese-chess-1.0.0.jar --name ChineseChess --type dmg --dest target/jpackage

# Windows EXE
jpackage --input target --main-jar chinese-chess-1.0.0.jar --name ChineseChess --type exe --dest target/jpackage

# Linux DEB
jpackage --input target --main-jar chinese-chess-1.0.0.jar --name ChineseChess --type deb --dest target/jpackage
```

## プロジェクト構造

```
src/
├── main/
│   ├── java/cn/chinesechess/
│   │   ├── core/           # コアエンジン
│   │   │   ├── Board.java           # 盤面
│   │   │   ├── Piece.java           # 駒
│   │   │   ├── Move.java            # 着手
│   │   │   ├── GameState.java       # ゲーム状態
│   │   │   ├── GameEngine.java      # ゲームエンジン
│   │   │   └── rules/               # 移動ルール
│   │   │       ├── RookMoveRule.java     # 車
│   │   │       ├── KnightMoveRule.java   # 馬
│   │   │       ├── CannonMoveRule.java   # 炮
│   │   │       ├── PawnMoveRule.java     # 兵
│   │   │       ├── KingMoveRule.java     # 將
│   │   │       ├── AdvisorMoveRule.java  # 士
│   │   │       └── ElephantMoveRule.java # 象
│   │   ├── ai/               # AI エンジン
│   │   │   ├── MinimaxEngine.java      # Minimax アルゴリズム
│   │   │   ├── ChessEngine.java        # エンジンインターフェース
│   │   │   └── EngineManager.java      # エンジン管理
│   │   ├── ui/               # UI 層
│   │   │   ├── ChineseChessApp.java    # メインアプリ
│   │   │   ├── BoardView.java          # 盤面ビュー
│   │   │   └── GameController.java     # ゲームコントローラー
│   │   └── data/             # データ層
│   │       ├── AppSettings.java       # アプリ設定
│   │       └── GameRecordManager.java  # 棋譜管理
│   └── resources/
│       └── css/style.css      # スタイル
└── test/                      # ユニットテスト
```

## ドキュメント

プロジェクトドキュメントは `docs/` フォルダにあります：

- [コアコード解説と JavaFX 活用](docs/core-code-guide-ja.md)
- [Core Code Guide & JavaFX Usage](docs/core-code-guide.md)
- [핵심 코드 설명 및 JavaFX 활용](docs/core-code-guide-ko.md)

## コントリビューション

Issue と Pull Request をお待ちしています！

## ライセンス

MIT License
