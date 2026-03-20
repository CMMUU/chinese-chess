package cn.chinesechess.core.rules;

import cn.chinesechess.core.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 车走棋规则
 * <p>车沿直线（横竖）走任意格数，中间不能有棋子阻挡，可吃对方棋子。</p>
 */
public class RookMoveRule implements IMoveRule {

    /** 四个直线方向：上下左右 */
    private static final int[][] DIRECTIONS = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}};

    @Override
    public boolean isApplicable(PieceType type) {
        return type == PieceType.ROOK;
    }

    @Override
    public List<Position> getPseudoLegalMoves(Position from, IBoard board, Color color) {
        List<Position> moves = new ArrayList<>();

        for (int[] dir : DIRECTIONS) {
            // 沿方向逐格搜索
            for (int step = 1; step <= 8; step++) {
                Position target = from.offset(dir[0] * step, dir[1] * step);
                if (!target.isValid()) {
                    break; // 超出棋盘边界
                }
                if (board.isEmpty(target)) {
                    // 空位，可以走
                    moves.add(target);
                } else if (board.isOccupiedBy(target, color)) {
                    // 己方棋子阻挡，停止该方向搜索
                    break;
                } else {
                    // 对方棋子，可以吃，然后停止
                    moves.add(target);
                    break;
                }
            }
        }

        return moves;
    }
}
