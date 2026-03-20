package cn.chinesechess.core.rules;

import cn.chinesechess.core.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 相（象）走棋规则
 * <p>相象走"田"字对角线，不能过河，且需检查塞象眼（田字中心是否有子）。</p>
 */
public class ElephantMoveRule implements IMoveRule {

    /** 四个田字方向：右上、右下、左上、左下 */
    private static final int[][] DIRECTIONS = {{2, 2}, {2, -2}, {-2, 2}, {-2, -2}};

    @Override
    public boolean isApplicable(PieceType type) {
        return type == PieceType.ELEPHANT;
    }

    @Override
    public List<Position> getPseudoLegalMoves(Position from, IBoard board, Color color) {
        List<Position> moves = new ArrayList<>();

        for (int[] dir : DIRECTIONS) {
            Position target = from.offset(dir[0], dir[1]);
            // 目标位置必须有效且不能过河
            if (target.isValid() && !target.hasCrossedRiver(color)) {
                // 检查塞象眼：田字中心位置是否有棋子
                Position eyePos = from.offset(dir[0] / 2, dir[1] / 2);
                if (board.isEmpty(eyePos)) {
                    // 目标位置为空或有对方棋子
                    if (board.isEmpty(target) || !board.isOccupiedBy(target, color)) {
                        moves.add(target);
                    }
                }
            }
        }

        return moves;
    }
}
