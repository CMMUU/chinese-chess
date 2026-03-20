package cn.chinesechess.core.rules;

import cn.chinesechess.core.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 炮走棋规则
 * <p>炮移动时与车相同（沿直线走任意格数，不能越子）。
 * 吃子时必须隔一个棋子（炮架/翻山）才能吃掉目标棋子。</p>
 */
public class CannonMoveRule implements IMoveRule {

    /** 四个直线方向：上下左右 */
    private static final int[][] DIRECTIONS = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}};

    @Override
    public boolean isApplicable(PieceType type) {
        return type == PieceType.CANNON;
    }

    @Override
    public List<Position> getPseudoLegalMoves(Position from, IBoard board, Color color) {
        List<Position> moves = new ArrayList<>();

        for (int[] dir : DIRECTIONS) {
            boolean foundScreen = false; // 是否已找到炮架

            for (int step = 1; step <= 8; step++) {
                Position target = from.offset(dir[0] * step, dir[1] * step);
                if (!target.isValid()) {
                    break; // 超出棋盘边界
                }

                if (!foundScreen) {
                    // 尚未找到炮架
                    if (board.isEmpty(target)) {
                        // 空位，可以走（移动）
                        moves.add(target);
                    } else {
                        // 找到炮架（任意颜色的棋子都可以当炮架）
                        foundScreen = true;
                    }
                } else {
                    // 已找到炮架，寻找可吃的目标
                    if (!board.isEmpty(target)) {
                        // 找到炮架后的第一个棋子
                        if (!board.isOccupiedBy(target, color)) {
                            // 是对方棋子，可以吃
                            moves.add(target);
                        }
                        // 无论是否己方棋子，都不能再继续（翻山后不能再翻）
                        break;
                    }
                    // 炮架后的空位，继续搜索
                }
            }
        }

        return moves;
    }
}
