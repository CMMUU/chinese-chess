package cn.chinesechess.ai;

import cn.chinesechess.core.Color;
import cn.chinesechess.core.IBoard;
import cn.chinesechess.core.Move;
import cn.chinesechess.core.Position;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 内置象棋引擎适配器
 * <p>将 MinimaxEngine 封装为 ChessEngine 接口。</p>
 */
public class BuiltInEngine implements ChessEngine {

    private final String name;
    private final String version;
    private final String description;
    private final IAIEngine aiEngine;

    private int depth = 3;
    private Color color = Color.RED;
    private IBoard board;
    private final AtomicBoolean thinking = new AtomicBoolean(false);
    private final AtomicReference<String> bestMove = new AtomicReference<>();

    public BuiltInEngine(String name, String version, IAIEngine aiEngine) {
        this.name = name;
        this.version = version;
        this.description = "内置象棋引擎 - 基于 Alpha-Beta 剪枝算法";
        this.aiEngine = aiEngine;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public EngineType getType() {
        return EngineType.BUILTIN;
    }

    @Override
    public Path getPath() {
        return null;
    }

    @Override
    public void initialize() {
        // 内置引擎无需初始化
    }

    @Override
    public void shutdown() {
        stopThinking();
    }

    @Override
    public void setDepth(int depth) {
        this.depth = depth;
        // 根据深度选择合适的难度等级
        DifficultyLevel level;
        if (depth <= 2) {
            level = DifficultyLevel.EASY;
        } else if (depth <= 4) {
            level = DifficultyLevel.MEDIUM;
        } else {
            level = DifficultyLevel.HARD;
        }
        aiEngine.setDifficulty(level);
    }

    @Override
    public void setPosition(Color color) {
        this.color = color;
    }

    @Override
    public void setFen(String fen) {
        // TODO: 实现 FEN 解析
    }

    @Override
    public void setMoves(List<String> moves) {
        // TODO: 实现走棋历史设置
    }

    @Override
    public void startThinking() {
        if (thinking.get()) {
            return;
        }
        thinking.set(true);

        Thread thread = new Thread(() -> {
            try {
                // 创建临时棋盘
                IBoard workBoard = board;
                if (workBoard == null) {
                    return;
                }

                Move move = aiEngine.findBestMove(workBoard, color);
                if (move != null) {
                    bestMove.set(moveToUci(move));
                }
            } finally {
                thinking.set(false);
            }
        }, "BuiltInEngine");
        thread.start();
    }

    @Override
    public void stopThinking() {
        thinking.set(false);
    }

    @Override
    public String getBestMove() {
        return bestMove.get();
    }

    @Override
    public boolean isThinking() {
        return thinking.get();
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    /**
     * 设置棋盘
     */
    public void setBoard(IBoard board) {
        this.board = board;
    }

    /**
     * 将 Move 转换为 UCI 格式
     */
    private String moveToUci(Move move) {
        Position from = move.from();
        Position to = move.to();
        StringBuilder sb = new StringBuilder();
        sb.append(colToChar(from.col())).append(rowToChar(from.row()));
        sb.append(colToChar(to.col())).append(rowToChar(to.row()));
        return sb.toString();
    }

    private char colToChar(int col) {
        return (char) ('a' + col);
    }

    private char rowToChar(int row) {
        return (char) ('0' + (9 - row));
    }
}
