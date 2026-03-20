package cn.chinesechess.core.rules;

import cn.chinesechess.core.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 马走棋规则
 * <p>马走"日"字（先直一格再斜一格），需检查蹩马腿（直行方向第一格是否有子）。</p>
 */
public class KnightMoveRule implements IMoveRule {

    /**
     * 马的8个移动方向，每个方向包含：
     * {直行方向dCol, 直行方向dRow, 目标偏移dCol, 目标偏移dRow}
     * 直行方向即蹩马腿的位置
     */
    private static final int[][][] MOVES = {
            // 上方向的左右两个日字
            { {0, 1}, {-1, 2} },  // 上左
            { {0, 1}, {1, 2} },   // 上右
            // 下方向的左右两个日字
            { {0, -1}, {-1, -2} }, // 下左
            { {0, -1}, {1, -2} },  // 下右
            // 左方向的上下两个日字
            { {-1, 0}, {-2, 1} },  // 左上
            { {-1, 0}, {-2, -1} }, // 左下
            // 右方向的上下两个日字
            { {1, 0}, {2, 1} },   // 右上
            { {1, 0}, {2, -1} }   // 右下
    };

    @Override
    public boolean isApplicable(PieceType type) {
        return type == PieceType.KNIGHT;
    }

    @Override
    public List<Position> getPseudoLegalMoves(Position from, IBoard board, Color color) {
        List<Position> moves = new ArrayList<>();

        for (int[][] move : MOVES) {
            // 蹩马腿位置（直行方向第一格）
            Position legPos = from.offset(move[0][0], move[0][1]);
            // 目标位置
            Position target = from.offset(move[1][0], move[1][1]);

            // 目标位置必须有效
            if (!target.isValid()) {
                continue;
            }
            // 检查蹩马腿：直行方向第一格不能有棋子
            if (board.isOccupied(legPos)) {
                continue;
            }
            // 目标位置为空或有对方棋子
            if (board.isEmpty(target) || !board.isOccupiedBy(target, color)) {
                moves.add(target);
            }
        }

        return moves;
    }
}
