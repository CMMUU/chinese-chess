package cn.chinesechess.core.rules;

import cn.chinesechess.core.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 兵（卒）走棋规则
 * <p>
 * 未过河：只能向前走一格<br>
 * 已过河：可以向前、向左、向右各走一格，不能后退
 * </p>
 * <p>红方前进方向为 row 增大，黑方前进方向为 row 减小。</p>
 */
public class PawnMoveRule implements IMoveRule {

    @Override
    public boolean isApplicable(PieceType type) {
        return type == PieceType.PAWN;
    }

    @Override
    public List<Position> getPseudoLegalMoves(Position from, IBoard board, Color color) {
        List<Position> moves = new ArrayList<>();

        // 前进方向：红方向上（row+1），黑方向下（row-1）
        int forward = (color == Color.RED) ? 1 : -1;

        // 向前走一格（始终可走）
        addIfValid(from.offset(0, forward), board, color, moves);

        // 过河后可以左右横走
        if (from.hasCrossedRiver(color)) {
            addIfValid(from.offset(1, 0), board, color, moves);
            addIfValid(from.offset(-1, 0), board, color, moves);
        }

        return moves;
    }

    /**
     * 检查目标位置是否合法，合法则加入列表
     */
    private void addIfValid(Position target, IBoard board, Color color, List<Position> moves) {
        if (target.isValid()) {
            if (board.isEmpty(target) || !board.isOccupiedBy(target, color)) {
                moves.add(target);
            }
        }
    }
}
