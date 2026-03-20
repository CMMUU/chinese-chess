package cn.chinesechess.core.rules;

import cn.chinesechess.core.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 仕（士）走棋规则
 * <p>仕士只能在九宫内沿斜线走一格，可吃对方棋子。</p>
 */
public class AdvisorMoveRule implements IMoveRule {

    /** 可能的移动方向：四个斜向 */
    private static final int[][] DIRECTIONS = {{1, 1}, {1, -1}, {-1, 1}, {-1, -1}};

    @Override
    public boolean isApplicable(PieceType type) {
        return type == PieceType.ADVISOR;
    }

    @Override
    public List<Position> getPseudoLegalMoves(Position from, IBoard board, Color color) {
        List<Position> moves = new ArrayList<>();

        for (int[] dir : DIRECTIONS) {
            Position target = from.offset(dir[0], dir[1]);
            // 目标位置必须在九宫内且有效
            if (target.isValid() && target.isInPalace(color)) {
                // 目标位置为空或有对方棋子
                if (board.isEmpty(target) || !board.isOccupiedBy(target, color)) {
                    moves.add(target);
                }
            }
        }

        return moves;
    }
}
