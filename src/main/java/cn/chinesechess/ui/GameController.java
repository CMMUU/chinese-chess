package cn.chinesechess.ui;

import cn.chinesechess.ai.DifficultyLevel;
import cn.chinesechess.ai.EngineManager;
import cn.chinesechess.core.*;
import cn.chinesechess.core.engine.GameEventListener;
import cn.chinesechess.core.engine.IGameEngine;
import cn.chinesechess.data.GameRecord;
import cn.chinesechess.data.GameRecordManager;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.Collections;
import java.util.List;

/**
 * 游戏控制器
 * <p>处理棋盘点击交互：选棋→显示合法位置→走棋。
 * 同时处理工具栏操作（悔棋、认输、求和、保存/加载棋谱）。
 * 支持人机对战模式下的 AI 自动走棋。</p>
 */
public class GameController implements GameEventListener {

    /** 游戏引擎 */
    private final IGameEngine gameEngine;

    /** 棋盘视图 */
    private final BoardView boardView;

    /** 状态栏标签 */
    private final Label statusLabel;

    /** AI 集成组件 */
    private final AIIntegration aiIntegration;

    /** 棋谱管理器 */
    private final GameRecordManager recordManager;

    /** 当前选中的起始位置 */
    private Position selectedFrom;

    /** 当前选中棋子的合法目标位置 */
    private List<Position> currentLegalTargets;

    public GameController(IGameEngine gameEngine, BoardView boardView) {
        this(gameEngine, boardView, null);
    }

    public GameController(IGameEngine gameEngine, BoardView boardView, EngineManager engineManager) {
        this.gameEngine = gameEngine;
        this.boardView = boardView;
        this.statusLabel = new Label("准备开始");
        this.currentLegalTargets = Collections.emptyList();
        this.aiIntegration = new AIIntegration();
        this.recordManager = new GameRecordManager();

        // 注册事件监听器
        gameEngine.addEventListener(this);

        // 绑定鼠标点击事件
        boardView.getCanvas().setOnMouseClicked(this::handleBoardClick);

        // 设置 AI 状态回调
        aiIntegration.setStatusCallback(status ->
                Platform.runLater(() -> statusLabel.setText(status)));
    }

    /**
     * 获取状态栏标签
     * @return 状态标签
     */
    public Label getStatusLabel() {
        return statusLabel;
    }

    /**
     * 配置游戏模式
     * @param mode 游戏模式
     * @param difficulty AI 难度（仅 PVE 模式有效）
     * @param firstHand 先手方
     */
    public void configureGame(GameMode mode, DifficultyLevel difficulty, Color firstHand) {
        gameEngine.setGameMode(mode);
        gameEngine.setFirstHand(firstHand);

        aiIntegration.setEnabled(mode == GameMode.PVE);
        // PVE模式下，AI执对方颜色
        aiIntegration.setAiColor(firstHand == Color.RED ? Color.BLACK : Color.RED);
        if (mode == GameMode.PVE) {
            aiIntegration.setDifficulty(difficulty);
        }

        // 自动翻转棋盘：先手方显示在下方
        boardView.setFlipped(true);
    }

    /**
     * 开始新游戏
     */
    public void newGame() {
        gameEngine.newGame();
        selectedFrom = null;
        currentLegalTargets = Collections.emptyList();
        updateBoardView();
    }

    /**
     * 悔棋
     */
    public void undo() {
        if (gameEngine.isGameOver() || aiIntegration.isComputing()) {
            return;
        }
        // PVE 模式下悔两步（AI + 玩家各一步）
        if (aiIntegration.isEnabled()) {
            gameEngine.undoMove(); // 撤销 AI 的一步
            gameEngine.undoMove(); // 撤销玩家的一步
        } else {
            gameEngine.undoMove();
        }
        selectedFrom = null;
        currentLegalTargets = Collections.emptyList();
        updateBoardView();
    }

