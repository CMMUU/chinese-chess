package cn.chinesechess.core.engine;

import cn.chinesechess.core.*;
import cn.chinesechess.core.rules.CheckDetector;
import cn.chinesechess.core.rules.MoveGenerator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 游戏引擎实现
 * <p>管理游戏核心逻辑，包括走棋、悔棋、认输、求和等操作，
 * 通过观察者模式通知 UI 层游戏状态变化。</p>
 */
public class GameEngine implements IGameEngine {

    /** 棋盘 */
    private IBoard board;

    /** 当前回合方 */
    private Color currentTurn;

    /** 游戏结果 */
    private GameResult gameResult;

    /** 走棋历史（命令栈，用于悔棋） */
    private final List<GameCommand> moveHistory;

    /** 走棋记录（对应中文记谱） */
    private final List<Move> moveRecords;

    /** 合法走法生成器 */
    private final MoveGenerator moveGenerator;

    /** 将军检测器 */
    private final CheckDetector checkDetector;

    /** 游戏事件监听器列表 */
    private final List<GameEventListener> listeners;

    /** 游戏模式 */
    private GameMode gameMode;

    /** 先手方 */
    private Color firstHand;

    /** 是否有待处理的和棋请求 */
    private boolean drawOffered;

    /** 求和方 */
    private Color drawOfferBy;

    /** 局面历史记录（局面指纹 -> 出现次数，用于三次重复局面判和） */
    private final Map<String, Integer> positionHistory;

    public GameEngine() {
        this.moveGenerator = new MoveGenerator();
        this.checkDetector = new CheckDetector();
        this.moveHistory = new ArrayList<>();
        this.moveRecords = new ArrayList<>();
        this.positionHistory = new HashMap<>();
        this.listeners = new CopyOnWriteArrayList<>();
        this.gameMode = GameMode.PVP;
        this.firstHand = Color.RED;
        this.gameResult = GameResult.IN_PROGRESS;
        this.currentTurn = Color.RED;
        newGame();
    }

    @Override
    public void newGame() {
        this.board = BoardInitializer.createInitialBoard();
        this.currentTurn = firstHand;
        this.gameResult = GameResult.IN_PROGRESS;
        this.moveHistory.clear();
        this.moveRecords.clear();
        this.positionHistory.clear();
        this.drawOffered = false;
        this.drawOfferBy = null;

        // 记录初始局面
        String initKey = generatePositionKey();
        positionHistory.put(initKey, 1);

        notifyTurnChanged();
        notifyStateChanged();
    }

    @Override
    public MoveResult makeMove(Position from, Position to) {
        // 检查游戏是否已结束
        if (isGameOver()) {
            return MoveResult.GAME_OVER;
        }

        // 获取起始位置的棋子
        Piece piece = board.getPiece(from);
        if (piece == null) {
            return MoveResult.INVALID;
        }

        // 检查是否轮到该方走棋
        if (piece.getColor() != currentTurn) {
            return MoveResult.INVALID;
        }

        // 获取该棋子的合法目标位置
        List<Position> legalTargets = getLegalTargets(from);
        if (!legalTargets.contains(to)) {
            return MoveResult.INVALID;
        }

        // 构造走棋记录
        Piece captured = board.getPiece(to);
        Move move = new Move(from, to, captured);

        // 执行走棋
        MoveCommand command = new MoveCommand(move, piece);
        command.execute(board);
        moveHistory.add(command);
        moveRecords.add(move);

        // 清除和棋请求状态
        drawOffered = false;
        drawOfferBy = null;

        // 获取中文记谱并通知
        String notation = move.toChineseNotation(piece);

        // 检查走棋后的状态
        Color nextTurn = currentTurn.opposite();
        MoveResult result;

        // 检查三次重复局面
        String posKey = generatePositionKeyWithTurn(nextTurn);
        int count = positionHistory.getOrDefault(posKey, 0) + 1;
        positionHistory.put(posKey, count);

        if (count >= 3) {
            // 三次重复局面，判和
            result = MoveResult.DRAW;
            gameResult = GameResult.DRAW;
        } else if (checkDetector.isCheckmate(board, nextTurn)) {
            // 将死
            result = MoveResult.CHECKMATE;
            gameResult = (nextTurn == Color.RED) ? GameResult.BLACK_WIN : GameResult.RED_WIN;
        } else if (checkDetector.isStalemate(board, nextTurn)) {
            // 困毙
            result = MoveResult.STALEMATE;
            gameResult = GameResult.DRAW;
        } else if (checkDetector.isInCheck(board, nextTurn)) {
            // 将军
            result = MoveResult.CHECK;
            notifyCheck(currentTurn);
        } else {
            result = MoveResult.SUCCESS;
        }

        // 切换回合
        currentTurn = nextTurn;

        // 通知监听器
        notifyMoveMade(move, result);
        notifyTurnChanged();
        notifyStateChanged();

        // 游戏结束通知
        if (gameResult != GameResult.IN_PROGRESS) {
            notifyGameOver();
        }

        return result;
    }

