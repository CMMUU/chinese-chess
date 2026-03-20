# コアコード解説と JavaFX 活用

このドキュメントでは、中国象棋プロジェクトのコアコードアーキテクチャと、本プロジェクトにおける JavaFX の具体的な活用方法について詳しく説明します。

## 目次

- [アーキテクチャ概要](#アーキテクチャ概要)
- [コアエンジン層](#コアエンジン層)
- [AI エンジン層](#ai-エンジン層)
- [UI 層と JavaFX 活用](#ui-層と-javafx-活用)
- [データ層](#データ層)

---

## アーキテクチャ概要

プロジェクトは古典的な **MVC + オブザーバーパターン** アーキテクチャを採用しています：

```
┌─────────────────────────────────────────────────────────────┐
│                         UI 層                                │
│  ChineseChessApp → BoardView → GameController → AIIntegration│
└──────────────────────────┬──────────────────────────────────┘
                           │ オブザーバーパターン
┌──────────────────────────▼──────────────────────────────────┐
│                      コアエンジン層                          │
│  GameEngine → MoveGenerator → CheckDetector → 各棋子ルール │
└──────────────────────────┬──────────────────────────────────┘
                           │
┌──────────────────────────▼──────────────────────────────────┐
│                        データ層                              │
│              AppSettings → GameRecordManager                │
└─────────────────────────────────────────────────────────────┘
```

---

## コアエンジン層

### 1. ゲームエンジン (GameEngine)

**ファイル位置**: `src/main/java/cn/chinesechess/core/engine/GameEngine.java`

ゲームエンジンはアプリケーションの中核であり、ゲーム状態の管理、着手ロジックの処理、勝敗判定を担当します。

**中核的な責務**:
- 着手検証と実行
- ターン管理
- 王手/詰み/ステalemate 検出
- 三千日局面の引き分け検出
- イベント通知（オブザーバーパターン）

**主要コード片段** - 着手実行と状態検出：

```java
@Override
public MoveResult makeMove(Position from, Position to) {
    // 1. 駒を取得し所有者を検証
    Piece piece = board.getPiece(from);
    if (piece == null || piece.getColor() != currentTurn) {
        return MoveResult.INVALID;
    }

    // 2. 目標位置の合法性を検証
    List<Position> legalTargets = getLegalTargets(from);
    if (!legalTargets.contains(to)) {
        return MoveResult.INVALID;
    }

    // 3. 着手を実行（コマンドパターン）
    MoveCommand command = new MoveCommand(move, piece);
    command.execute(board);
    moveHistory.add(command);

    // 4. ゲーム状態を検出
    if (checkDetector.isCheckmate(board, nextTurn)) {
        result = MoveResult.CHECKMATE;
        gameResult = (nextTurn == Color.RED) ? GameResult.BLACK_WIN : GameResult.RED_WIN;
    } else if (checkDetector.isInCheck(board, nextTurn)) {
        result = MoveResult.CHECK;
    }
    // ...

    // 5. UI 層に通知
    notifyMoveMade(move, result);
    notifyStateChanged();
    return result;
}
```

### 2. 着手生成器 (MoveGenerator)

**ファイル位置**: `src/main/java/cn/chinesechess/core/rules/MoveGenerator.java`

すべての駒の合法手を生成し、中国象棋ルールのコア実装を担当します。

**デザインパターン - ストラテジーパターン**:
- 各駒タイプに対応する `IMoveRule` 実装クラス
- `MoveRuleFactory` ファクトリーが対応するルールハンドラを作成

```java
// 着手生成コアロジック
public List<Move> getLegalMoves(IBoard board, Color color) {
    List<Move> legalMoves = new ArrayList<>();

    // 現プレイヤーの全駒を走査
    for (Piece piece : board.getPiecesByColor(color)) {
        // 该駒タイプのルールハンドラを取得
        IMoveRule rule = MoveRuleFactory.getRule(piece.getType());
        // 该駒の全合法手を生成
        List<Position> targets = rule.getLegalTargets(board, piece);
        for (Position target : targets) {
            // 着手を模擬し、自軍が王手になるか検証
            if (!wouldCauseSelfCheck(board, piece, target)) {
                legalMoves.add(new Move(piece.getPosition(), target, captured));
            }
        }
    }
    return legalMoves;
}
```

### 3. 駒ルールクラス

各駒のルール実装は `cn.chinesechess.core.rules` パッケージにあります：

| クラス | 対応棋子 | ルール特徴 |
|--------|----------|------------|
| `RookMoveRule` | 車 | 直線移動、障害物なし |
| `KnightMoveRule` | 馬 | 日字形、「馬足」検出 |
| `CannonMoveRule` | 炮 | 直線移動、食べるには飛び越し必要 |
| `PawnMoveRule` | 兵/卒 | 川前後で異なる動き |
| `KingMoveRule` | 将/帅 | 宮内で移動 |
| `AdvisorMoveRule` | 士/仕 | 斜め、宮内 |
| `ElephantMoveRule` | 象/相 | 田字形、「塞象眼」検出 |

---

## AI エンジン層

### 1. AI インターフェース設計

**ファイル位置**: `src/main/java/cn/chinesechess/ai/IAIEngine.java`

AI エンジンの統一インターフェースを定義し、複数のエンジン実装をサポート：

```java
public interface IAIEngine {
    /** 最善手を検索 */
    Move searchBestMove(IBoard board, Color color, int depth);

    /** 探索深さを設定（難易度） */
    void setDepth(int depth);

    /** 探索を停止 */
    void stop();
}
```

### 2. 組み込み Minimax エンジン

**ファイル位置**: `src/main/java/cn/chinesechess/ai/MinimaxEngine.java`

Alpha-Beta 枝刈り付き Minimax アルゴリズムを実装：

```java
@Override
public Move searchBestMove(IBoard board, Color color, int depth) {
    // 評価関数 + Minimax 再帰探索
    Move bestMove = null;
    int bestScore = color == Color.RED ? Integer.MIN_VALUE : Integer.MAX_VALUE;

    for (Move move : moveGenerator.getLegalMoves(board, color)) {
        // 1. 着手を実行
        board.makeMove(move);

        // 2. 再帰探索
        int score = minimax(board, depth - 1, alpha, beta,
                color.opposite(), false);

        // 3. 着手を戻す
        board.undoMove();

        // 4. 最善手を更新
        if (color == Color.RED ? score > bestScore : score < bestScore) {
            bestScore = score;
            bestMove = move;
        }

        // 5. Alpha-Beta 枝刈り
        if (color == Color.RED) {
            alpha = Math.max(alpha, bestScore);
        } else {
            beta = Math.min(beta, bestScore);
        }
        if (beta <= alpha) break; // 枝刈り
    }
    return bestMove;
}
```

### 3. 局面評価 (BoardEvaluator)

**ファイル位置**: `src/main/java/cn/chinesechess/ai/BoardEvaluator.java`

静的局面評価関数，考虑：
- 駒の価値点（車>馬>炮>兵...）
- 駒の位置点（各駒の異なる位置での点数）
- 柔軟性点（可能な着手数）
- 脅威と保護の関係

---

## UI 層と JavaFX 活用

### 1. アプリケーションエントリ (ChineseChessApp)

**ファイル位置**: `src/main/java/cn/chinesechess/ui/ChineseChessApp.java`

`javafx.application.Application` を継承し、JavaFX アプリケーションのエントリポイント：

```java
public class ChineseChessApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        // 1. ゲームエンジンとビューを作成
        IGameEngine gameEngine = new GameEngine();
        BoardView boardView = new BoardView();
        GameController controller = new GameController(gameEngine, boardView, engineManager);

        // 2. レイアウトを作成 (BorderPane: 上-中-下構造)
        BorderPane root = new BorderPane();
        root.setCenter(boardView);
        root.setTop(menuBar);
        root.setBottom(statusBar);

        // 3. シーンを作成し CSS をロード
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

        // 4. キーボードショートカットを登録
        scene.getAccelerators().put(
            new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN),
            () -> showNewGameDialog(controller, owner)
        );

        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
```

**JavaFX 機能の活用**:
- `Application` - JavaFX アプリケーションベースクラス
- `Stage` / `Scene` - ウィンドウとシーン
- `BorderPane` - ボーダーレイアウト
- `MenuBar` - メニューBAR
- `ToolBar` - ツールBAR
- `Accelerator` - キーボードショートカット

### 2. 盤面ビュー (BoardView)

**ファイル位置**: `src/main/java/cn/chinesechess/ui/BoardView.java`

`Canvas` を使用して盤面を描画し、`StackPane` を継承：

```java
public class BoardView extends StackPane {

    private final Canvas canvas;
    private IBoard board;

    public BoardView() {
        canvas = new Canvas(BOARD_WIDTH, BOARD_HEIGHT);
        getChildren().add(canvas);
    }

    // 盤面のグリッドを描画
    private void drawBoard(GraphicsContext gc) {
        // 9本の横線を描画
        for (int row = 0; row <= 9; row++) {
            gc.strokeLine(x1, y, x2, y);
        }
        // 10本の縦線を描画（川的地方で途切れる）
        // 九宮の対角線を描画
        // 兵/炮の位置マークを描画
    }

    // 棋子を描画
    private void drawPieces(GraphicsContext gc) {
        for (Piece piece : board.getAllPieces()) {
            // 棋子の円の背景を描画
            gc.setFill(Color.rgb(255, 248, 220));
            gc.fillOval(x - radius, y - radius, radius * 2, radius * 2);

            // 枠を描画（赤方は赤、黒方は黒）
            gc.setStroke(piece.getColor() == Color.RED ?
                Color.rgb(180, 0, 0) : Color.rgb(30, 30, 30));

            // 棋子の文字を描画（楷書）
            gc.setFont(Font.font("楷体", FontWeight.BOLD, 26));
            gc.fillText(piece.getDisplayName(), textX, textY);
        }
    }

    // 座標変換
    public Position canvasToPosition(double canvasX, double canvasY) {
        int col = (int) Math.round((canvasX - PADDING) / CELL_SIZE);
        int row = (int) Math.round((canvasY - PADDING) / CELL_SIZE);
        // 反転状態を処理
        if (flipped) {
            col = 8 - col;
            row = 9 - row;
        }
        return new Position(col, row);
    }
}
```

**JavaFX 機能の活用**:
- `Canvas` + `GraphicsContext` - カスタム描画
- マウスイベント処理 - `setOnMouseClicked`
- 座標系変換 - スクリーン座標から盤面座標へ

### 3. ゲームコントローラー (GameController)

**ファイル位置**: `src/main/java/cn/chinesechess/ui/GameController.java`

UI 層とコアエンジンを接続し、ユーザーインタラクションを処理：

```java
public class GameController implements GameEventListener {

    public GameController(IGameEngine gameEngine, BoardView boardView, EngineManager engineManager) {
        // 1. ゲームイベントリスナーを登録
        gameEngine.addEventListener(this);

        // 2. マウスクリックイベントをバインド
        boardView.getCanvas().setOnMouseClicked(this::handleBoardClick);
    }

    // 盤面クリックを処理
    private void handleBoardClick(MouseEvent event) {
        Position clickedPos = boardView.canvasToPosition(event.getX(), event.getY());

        if (selectedFrom == null) {
            // 最初のクリック：棋子選択
            Piece piece = board.getPiece(clickedPos);
            if (piece != null && piece.getColor() == gameEngine.getCurrentTurn()) {
                selectedFrom = clickedPos;
                currentLegalTargets = gameEngine.getLegalTargets(clickedPos);
                boardView.setSelected(clickedPos, currentLegalTargets);
            }
        } else {
            // 2回目のクリック：着手またはキャンセル
            if (currentLegalTargets.contains(clickedPos)) {
                gameEngine.makeMove(selectedFrom, clickedPos);
            }
            boardView.clearSelection();
            selectedFrom = null;
        }
    }

    // オブザーバーパターン実装 - ゲーム状態が変化したらUIを更新
    @Override
    public void onGameStateChanged(GameState state) {
        boardView.setBoard(state.getBoard());
        boardView.repaint();
    }

    @Override
    public void onCheck(Color attacker) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setContentText(attacker + "方が王手！");
            alert.show();
        });
    }
}
```

**JavaFX 機能の活用**:
- `MouseEvent` - マウスクリックイベント
- `Platform.runLater()` - UI スレッド更新
- `Alert` - 警告ダイアログ

### 4. ダイアログコンポーネント

プロジェクトは JavaFX 組み込みダイアログをモード選択に使用：

- `GameModeDialog` - ゲームモード選択（PvP/PvE、難易度、先手）
- `EngineSettingsDialog` - 外部エンジン設定

```java
// 例：モード選択ダイアログ
public class GameModeDialog extends Dialog<GameModeConfig> {

    private ChoiceBox<GameMode> modeChoice;
    private ChoiceBox<DifficultyLevel> difficultyChoice;
    private ChoiceBox<Color> firstHandChoice;

    public GameModeDialog() {
        setTitle("ゲームモードを選択");

        // オプションコントロールを作成
        modeChoice = new ChoiceBox<>();
        modeChoice.getItems().addAll(GameMode.PVP, GameMode.PVE);

        difficultyChoice = new ChoiceBox<>();
        difficultyChoice.getItems().addAll(DifficultyLevel.values());

        // 確認ボタンを設定
        ButtonType confirmType = new ButtonType("確認", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(confirmType, ButtonType.CANCEL);

        // レイアウト
        GridPane grid = new GridPane();
        grid.add(new Label("モード:"), 0, 0);
        grid.add(modeChoice, 1, 0);
        // ...
    }
}
```

### 5. スタイルとテーマ

**ファイル位置**: `src/main/resources/css/style.css`

CSS を使用して JavaFX コントロールスタイルをカスタマイズ：

```css
/* メニューBARスタイル */
.menu-bar {
    -fx-background-color: #f5f5f5;
}

.menu-item:hover {
    -fx-background-color: #e0e0e0;
}

/* ツールBARボタンスタイル */
.tool-bar .button {
    -fx-background-radius: 4;
    -fx-border-radius: 4;
    -fx-padding: 5 10;
}

.tool-bar .button:hover {
    -fx-background-color: #e0e0e0;
}

/* 盤面コンテナスタイル */
.board-container {
    -fx-background-color: #DEB887;
    -fx-border-color: #8B4513;
    -fx-border-width: 2;
}
```

---

## データ層

### 1. アプリケーション設定 (AppSettings)

**ファイル位置**: `src/main/java/cn/chinesechess/data/AppSettings.java`

Properties を使用してユーザー設定を保存：

```java
public class AppSettings {
    private static final String SETTINGS_FILE = "settings.properties";

    // シングルトンパターン
    private static AppSettings instance;

    private Properties properties;

    public static AppSettings getInstance() {
        if (instance == null) {
            instance = new AppSettings();
        }
        return instance;
    }

    // 設定の保存/読み込み
    public void save() throws IOException {
        try (FileOutputStream fos = new FileOutputStream(SETTINGS_FILE)) {
            properties.store(fos, "Chinese Chess Settings");
        }
    }

    public void load() throws IOException {
        try (FileInputStream fis = new FileInputStream(SETTINGS_FILE)) {
            properties.load(fis);
        }
    }
}
```

### 2. 棋譜管理 (GameRecordManager)

**ファイル位置**: `src/main/java/cn/chinesechess/data/GameRecordManager.java`

棋譜の保存と読み込みを実装：

```java
public class GameRecordManager {

    // 棋譜をテキスト形式で保存
    public void saveRecord(GameRecord record, File file) throws IOException {
        try (PrintWriter writer = new PrintWriter(file)) {
            writer.println("中国象棋棋譜");
            writer.println("=".repeat(20));

            int moveNumber = 1;
            for (Move move : record.getMoves()) {
                if (move.getColor() == Color.RED) {
                    writer.printf("%d. %s", moveNumber, move.toChineseNotation());
                } else {
                    writer.printf("  %s%n", move.toChineseNotation());
                    moveNumber++;
                }
            }
        }
    }
}
```

---

## まとめ

このプロジェクトはデスクトップアプリケーション開発における JavaFX の完全な活用を示しています：

| 機能 | 適用シナリオ |
|------|--------------|
| `Application` | アプリケーションエントリ точки とライフサイクル管理 |
| `Stage` / `Scene` | ウィンドウとシーン管理 |
| `Canvas` | カスタムグラフィックス描画（盤面） |
| `MenuBar` / `ToolBar` | メニューとツールBAR |
| `Dialog` | モード選択ダイアログ |
| `Accelerator` | キーボードショートカット |
| CSS | スタイルカスタマイズ |
| オブザーバーパターン | UI とエンジンの疎結合化 |
| `Platform.runLater()` | スレッドセーフな UI 更新 |