    /**
     * 认输
     */
    public void resign() {
        if (gameEngine.isGameOver()) {
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("认输确认");
        alert.setHeaderText(null);
        alert.setContentText("确定要认输吗？");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                gameEngine.resign(gameEngine.getCurrentTurn());
            }
        });
    }

    /**
     * 求和
     */
    public void offerDraw() {
        if (gameEngine.isGameOver()) {
            return;
        }

        // PVE 模式下直接接受和棋
        if (aiIntegration.isEnabled()) {
            gameEngine.acceptDraw();
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("求和确认");
        alert.setHeaderText(null);
        alert.setContentText("确定要提出和棋请求吗？对方将被询问是否接受。");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (gameEngine.offerDraw(gameEngine.getCurrentTurn())) {
                    showDrawOfferDialog();
                }
            }
        });
    }

    /**
     * 显示和棋请求对话框
     */
    private void showDrawOfferDialog() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("和棋请求");
        alert.setHeaderText(null);
        alert.setContentText("对方提出和棋，是否接受？");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                gameEngine.acceptDraw();
            }
        });
    }

    /**
     * 保存棋谱
     */
    public void saveGameRecord(Stage owner) {
        if (gameEngine.getMoveHistory().isEmpty()) {
            statusLabel.setText("没有走棋记录可保存");
            return;
        }

        FileChooser chooser = new FileChooser();
        chooser.setTitle("保存棋谱");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("棋谱文件", "*.json")
        );
        chooser.setInitialFileName("棋谱_" + System.currentTimeMillis() + ".json");

        File file = chooser.showSaveDialog(owner);
        if (file != null) {
            try {
                GameRecord record = new GameRecord(
                        gameEngine.getGameMode(),
                        gameEngine.getState(),
                        gameEngine.getMoveHistory()
                );
                recordManager.saveRecord(record, file.toPath());
                statusLabel.setText("棋谱已保存: " + file.getName());
            } catch (Exception ex) {
                showError("保存失败", "无法保存棋谱: " + ex.getMessage());
            }
        }
    }

    /**
     * 加载棋谱
     */
    public void loadGameRecord(Stage owner) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("加载棋谱");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("棋谱文件", "*.json")
        );

        File file = chooser.showOpenDialog(owner);
        if (file != null) {
            try {
                GameRecord record = recordManager.loadRecord(file.toPath());
                // 开始新游戏并回放走棋
                gameEngine.newGame();
                for (Move move : record.moves()) {
                    gameEngine.makeMove(move.from(), move.to());
                }
                selectedFrom = null;
                currentLegalTargets = Collections.emptyList();
                updateBoardView();
                statusLabel.setText("棋谱已加载: " + file.getName());
            } catch (Exception ex) {
                showError("加载失败", "无法加载棋谱: " + ex.getMessage());
            }
        }
    }

    /**
     * 处理棋盘点击事件
     */
    private void handleBoardClick(MouseEvent event) {
        if (gameEngine.isGameOver() || aiIntegration.isComputing()) {
            return;
        }

        // PVE 模式下，轮到 AI 时不响应点击
        if (aiIntegration.isEnabled() && gameEngine.getCurrentTurn() == aiIntegration.getAiColor()) {
            return;
        }

        Position clickedPos = boardView.canvasToPosition(event.getX(), event.getY());
        if (clickedPos == null) {
            return;
        }

        if (selectedFrom == null) {
            handleSelectPiece(clickedPos);
        } else {
            if (clickedPos.equals(selectedFrom)) {
                clearSelection();
            } else if (currentLegalTargets.contains(clickedPos)) {
                handleMakeMove(clickedPos);
            } else {
                Piece clickedPiece = gameEngine.getBoard().getPiece(clickedPos);
                if (clickedPiece != null && clickedPiece.getColor() == gameEngine.getCurrentTurn()) {
                    handleSelectPiece(clickedPos);
                } else {
                    clearSelection();
                }
            }
        }
    }

    /**
     * 处理选中棋子
     */
    private void handleSelectPiece(Position pos) {
        Piece piece = gameEngine.getBoard().getPiece(pos);
        if (piece == null || piece.getColor() != gameEngine.getCurrentTurn()) {
            return;
        }

        List<Position> targets = gameEngine.getLegalTargets(pos);
        if (targets.isEmpty()) {
            return;
        }

        selectedFrom = pos;
        currentLegalTargets = targets;
        boardView.setSelected(pos, targets);
    }

    /**
     * 处理走棋
     */
    private void handleMakeMove(Position to) {
        MoveResult result = gameEngine.makeMove(selectedFrom, to);
        clearSelection();

        if (result == MoveResult.INVALID) {
            statusLabel.setText("无效走法！");
        }
    }

    /**
     * 触发 AI 走棋
     */
    private void triggerAIMove() {
        if (!aiIntegration.isEnabled() || gameEngine.isGameOver()) {
            return;
        }
        if (gameEngine.getCurrentTurn() != aiIntegration.getAiColor()) {
            return;
        }

        aiIntegration.calculateMove(gameEngine.getBoard(), move -> {
            // 在 UI 线程执行 AI 走棋
            gameEngine.makeMove(move.from(), move.to());
        });
    }

    /**
     * 清除选中状态
     */
    private void clearSelection() {
        selectedFrom = null;
        currentLegalTargets = Collections.emptyList();
        boardView.clearSelection();
    }

    /**
     * 取消当前选择（供键盘快捷键调用）
     */
    public void cancelSelection() {
        if (selectedFrom != null) {
            clearSelection();
        }
    }

    /**
     * 设置棋盘翻转状态
     * @param flipped true 表示翻转
     */
    public void setFlipped(boolean flipped) {
        boardView.setFlipped(flipped);
    }

    /**
     * 获取棋盘翻转状态
     * @return true 表示已翻转
     */
    public boolean isBoardFlipped() {
        return boardView.isFlipped();
    }

    /**
     * 翻转棋盘
     */
    public void flipBoard() {
        boardView.flip();
    }

    /**
     * 更新棋盘视图
     */
    private void updateBoardView() {
        boardView.setBoard(gameEngine.getBoard());
    }

    /**
     * 显示错误对话框
     */
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // ==================== GameEventListener 回调 ====================

    @Override
    public void onMoveMade(Move move, MoveResult result) {
        Platform.runLater(() -> {
            updateBoardView();
            String colorName = gameEngine.getCurrentTurn().getName();
            switch (result) {
                case CHECK -> statusLabel.setText(colorName + " 被将军！");
                case CHECKMATE -> statusLabel.setText("将死！" + gameEngine.getGameResult().getDescription());
                case STALEMATE -> statusLabel.setText("困毙！和棋！");
                case DRAW -> statusLabel.setText("三次重复局面，和棋！");
                default -> statusLabel.setText("轮到 " + colorName);
            }

            // 如果轮到 AI，触发 AI 计算
            if (!gameEngine.isGameOver()) {
                triggerAIMove();
            }
        });
    }

    @Override
    public void onMoveUndone(Move move) {
        Platform.runLater(() -> {
            updateBoardView();
            statusLabel.setText("悔棋成功，轮到 " + gameEngine.getCurrentTurn().getName());
        });
    }

    @Override
    public void onGameOver(GameResult result) {
        Platform.runLater(() -> {
            statusLabel.setText("游戏结束：" + result.getDescription());
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("游戏结束");
            alert.setHeaderText(null);
            alert.setContentText(result.getDescription());
            alert.showAndWait();
        });
    }

    @Override
    public void onTurnChanged(Color currentTurn) {
        Platform.runLater(() -> {
            if (!gameEngine.isGameOver()) {
                statusLabel.setText("轮到 " + currentTurn.getName());
            }
        });
    }

    @Override
    public void onCheck(Color attacker) {
        Platform.runLater(() -> {
            statusLabel.setText(attacker.opposite().getName() + " 被将军！");
        });
    }

    @Override
    public void onGameStateChanged(GameState state) {
        // 状态变化已在其他回调中处理
    }
}
