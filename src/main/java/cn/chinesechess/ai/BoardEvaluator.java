package cn.chinesechess.ai;

import cn.chinesechess.core.*;

/**
 * 棋盘局面评估函数
 * <p>对当前棋盘局面进行评分，正值表示红方优势，负值表示黑方优势。
 * 评估维度：棋子价值、位置加分、棋子活性。</p>
 */
public class BoardEvaluator {

    /** 物资分权重 */
    private static final int MATERIAL_WEIGHT = 10;

    /** 位置分权重 */
    private static final int POSITION_WEIGHT = 1;

    private BoardEvaluator() {
        // 工具类，禁止实例化
    }

    /**
     * 评估当前局面
     * @param board 棋盘
     * @param color 当前评估方（用于搜索优化）
     * @return 评分（正值对当前方有利）
     */
    public static int evaluate(IBoard board, Color color) {
        int score = 0;

        for (Piece piece : board.getAllPieces()) {
            int pieceScore = evaluatePiece(piece);
            // 红方正分，黑方负分
            if (piece.getColor() == Color.RED) {
                score += pieceScore;
            } else {
                score -= pieceScore;
            }
        }

        // 返回相对于当前评估方的分数
        return (color == Color.RED) ? score : -score;
    }

    /**
     * 评估单个棋子的价值
     * @param piece 棋子
     * @return 棋子价值（正值）
     */
    private static int evaluatePiece(Piece piece) {
        int baseValue = PieceValueTable.getBaseValue(piece.getType());
        int positionBonus = PieceValueTable.getPositionBonus(
                piece.getType(), piece.getPosition(), piece.getColor()
        );
        return baseValue * MATERIAL_WEIGHT + positionBonus * POSITION_WEIGHT;
    }
}