    @Override
    public boolean undoMove() {
        if (moveHistory.isEmpty()) {
            return false;
        }

        // 如果游戏已结束，悔棋后重新开始
        if (isGameOver()) {
            gameResult = GameResult.IN_PROGRESS;
        }

        // 撤销最后一步前，先减少当前局面的出现次数
        String currentPosKey = generatePositionKeyWithTurn(currentTurn);
        int count = positionHistory.getOrDefault(currentPosKey, 0);
        if (count > 1) {
            positionHistory.put(currentPosKey, count - 1);
        } else {
            positionHistory.remove(currentPosKey);
        }

        // 撤销最后一步
        GameCommand lastCommand = moveHistory.remove(moveHistory.size() - 1);
        Move undoneMove = moveRecords.remove(moveRecords.size() - 1);
        lastCommand.undo(board);

        // 切换回合
        currentTurn = currentTurn.opposite();

        // 通知监听器
        notifyMoveUndone(undoneMove);
        notifyTurnChanged();
        notifyStateChanged();

        return true;
    }

    @Override
    public void resign(Color color) {
        if (isGameOver()) {
            return;
        }

        gameResult = (color == Color.RED) ? GameResult.BLACK_WIN : GameResult.RED_WIN;
        notifyGameOver();
        notifyStateChanged();
    }

    @Override
    public boolean offerDraw(Color color) {
        if (isGameOver()) {
            return false;
        }

        drawOffered = true;
        drawOfferBy = color;
        return true;
    }

    @Override
    public void acceptDraw() {
        if (!drawOffered) {
            return;
        }

        gameResult = GameResult.DRAW;
        drawOffered = false;
        drawOfferBy = null;
        notifyGameOver();
        notifyStateChanged();
    }

    @Override
    public Color getCurrentTurn() {
        return currentTurn;
    }

    @Override
    public boolean isGameOver() {
        return gameResult != GameResult.IN_PROGRESS;
    }

    @Override
    public GameResult getGameResult() {
        return gameResult;
    }

    @Override
    public IBoard getBoard() {
        return board;
    }

    @Override
    public List<Move> getMoveHistory() {
        return Collections.unmodifiableList(moveRecords);
    }

    @Override
    public List<Position> getLegalTargets(Position from) {
        Piece piece = board.getPiece(from);
        if (piece == null || piece.getColor() != currentTurn) {
            return Collections.emptyList();
        }
        return moveGenerator.getLegalMoves(board, currentTurn).stream()
                .filter(move -> move.from().equals(from))
                .map(Move::to)
                .toList();
    }

    @Override
    public GameState getState() {
        return new GameState(
                board.getAllPieces(),
                currentTurn,
                gameResult,
                Collections.unmodifiableList(moveRecords),
                moveRecords.size()
        );
    }

    @Override
    public void addEventListener(GameEventListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    @Override
    public void removeEventListener(GameEventListener listener) {
        listeners.remove(listener);
    }

    @Override
    public GameMode getGameMode() {
        return gameMode;
    }

    @Override
    public void setGameMode(GameMode mode) {
        this.gameMode = mode;
    }

    @Override
    public void setFirstHand(Color firstHand) {
        this.firstHand = firstHand;
    }

    /**
     * 获取是否有待处理的和棋请求
     * @return 是否有和棋请求
     */
    public boolean isDrawOffered() {
        return drawOffered;
    }

    /**
     * 获取求和方
     * @return 求和方
     */
    public Color getDrawOfferBy() {
        return drawOfferBy;
    }

    // ==================== 局面指纹方法 ====================

    /**
     * 生成当前局面的指纹字符串（包含当前回合方）
     * <p>局面指纹由所有棋子的类型、颜色、位置排序拼接，
     * 加上当前回合方组成，用于判断三次重复局面。</p>
     * @return 局面指纹字符串
     */
    private String generatePositionKeyWithTurn(Color turn) {
        StringBuilder sb = new StringBuilder();
        // 按位置排序，保证同一局面生成相同的指纹
        board.getAllPieces().stream()
                .sorted((a, b) -> {
                    int cmp = Integer.compare(a.getPosition().col(), b.getPosition().col());
                    if (cmp != 0) return cmp;
                    cmp = Integer.compare(a.getPosition().row(), b.getPosition().row());
                    if (cmp != 0) return cmp;
                    cmp = a.getType().compareTo(b.getType());
                    if (cmp != 0) return cmp;
                    return a.getColor().compareTo(b.getColor());
                })
                .forEach(p -> sb.append(p.getType().name())
                        .append(p.getColor().name())
                        .append(p.getPosition().col())
                        .append(p.getPosition().row())
                        .append(';'));
        sb.append('|').append(turn.name());
        return sb.toString();
    }

    /**
     * 生成当前局面的指纹字符串
     * @return 局面指纹字符串
     */
    private String generatePositionKey() {
        return generatePositionKeyWithTurn(currentTurn);
    }

    // ==================== 事件通知方法 ====================

    private void notifyMoveMade(Move move, MoveResult result) {
        for (GameEventListener listener : listeners) {
            listener.onMoveMade(move, result);
        }
    }

    private void notifyMoveUndone(Move move) {
        for (GameEventListener listener : listeners) {
            listener.onMoveUndone(move);
        }
    }

    private void notifyGameOver() {
        for (GameEventListener listener : listeners) {
            listener.onGameOver(gameResult);
        }
    }

    private void notifyTurnChanged() {
        for (GameEventListener listener : listeners) {
            listener.onTurnChanged(currentTurn);
        }
    }

    private void notifyCheck(Color attacker) {
        for (GameEventListener listener : listeners) {
            listener.onCheck(attacker);
        }
    }

    private void notifyStateChanged() {
        GameState state = getState();
        for (GameEventListener listener : listeners) {
            listener.onGameStateChanged(state);
        }
    }
}
