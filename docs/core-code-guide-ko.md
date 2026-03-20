# 핵심 코드 설명 및 JavaFX 활용

이 문서에서는 중국 장기 프로젝트의 핵심 코드 아키텍처와 본 프로젝트에서 JavaFX를 구체적으로 활용하는 방법에 대해 자세히 설명합니다.

## 목차

- [아키텍처 개요](#아키텍처-개요)
- [코어 엔진 계층](#코어-엔진-계층)
- [AI 엔진 계층](#ai-엔색-계층)
- [UI 계층과 JavaFX 활용](#ui-계층과-javafx-활용)
- [데이터 계층](#데이터-계층)

---

## 아키텍처 개요

프로젝트는 고전적인 **MVC + 옵저버 패턴** 아키텍처를 채택합니다:

```
┌─────────────────────────────────────────────────────────────┐
│                         UI 계층                              │
│  ChineseChessApp → BoardView → GameController → AIIntegration│
└──────────────────────────┬──────────────────────────────────┘
                           │ 옵저버 패턴
┌──────────────────────────▼──────────────────────────────────┐
│                      코어 엔진 계층                          │
│  GameEngine → MoveGenerator → CheckDetector →棋子 규칙 클래스 │
└──────────────────────────┬──────────────────────────────────┘
                           │
┌──────────────────────────▼──────────────────────────────────┐
│                        데이터 계층                           │
│              AppSettings → GameRecordManager                │
└─────────────────────────────────────────────────────────────┘
```

---

## 코어 엔진 계층

### 1. 게임 엔진 (GameEngine)

**파일 위치**: `src/main/java/cn/chinesechess/core/engine/GameEngine.java`

게임 엔진은 전체 애플리케이션의 핵심으로, 게임 상태 관리, 수순 로직 처리, 승패 판정을 담당합니다.

**핵심 업무**:
- 수순 검증 및 실행
- 순서 관리
- 체크/체크메이트/스테일메이트 탐지
- 세 번 반복 상황 무승부 판정
- 이벤트 알림 (옵저버 패턴)

**주요 코드 부분** - 수순 실행 및 상태 탐지:

```java
@Override
public MoveResult makeMove(Position from, Position to) {
    // 1. 기물을 가져오고 소유권 검증
    Piece piece = board.getPiece(from);
    if (piece == null || piece.getColor() != currentTurn) {
        return MoveResult.INVALID;
    }

    // 2. 목표 위치의 합법성 검증
    List<Position> legalTargets = getLegalTargets(from);
    if (!legalTargets.contains(to)) {
        return MoveResult.INVALID;
    }

    // 3. 수순 실행 (커맨드 패턴)
    MoveCommand command = new MoveCommand(move, piece);
    command.execute(board);
    moveHistory.add(command);

    // 4. 게임 상태 탐지
    if (checkDetector.isCheckmate(board, nextTurn)) {
        result = MoveResult.CHECKMATE;
        gameResult = (nextTurn == Color.RED) ? GameResult.BLACK_WIN : GameResult.RED_WIN;
    } else if (checkDetector.isInCheck(board, nextTurn)) {
        result = MoveResult.CHECK;
    }
    // ...

    // 5. UI 계층에 알림
    notifyMoveMade(move, result);
    notifyStateChanged();
    return result;
}
```

### 2. 수순 생성기 (MoveGenerator)

**파일 위치**: `src/main/java/cn/chinesechess/core/rules/MoveGenerator.java`

모든 기물의 합법적인 수순을 생성하며, 중국 장기 규칙의 핵심 구현입니다.

**디자인 패턴 - 전략 패턴**:
- 각 기물 유형에 해당하는 `IMoveRule` 구현 클래스
- `MoveRuleFactory` 팩토리가 해당 규칙 핸들러 생성

```java
// 수순 생성 핵심 로직
public List<Move> getLegalMoves(IBoard board, Color color) {
    List<Move> legalMoves = new ArrayList<>();

    // 현재 플레이어의 모든 기물 순회
    for (Piece piece : board.getPiecesByColor(color)) {
        // 해당 기물 유형의 규칙 핸들러 가져오기
        IMoveRule rule = MoveRuleFactory.getRule(piece.getType());
        // 해당 기물의 모든 합법적 수순 생성
        List<Position> targets = rule.getLegalTargets(board, piece);
        for (Position target : targets) {
            // 수순 시뮬레이션, 자국이 체크되는지 검증
            if (!wouldCauseSelfCheck(board, piece, target)) {
                legalMoves.add(new Move(piece.getPosition(), target, captured));
            }
        }
    }
    return legalMoves;
}
```

### 3. 기물 규칙 클래스

각 기물 규칙 구현은 `cn.chinesechess.core.rules` 패키지에 있습니다:

| 클래스 | 해당 기물 | 규칙 특징 |
|--------|----------|------------|
| `RookMoveRule` | 차(車) | 직선 이동, 장애물 없음 |
| `KnightMoveRule` | 마(馬) | 일자형, "다리 막기" 탐지 |
| `CannonMoveRule` | 포(炮) | 직선 이동, 잡으려면 점프 필요 |
| `PawnMoveRule` | 병/졸(兵/卒) | 강 건너前后 다른 이동 |
| `KingMoveRule` | 장/수(將/帥) | 궁 내에서 이동 |
| `AdvisorMoveRule` | 사(士) | 대각선, 궁 내 |
| `ElephantMoveRule` | 상(象) | 전자형, "눈 막기" 탐지 |

---

## AI 엔진 계층

### 1. AI 인터페이스 설계

**파일 위치**: `src/main/java/cn/chinesechess/ai/IAIEngine.java`

AI 엔진의 통합 인터페이스를 정의하며,多种 엔진 구현 지원:

```java
public interface IAIEngine {
    /** 최선 수순 검색 */
    Move searchBestMove(IBoard board, Color color, int depth);

    /** 검색 깊이 설정 (난이도) */
    void setDepth(int depth);

    /** 검색 중지 */
    void stop();
}
```

### 2. 내장 Minimax 엔진

**파일 위치**: `src/main/java/cn/chinesechess/ai/MinimaxEngine.java`

Alpha-Beta 가지치기 Minimax 알고리즘 구현:

```java
@Override
public Move searchBestMove(IBoard board, Color color, int depth) {
    // 평가 함수 + Minimax 재귀 검색
    Move bestMove = null;
    int bestScore = color == Color.RED ? Integer.MIN_VALUE : Integer.MAX_VALUE;

    for (Move move : moveGenerator.getLegalMoves(board, color)) {
        // 1. 수순 실행
        board.makeMove(move);

        // 2. 재귀 검색
        int score = minimax(board, depth - 1, alpha, beta,
                color.opposite(), false);

        // 3. 수순 취소
        board.undoMove();

        // 4. 최선 수순 업데이트
        if (color == Color.RED ? score > bestScore : score < bestScore) {
            bestScore = score;
            bestMove = move;
        }

        // 5. Alpha-Beta 가지치기
        if (color == Color.RED) {
            alpha = Math.max(alpha, bestScore);
        } else {
            beta = Math.min(beta, bestScore);
        }
        if (beta <= alpha) break; // 가지치기
    }
    return bestMove;
}
```

### 3. 판정 평가 (BoardEvaluator)

**파일 위치**: `src/main/java/cn/chinesechess/ai/BoardEvaluator.java`

정적 판정 평가 함수로 다음을 고려:
- 기물 가치 점수 (차>마>포>병...)
- 기물 위치 점수 (각 기물의 다른 위치에 따른 점수)
- 유연성 점수 (가능한 수순 수)
- 위협과 보호 관계

---

## UI 계층과 JavaFX 활용

### 1. 애플리케이션 진입점 (ChineseChessApp)

**파일 위치**: `src/main/java/cn/chinesechess/ui/ChineseChessApp.java`

`javafx.application.Application`을 상속하며 JavaFX 애플리케이션 진입점:

```java
public class ChineseChessApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        // 1. 게임 엔진 및 뷰 생성
        IGameEngine gameEngine = new GameEngine();
        BoardView boardView = new BoardView();
        GameController controller = new GameController(gameEngine, boardView, engineManager);

        // 2. 레이아웃 생성 (BorderPane: 상-중-하 구조)
        BorderPane root = new BorderPane();
        root.setCenter(boardView);
        root.setTop(menuBar);
        root.setBottom(statusBar);

        // 3. 씬 생성 및 CSS 로드
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

        // 4. 키보드 단축키 등록
        scene.getAccelerators().put(
            new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN),
            () -> showNewGameDialog(controller, owner)
        );

        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
```

**JavaFX 기능 활용**:
- `Application` - JavaFX 애플리케이션 기본 클래스
- `Stage` / `Scene` - 창 및 씬
- `BorderPane` - 테두리 레이아웃
- `MenuBar` - 메뉴 모음
- `ToolBar` - 도구 모음
- `Accelerator` - 키보드 단축키

### 2. 판독 뷰 (BoardView)

**파일 위치**: `src/main/java/cn/chinesechess/ui/BoardView.java`

`Canvas`를 사용하여 판독 그리기를 구현하고 `StackPane`을 상속:

```java
public class BoardView extends StackPane {

    private final Canvas canvas;
    private IBoard board;

    public BoardView() {
        canvas = new Canvas(BOARD_WIDTH, BOARD_HEIGHT);
        getChildren().add(canvas);
    }

    // 판독 그리드 그리기
    private void drawBoard(GraphicsContext gc) {
        // 9개의 가로줄 그리기
        for (int row = 0; row <= 9; row++) {
            gc.strokeLine(x1, y, x2, y);
        }
        // 10개의 세로줄 그리기 (하천 구간 끊김)
        // 궁 대각선 그리기
        // 병/포 위치 표시 그리기
    }

    // 기물 그리기
    private void drawPieces(GraphicsContext gc) {
        for (Piece piece : board.getAllPieces()) {
            // 기물 배경 원 그리기
            gc.setFill(Color.rgb(255, 248, 220));
            gc.fillOval(x - radius, y - radius, radius * 2, radius * 2);

            // 테두리 그리기 (적색:红色, 검은색: 검정)
            gc.setStroke(piece.getColor() == Color.RED ?
                Color.rgb(180, 0, 0) : Color.rgb(30, 30, 30));

            // 기물 문자 그리기 (캘체)
            gc.setFont(Font.font("楷体", FontWeight.BOLD, 26));
            gc.fillText(piece.getDisplayName(), textX, textY);
        }
    }

    // 좌표 변환
    public Position canvasToPosition(double canvasX, double canvasY) {
        int col = (int) Math.round((canvasX - PADDING) / CELL_SIZE);
        int row = (int) Math.round((canvasY - PADDING) / CELL_SIZE);
        // 뒤집기 상태 처리
        if (flipped) {
            col = 8 - col;
            row = 9 - row;
        }
        return new Position(col, row);
    }
}
```

**JavaFX 기능 활용**:
- `Canvas` + `GraphicsContext` - 사용자 정의 그리기
- 마우스 이벤트 처리 - `setOnMouseClicked`
- 좌표계 변환 - 화면 좌표에서 판독 좌표로

### 3. 게임 컨트롤러 (GameController)

**파일 위치**: `src/main/java/cn/chinesechess/ui/GameController.java`

UI 계층과 핵심 엔진을 연결하고 사용자 상호작용 처리:

```java
public class GameController implements GameEventListener {

    public GameController(IGameEngine gameEngine, BoardView boardView, EngineManager engineManager) {
        // 1. 게임 이벤트 리스너 등록
        gameEngine.addEventListener(this);

        // 2. 마우스 클릭 이벤트 바인딩
        boardView.getCanvas().setOnMouseClicked(this::handleBoardClick);
    }

    // 판독 클릭 처리
    private void handleBoardClick(MouseEvent event) {
        Position clickedPos = boardView.canvasToPosition(event.getX(), event.getY());

        if (selectedFrom == null) {
            // 첫 번째 클릭: 기물 선택
            Piece piece = board.getPiece(clickedPos);
            if (piece != null && piece.getColor() == gameEngine.getCurrentTurn()) {
                selectedFrom = clickedPos;
                currentLegalTargets = gameEngine.getLegalTargets(clickedPos);
                boardView.setSelected(clickedPos, currentLegalTargets);
            }
        } else {
            // 두 번째 클릭: 수순 또는 취소
            if (currentLegalTargets.contains(clickedPos)) {
                gameEngine.makeMove(selectedFrom, clickedPos);
            }
            boardView.clearSelection();
            selectedFrom = null;
        }
    }

    // 옵저버 패턴 구현 - 게임 상태 변경 시 UI 업데이트
    @Override
    public void onGameStateChanged(GameState state) {
        boardView.setBoard(state.getBoard());
        boardView.repaint();
    }

    @Override
    public void onCheck(Color attacker) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setContentText(attacker + "측 체크!");
            alert.show();
        });
    }
}
```

**JavaFX 기능 활용**:
- `MouseEvent` - 마우스 클릭 이벤트
- `Platform.runLater()` - UI 스레드 업데이트
- `Alert` - 경고 대화상자

### 4. 대화상자 컴포넌트

프로젝트는 JavaFX 내장 대화상자를 모드 선택에 사용:

- `GameModeDialog` - 게임 모드 선택 (PVP/PVE, 난이도, 선공)
- `EngineSettingsDialog` - 외부 엔진 설정

```java
// 예시: 모드 선택 대화상자
public class GameModeDialog extends Dialog<GameModeConfig> {

    private ChoiceBox<GameMode> modeChoice;
    private ChoiceBox<DifficultyLevel> difficultyChoice;
    private ChoiceBox<Color> firstHandChoice;

    public GameModeDialog() {
        setTitle("게임 모드 선택");

        // 옵션 컨트롤 생성
        modeChoice = new ChoiceBox<>();
        modeChoice.getItems().addAll(GameMode.PVP, GameMode.PVE);

        difficultyChoice = new ChoiceBox<>();
        difficultyChoice.getItems().addAll(DifficultyLevel.values());

        // 확인 버튼 설정
        ButtonType confirmType = new ButtonType("확인", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(confirmType, ButtonType.CANCEL);

        // 레이아웃
        GridPane grid = new GridPane();
        grid.add(new Label("모드:"), 0, 0);
        grid.add(modeChoice, 1, 0);
        // ...
    }
}
```

### 5. 스타일 및 테마

**파일 위치**: `src/main/resources/css/style.css`

CSS를 사용하여 JavaFX 컨트롤 스타일 사용자 정의:

```css
/* 메뉴 모음 스타일 */
.menu-bar {
    -fx-background-color: #f5f5f5;
}

.menu-item:hover {
    -fx-background-color: #e0e0e0;
}

/* 도구 모음 버튼 스타일 */
.tool-bar .button {
    -fx-background-radius: 4;
    -fx-border-radius: 4;
    -fx-padding: 5 10;
}

.tool-bar .button:hover {
    -fx-background-color: #e0e0e0;
}

/* 판독 컨테이너 스타일 */
.board-container {
    -fx-background-color: #DEB887;
    -fx-border-color: #8B4513;
    -fx-border-width: 2;
}
```

---

## 데이터 계층

### 1. 애플리케이션 설정 (AppSettings)

**파일 위치**: `src/main/java/cn/chinesechess/data/AppSettings.java`

Properties를 사용하여 사용자 기본 설정 저장:

```java
public class AppSettings {
    private static final String SETTINGS_FILE = "settings.properties";

    // 싱글톤 패턴
    private static AppSettings instance;

    private Properties properties;

    public static AppSettings getInstance() {
        if (instance == null) {
            instance = new AppSettings();
        }
        return instance;
    }

    // 설정 저장/불러오기
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

### 2. 기보 관리 (GameRecordManager)

**파일 위치**: `src/main/java/cn/chinesechess/data/GameRecordManager.java`

기보 저장 및 불러오기 구현:

```java
public class GameRecordManager {

    // 기보 텍스트 형식으로 저장
    public void saveRecord(GameRecord record, File file) throws IOException {
        try (PrintWriter writer = new PrintWriter(file)) {
            writer.println("중국 장기 기보");
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

## 요약

이 프로젝트는 데스크톱 애플리케이션 개발에서 JavaFX의 완전한 활용을 보여줍니다:

| 기능 | 적용 시나리오 |
|------|--------------|
| `Application` | 애플리케이션 진입점 및 생명주기 관리 |
| `Stage` / `Scene` | 창 및 씬 관리 |
| `Canvas` | 사용자 정의 그래픽 그리기 (판독) |
| `MenuBar` / `ToolBar` | 메뉴 및 도구 모음 |
| `Dialog` | 모드 선택 대화상자 |
| `Accelerator` | 키보드 단축키 |
| CSS | 스타일 사용자 정의 |
| 옵저버 패턴 | UI와 엔진 분리 |
| `Platform.runLater()` | 스레드 안전한 UI 업데이트 |
