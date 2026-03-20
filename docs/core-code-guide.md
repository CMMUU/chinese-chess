# Core Code Guide & JavaFX Usage

This document provides a detailed explanation of the Chinese Chess project's core code architecture and how JavaFX is specifically used in this project.

## Table of Contents

- [Architecture Overview](#architecture-overview)
- [Core Engine Layer](#core-engine-layer)
- [AI Engine Layer](#ai-engine-layer)
- [UI Layer & JavaFX Usage](#ui-layer--javafx-usage)
- [Data Layer](#data-layer)

---

## Architecture Overview

The project uses a classic **MVC + Observer Pattern** architecture:

```
┌─────────────────────────────────────────────────────────────┐
│                         UI Layer                            │
│  ChineseChessApp → BoardView → GameController → AIIntegration│
└──────────────────────────┬──────────────────────────────────┘
                           │ Observer Pattern
┌──────────────────────────▼──────────────────────────────────┐
│                      Core Engine Layer                      │
│  GameEngine → MoveGenerator → CheckDetector → Piece Rules   │
└──────────────────────────┬──────────────────────────────────┘
                           │
┌──────────────────────────▼──────────────────────────────────┐
│                       Data Layer                             │
│              AppSettings → GameRecordManager                │
└─────────────────────────────────────────────────────────────┘
```

---

## Core Engine Layer

### 1. Game Engine (GameEngine)

**File Location**: `src/main/java/cn/chinesechess/core/engine/GameEngine.java`

The game engine is the core of the entire application, responsible for managing game state, handling move logic, and determining win/loss conditions.

**Core Responsibilities**:
- Move validation and execution
- Turn management
- Check/Checkmate/Stalemate detection
- Threefold repetition draw detection
- Event notification (Observer Pattern)

**Key Code Snippet** - Move Execution and State Detection:

```java
@Override
public MoveResult makeMove(Position from, Position to) {
    // 1. Get piece and verify ownership
    Piece piece = board.getPiece(from);
    if (piece == null || piece.getColor() != currentTurn) {
        return MoveResult.INVALID;
    }

    // 2. Validate target position legality
    List<Position> legalTargets = getLegalTargets(from);
    if (!legalTargets.contains(to)) {
        return MoveResult.INVALID;
    }

    // 3. Execute move (Command Pattern)
    MoveCommand command = new MoveCommand(move, piece);
    command.execute(board);
    moveHistory.add(command);

    // 4. Detect game state
    if (checkDetector.isCheckmate(board, nextTurn)) {
        result = MoveResult.CHECKMATE;
        gameResult = (nextTurn == Color.RED) ? GameResult.BLACK_WIN : GameResult.RED_WIN;
    } else if (checkDetector.isInCheck(board, nextTurn)) {
        result = MoveResult.CHECK;
    }
    // ...

    // 5. Notify UI layer
    notifyMoveMade(move, result);
    notifyStateChanged();
    return result;
}
```

### 2. Move Generator (MoveGenerator)

**File Location**: `src/main/java/cn/chinesechess/core/rules/MoveGenerator.java`

Responsible for generating legal moves for all pieces, implementing the core Chinese chess rules.

**Design Pattern - Strategy Pattern**:
- Each piece type has a corresponding `IMoveRule` implementation
- `MoveRuleFactory` creates the appropriate rule handler

```java
// Core move generation logic
public List<Move> getLegalMoves(IBoard board, Color color) {
    List<Move> legalMoves = new ArrayList<>();

    // Iterate through all pieces of the current player
    for (Piece piece : board.getPiecesByColor(color)) {
        // Get the rule handler for this piece type
        IMoveRule rule = MoveRuleFactory.getRule(piece.getType());
        // Generate all legal moves for this piece
        List<Position> targets = rule.getLegalTargets(board, piece);
        for (Position target : targets) {
            // Simulate move, verify it doesn't cause self-check
            if (!wouldCauseSelfCheck(board, piece, target)) {
                legalMoves.add(new Move(piece.getPosition(), target, captured));
            }
        }
    }
    return legalMoves;
}
```

### 3. Piece Rule Classes

Piece rule implementations are located in the `cn.chinesechess.core.rules` package:

| Class | Piece | Rule Features |
|-------|-------|---------------|
| `RookMoveRule` | Chariot (Che) | Linear movement, no obstacles |
| `KnightMoveRule` | Horse (Ma) | L-shape, "blocking leg" detection |
| `CannonMoveRule` | Cannon (Pao) | Linear, capture requires jumping |
| `PawnMoveRule` | Soldier (Bing/Zu) | Different moves before/after crossing river |
| `KingMoveRule` | General (Jiang/Shuai) | Moves within palace |
| `AdvisorMoveRule` | Advisor (Shi) | Diagonal, within palace |
| `ElephantMoveRule` | Elephant (Xiang) |田-shape, "blocking eye" detection |

---

## AI Engine Layer

### 1. AI Interface Design

**File Location**: `src/main/java/cn/chinesechess/ai/IAIEngine.java`

Defines the unified AI engine interface, supporting multiple engine implementations:

```java
public interface IAIEngine {
    /** Search for best move */
    Move searchBestMove(IBoard board, Color color, int depth);

    /** Set search depth (difficulty) */
    void setDepth(int depth);

    /** Stop search */
    void stop();
}
```

### 2. Built-in Minimax Engine

**File Location**: `src/main/java/cn/chinesechess/ai/MinimaxEngine.java`

Implements Minimax algorithm with Alpha-Beta pruning:

```java
@Override
public Move searchBestMove(IBoard board, Color color, int depth) {
    // Evaluation function + Minimax recursive search
    Move bestMove = null;
    int bestScore = color == Color.RED ? Integer.MIN_VALUE : Integer.MAX_VALUE;

    for (Move move : moveGenerator.getLegalMoves(board, color)) {
        // 1. Execute move
        board.makeMove(move);

        // 2. Recursive search
        int score = minimax(board, depth - 1, alpha, beta,
                color.opposite(), false);

        // 3. Undo move
        board.undoMove();

        // 4. Update best move
        if (color == Color.RED ? score > bestScore : score < bestScore) {
            bestScore = score;
            bestMove = move;
        }

        // 5. Alpha-Beta pruning
        if (color == Color.RED) {
            alpha = Math.max(alpha, bestScore);
        } else {
            beta = Math.min(beta, bestScore);
        }
        if (beta <= alpha) break; // Pruning
    }
    return bestMove;
}
```

### 3. Board Evaluator (BoardEvaluator)

**File Location**: `src/main/java/cn/chinesechess/ai/BoardEvaluator.java`

Static position evaluation function considering:
- Piece value scores (Chariot > Horse > Cannon > Soldier...)
- Position-specific scores
- Flexibility scores (number of available moves)
- Threat and protection relationships

---

## UI Layer & JavaFX Usage

### 1. Application Entry (ChineseChessApp)

**File Location**: `src/main/java/cn/chinesechess/ui/ChineseChessApp.java`

Extends `javafx.application.Application`, serving as the JavaFX application entry point:

```java
public class ChineseChessApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        // 1. Create game engine and view
        IGameEngine gameEngine = new GameEngine();
        BoardView boardView = new BoardView();
        GameController controller = new GameController(gameEngine, boardView, engineManager);

        // 2. Create layout (BorderPane: top-center-bottom)
        BorderPane root = new BorderPane();
        root.setCenter(boardView);
        root.setTop(menuBar);
        root.setBottom(statusBar);

        // 3. Create scene and load CSS
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

        // 4. Register keyboard shortcuts
        scene.getAccelerators().put(
            new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN),
            () -> showNewGameDialog(controller, owner)
        );

        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
```

**JavaFX Features Used**:
- `Application` - JavaFX application base class
- `Stage` / `Scene` - Window and scene
- `BorderPane` - Border layout
- `MenuBar` - Menu bar
- `ToolBar` - Toolbar
- `Accelerator` - Keyboard shortcuts

### 2. Board View (BoardView)

**File Location**: `src/main/java/cn/chinesechess/ui/BoardView.java`

Uses `Canvas` for board rendering, extends `StackPane`:

```java
public class BoardView extends StackPane {

    private final Canvas canvas;
    private IBoard board;

    public BoardView() {
        canvas = new Canvas(BOARD_WIDTH, BOARD_HEIGHT);
        getChildren().add(canvas);
    }

    // Draw board grid
    private void drawBoard(GraphicsContext gc) {
        // Draw 9 horizontal lines
        for (int row = 0; row <= 9; row++) {
            gc.strokeLine(x1, y, x2, y);
        }
        // Draw 10 vertical lines (broken at river)
        // Draw palace diagonals
        // Draw soldier/cannon position markers
    }

    // Draw pieces
    private void drawPieces(GraphicsContext gc) {
        for (Piece piece : board.getAllPieces()) {
            // Draw piece background circle
            gc.setFill(Color.rgb(255, 248, 220));
            gc.fillOval(x - radius, y - radius, radius * 2, radius * 2);

            // Draw border (red for red side, black for black side)
            gc.setStroke(piece.getColor() == Color.RED ?
                Color.rgb(180, 0, 0) : Color.rgb(30, 30, 30));

            // Draw piece text (KaiTi font)
            gc.setFont(Font.font("KaiTi", FontWeight.BOLD, 26));
            gc.fillText(piece.getDisplayName(), textX, textY);
        }
    }

    // Coordinate conversion
    public Position canvasToPosition(double canvasX, double canvasY) {
        int col = (int) Math.round((canvasX - PADDING) / CELL_SIZE);
        int row = (int) Math.round((canvasY - PADDING) / CELL_SIZE);
        // Handle flipped state
        if (flipped) {
            col = 8 - col;
            row = 9 - row;
        }
        return new Position(col, row);
    }
}
```

**JavaFX Features Used**:
- `Canvas` + `GraphicsContext` - Custom drawing
- Mouse event handling - `setOnMouseClicked`
- Coordinate system conversion - Screen to board coordinates

### 3. Game Controller (GameController)

**File Location**: `src/main/java/cn/chinesechess/ui/GameController.java`

Connects UI layer with core engine, handles user interaction:

```java
public class GameController implements GameEventListener {

    public GameController(IGameEngine gameEngine, BoardView boardView, EngineManager engineManager) {
        // 1. Register game event listener
        gameEngine.addEventListener(this);

        // 2. Bind mouse click event
        boardView.getCanvas().setOnMouseClicked(this::handleBoardClick);
    }

    // Handle board click
    private void handleBoardClick(MouseEvent event) {
        Position clickedPos = boardView.canvasToPosition(event.getX(), event.getY());

        if (selectedFrom == null) {
            // First click: select piece
            Piece piece = board.getPiece(clickedPos);
            if (piece != null && piece.getColor() == gameEngine.getCurrentTurn()) {
                selectedFrom = clickedPos;
                currentLegalTargets = gameEngine.getLegalTargets(clickedPos);
                boardView.setSelected(clickedPos, currentLegalTargets);
            }
        } else {
            // Second click: move or cancel
            if (currentLegalTargets.contains(clickedPos)) {
                gameEngine.makeMove(selectedFrom, clickedPos);
            }
            boardView.clearSelection();
            selectedFrom = null;
        }
    }

    // Observer Pattern implementation - Update UI on game state change
    @Override
    public void onGameStateChanged(GameState state) {
        boardView.setBoard(state.getBoard());
        boardView.repaint();
    }

    @Override
    public void onCheck(Color attacker) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setContentText(attacker + " side has CHECK!");
            alert.show();
        });
    }
}
```

**JavaFX Features Used**:
- `MouseEvent` - Mouse click events
- `Platform.runLater()` - UI thread update
- `Alert` - Warning dialogs

### 4. Dialog Components

The project uses JavaFX built-in dialogs for mode selection:

- `GameModeDialog` - Game mode selection (PVP/PVE, difficulty, first hand)
- `EngineSettingsDialog` - External engine configuration

```java
// Example: Mode selection dialog
public class GameModeDialog extends Dialog<GameModeConfig> {

    private ChoiceBox<GameMode> modeChoice;
    private ChoiceBox<DifficultyLevel> difficultyChoice;
    private ChoiceBox<Color> firstHandChoice;

    public GameModeDialog() {
        setTitle("Select Game Mode");

        // Create option controls
        modeChoice = new ChoiceBox<>();
        modeChoice.getItems().addAll(GameMode.PVP, GameMode.PVE);

        difficultyChoice = new ChoiceBox<>();
        difficultyChoice.getItems().addAll(DifficultyLevel.values());

        // Set confirm button
        ButtonType confirmType = new ButtonType("Confirm", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(confirmType, ButtonType.CANCEL);

        // Layout
        GridPane grid = new GridPane();
        grid.add(new Label("Mode:"), 0, 0);
        grid.add(modeChoice, 1, 0);
        // ...
    }
}
```

### 5. Styles and Themes

**File Location**: `src/main/resources/css/style.css`

Use CSS to customize JavaFX control styles:

```css
/* Menu bar styles */
.menu-bar {
    -fx-background-color: #f5f5f5;
}

.menu-item:hover {
    -fx-background-color: #e0e0e0;
}

/* Toolbar button styles */
.tool-bar .button {
    -fx-background-radius: 4;
    -fx-border-radius: 4;
    -fx-padding: 5 10;
}

.tool-bar .button:hover {
    -fx-background-color: #e0e0e0;
}

/* Board container styles */
.board-container {
    -fx-background-color: #DEB887;
    -fx-border-color: #8B4513;
    -fx-border-width: 2;
}
```

---

## Data Layer

### 1. Application Settings (AppSettings)

**File Location**: `src/main/java/cn/chinesechess/data/AppSettings.java`

Uses Properties to store user preferences:

```java
public class AppSettings {
    private static final String SETTINGS_FILE = "settings.properties";

    // Singleton pattern
    private static AppSettings instance;

    private Properties properties;

    public static AppSettings getInstance() {
        if (instance == null) {
            instance = new AppSettings();
        }
        return instance;
    }

    // Save/Load settings
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

### 2. Game Record Manager (GameRecordManager)

**File Location**: `src/main/java/cn/chinesechess/data/GameRecordManager.java`

Implements game record save and load:

```java
public class GameRecordManager {

    // Save record as text format
    public void saveRecord(GameRecord record, File file) throws IOException {
        try (PrintWriter writer = new PrintWriter(file)) {
            writer.println("Chinese Chess Record");
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

## Summary

This project demonstrates the complete use of JavaFX in desktop application development:

| Feature | Application Scenario |
|---------|---------------------|
| `Application` | Application entry and lifecycle management |
| `Stage` / `Scene` | Window and scene management |
| `Canvas` | Custom graphics rendering (board) |
| `MenuBar` / `ToolBar` | Menu and toolbar |
| `Dialog` | Mode selection dialogs |
| `Accelerator` | Keyboard shortcuts |
| CSS | Style customization |
| Observer Pattern | UI and engine decoupling |
| `Platform.runLater()` | Thread-safe UI updates |
