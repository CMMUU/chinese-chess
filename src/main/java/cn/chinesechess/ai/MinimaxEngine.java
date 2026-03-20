package cn.chinesechess.ai;

import cn.chinesechess.core.*;
import cn.chinesechess.core.rules.CheckDetector;
import cn.chinesechess.core.rules.MoveGenerator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Alpha-Beta 剪枝搜索引擎
 * <p>使用 Minimax 算法配合 Alpha-Beta 剪枝搜索最佳走法。
 * 包含走法排序优化（优先搜索吃子走法）以提高剪枝效率。</p>
 */
public class MinimaxEngine implements IAIEngine {

    /** 当前难度等级 */
    private DifficultyLevel difficulty;

    /** 走法生成器 */
    private final MoveGenerator moveGenerator;

    /** 将军检测器 */
    private final CheckDetector checkDetector;

    /** 搜索节点计数（调试用） */
    private int nodeCount;

    public MinimaxEngine() {
        this(DifficultyLevel.MEDIUM);
    }

    public MinimaxEngine(DifficultyLevel difficulty) {
        this.difficulty = difficulty;
        this.moveGenerator = new MoveGenerator();
        this.checkDetector = new CheckDetector();
    }

    @Override
    public Move findBestMove(IBoard board, Color color) {
        nodeCount = 0;
        int depth = difficulty.getSearchDepth();
        List<Move> allMoves = moveGenerator.getLegalMoves(board, color);

        if (allMoves.isEmpty()) {
            return null;
        }

        // 走法排序：优先搜索吃子走法，提高剪枝效率
        allMoves.sort(moveComparator(board));

        Move bestMove = null;
        int bestScore = Integer.MIN_VALUE;
        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;

        for (Move move : allMoves) {
            // 模拟走棋
            IBoard boardCopy = board.copy();
            boardCopy.movePiece(move);

            // 递归搜索
            int score = -alphaBeta(boardCopy, depth - 1, -beta, -alpha, color.opposite());

            if (score > bestScore) {
                bestScore = score;
                bestMove = move;
            }

            alpha = Math.max(alpha, score);
        }

        return bestMove;
    }

    @Override
    public DifficultyLevel getDifficulty() {
        return difficulty;
    }

    @Override
    public void setDifficulty(DifficultyLevel level) {
        this.difficulty = level;
    }

    /**
     * Alpha-Beta 剪枝搜索
     * @param board 当前棋盘
     * @param depth 剩余搜索深度
     * @param alpha Alpha 值
     * @param beta Beta 值
     * @param color 当前方
     * @return 最优评分（相对于当前方）
     */
    private int alphaBeta(IBoard board, int depth, int alpha, int beta, Color color) {
        nodeCount++;

        // 到达搜索深度，返回静态评估
        if (depth <= 0) {
            return quiescenceSearch(board, alpha, beta, color, 4);
        }

        List<Move> moves = moveGenerator.getLegalMoves(board, color);

        // 无合法走法：将死或困毙
        if (moves.isEmpty()) {
            if (checkDetector.isInCheck(board, color)) {
                // 被将死，返回极大负分（越浅越好）
                return Integer.MIN_VALUE + (difficulty.getSearchDepth() - depth);
            } else {
                // 困毙，和棋
                return 0;
            }
        }

        // 走法排序
        moves.sort(moveComparator(board));

        int maxScore = Integer.MIN_VALUE;

        for (Move move : moves) {
            IBoard boardCopy = board.copy();
            boardCopy.movePiece(move);

            int score = -alphaBeta(boardCopy, depth - 1, -beta, -alpha, color.opposite());

            maxScore = Math.max(maxScore, score);
            alpha = Math.max(alpha, score);

            // Beta 剪枝
            if (alpha >= beta) {
                break;
            }
        }

        return maxScore;
    }

    /**
     * 静态搜索（Quiescence Search）
     * <p>在搜索深度耗尽后，继续搜索吃子走法以避免"水平线效应"。</p>
     */
    private int quiescenceSearch(IBoard board, int alpha, int beta, Color color, int qDepth) {
        nodeCount++;

        int standPat = BoardEvaluator.evaluate(board, color);

        if (qDepth <= 0) {
            return standPat;
        }

        if (standPat >= beta) {
            return beta;
        }

        if (standPat > alpha) {
            alpha = standPat;
        }

        // 只搜索吃子走法
        List<Move> moves = moveGenerator.getLegalMoves(board, color);
        List<Move> captureMoves = moves.stream()
                .filter(m -> m.captured() != null)
                .toList();

        for (Move move : captureMoves) {
            IBoard boardCopy = board.copy();
            boardCopy.movePiece(move);

            int score = -quiescenceSearch(boardCopy, -beta, -alpha, color.opposite(), qDepth - 1);

            if (score >= beta) {
                return beta;
            }
            if (score > alpha) {
                alpha = score;
            }
        }

        return alpha;
    }

    /**
     * 走法排序比较器
     * <p>优先搜索吃子走法（吃高价值棋子优先）和将军走法。</p>
     */
    private Comparator<Move> moveComparator(IBoard board) {
        return (m1, m2) -> {
            int score1 = moveScore(m1, board);
            int score2 = moveScore(m2, board);
            return Integer.compare(score2, score1); // 降序
        };
    }

    /**
     * 计算走法的启发式评分（用于排序）
     */
    private int moveScore(Move move, IBoard board) {
        int score = 0;

        // 吃子走法加分
        if (move.captured() != null) {
            score += PieceValueTable.getBaseValue(move.captured().getType());
        }

        // 中心位置走法加分
        Position to = move.to();
        int centerDist = Math.abs(to.col() - 4) + Math.abs(to.row() - 5);
        score += (14 - centerDist);

        return score;
    }
}
