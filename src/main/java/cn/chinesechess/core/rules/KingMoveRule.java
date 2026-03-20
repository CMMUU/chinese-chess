package cn.chinesechess.core.rules;

import cn.chinesechess.core.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 将帅走棋规则
 * <p>将帅只能在九宫内沿直线走一格。
 * 特殊规则：将帅不能在同一直线上对面（飞将规则），此规则由 CheckDetector 处理。</p>
 */
public class KingMoveRule implements IMoveRule {

    /** 可能的移动方向：上下左右 */
    private static final int[][] DIRECTIONS = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}};

    @Override
    public boolean isApplicable(PieceType type) {
        return type == PieceType.KING;
    }

    @Override
    public List<Position> getPseudoLegalMoves(Position from, IBoard board, Color color) {
        List<Position> moves = new ArrayList<>();

        for (int[] dir : DIRECTIONS) {
            Position target = from.offset(dir[0], dir[1]);
            // 目标位置必须在九宫内且有效
            if (target.isValid() && target.isInPalace(color)) {
                // 目标位置为空或有对方棋子（不能吃己方棋子）
                if (board.isEmpty(target) || !board.isOccupiedBy(target, color)) {
                    moves.add(target);
                }
            }
        }

        return moves;
    }
}
