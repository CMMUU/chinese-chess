package cn.chinesechess.core.rules;

import cn.chinesechess.core.*;

import java.util.List;

/**
 * 将军/将死/困毙检测器
 * <p>提供检测将军、将死、困毙状态的方法。</p>
 */
public class CheckDetector {

    /**
     * 检查指定颜色方是否被将军
     * @param board 棋盘
     * @param color 被检测方
     * @return 是否被将军
     */
    public boolean isInCheck(IBoard board, Color color) {
        // 找到己方将/帅位置
        Position kingPos = findKingPosition(board, color);
        if (kingPos == null) {
            return false; // 没有将帅，不认为被将军
        }

        // 检查对方所有棋子是否能攻击到将帅位置
        Color opponent = color.opposite();
        for (Piece piece : board.getPieces(opponent)) {
            IMoveRule rule = MoveRuleFactory.getRule(piece.getType());
            List<Position> moves = rule.getPseudoLegalMoves(piece.getPosition(), board, opponent);
            if (moves.contains(kingPos)) {
                return true;
            }
        }

        // 检查飞将：对方将帅与己方将帅面对面
        Position opponentKingPos = findKingPosition(board, opponent);
        if (opponentKingPos != null && kingPos.col() == opponentKingPos.col()) {
            int minRow = Math.min(kingPos.row(), opponentKingPos.row());
            int maxRow = Math.max(kingPos.row(), opponentKingPos.row());
            boolean blocked = false;
            for (int row = minRow + 1; row < maxRow; row++) {
                if (board.isOccupied(new Position(kingPos.col(), row))) {
                    blocked = true;
                    break;
                }
            }
            if (!blocked) {
                return true; // 飞将也是将军
            }
        }

        return false;
    }

    /**
     * 检查指定颜色方是否被将死
     * <p>将死 = 被将军 + 没有合法走法可以解除将军</p>
     * @param board 棋盘
     * @param color 被检测方
     * @return 是否被将死
     */
    public boolean isCheckmate(IBoard board, Color color) {
        return isInCheck(board, color) && !hasAnyLegalMove(board, color);
    }

    /**
     * 检查是否困毙（未被将军但无子可走）
     * <p>困毙 = 未被将军 + 没有任何合法走法</p>
     * @param board 棋盘
     * @param color 被检测方
     * @return 是否困毙
     */
    public boolean isStalemate(IBoard board, Color color) {
        return !isInCheck(board, color) && !hasAnyLegalMove(board, color);
    }

    /**
     * 检查指定颜色方是否存在至少一个合法走法
     */
    private boolean hasAnyLegalMove(IBoard board, Color color) {
        MoveGenerator moveGenerator = new MoveGenerator();
        for (Piece piece : board.getPieces(color)) {
            if (moveGenerator.hasLegalMove(board, piece)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 在棋盘上找到指定颜色的将/帅位置
     */
    private Position findKingPosition(IBoard board, Color color) {
        for (Piece piece : board.getPieces(color)) {
            if (piece.getType() == PieceType.KING) {
                return piece.getPosition();
            }
        }
        return null;
    }
}
