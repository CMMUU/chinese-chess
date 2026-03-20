package cn.chinesechess.core.rules;

import cn.chinesechess.core.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 合法走法生成器
 * <p>生成所有合法走法（已过滤导致己方被将军的走法），并处理飞将规则。</p>
 */
public class MoveGenerator {

    private final CheckDetector checkDetector;

    public MoveGenerator() {
        this.checkDetector = new CheckDetector();
    }

    /**
     * 获取指定颜色方的所有合法走法
     * @param board 棋盘
     * @param color 当前方
     * @return 所有合法走法列表
     */
    public List<Move> getLegalMoves(IBoard board, Color color) {
        List<Move> legalMoves = new ArrayList<>();
        List<Piece> pieces = board.getPieces(color);

        for (Piece piece : pieces) {
            Position from = piece.getPosition();
            IMoveRule rule = MoveRuleFactory.getRule(piece.getType());
            List<Position> pseudoMoves = rule.getPseudoLegalMoves(from, board, color);

            for (Position to : pseudoMoves) {
                // 模拟走棋，检查是否会导致己方被将军
                IBoard boardCopy = board.copy();
                Piece movedPiece = piece.moveTo(to);
                boardCopy.removePiece(from);
                boardCopy.setPiece(movedPiece);

                // 如果走棋后己方不被将军，则为合法走法
                if (!checkDetector.isInCheck(boardCopy, color)) {
                    Piece captured = board.getPiece(to);
                    legalMoves.add(new Move(from, to, captured));
                }
            }
        }

        // 处理飞将规则：将帅不能在同一直线上面对面且中间无子
        addFlyingGeneralMoves(legalMoves, board, color);

        return legalMoves;
    }

    /**
     * 检查指定棋子是否存在合法走法
     * @param board 棋盘
     * @param piece 棋子
     * @return 是否有合法走法
     */
    public boolean hasLegalMove(IBoard board, Piece piece) {
        Color color = piece.getColor();
        Position from = piece.getPosition();
        IMoveRule rule = MoveRuleFactory.getRule(piece.getType());
        List<Position> pseudoMoves = rule.getPseudoLegalMoves(from, board, color);

        for (Position to : pseudoMoves) {
            IBoard boardCopy = board.copy();
            Piece movedPiece = piece.moveTo(to);
            boardCopy.removePiece(from);
            boardCopy.setPiece(movedPiece);

            if (!checkDetector.isInCheck(boardCopy, color)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 处理飞将规则
     * <p>如果将帅在同一列且中间无棋子，则双方将帅都可以"飞将"到对方位置。</p>
     */
    private void addFlyingGeneralMoves(List<Move> legalMoves, IBoard board, Color color) {
        // 找到己方将/帅
        Piece myKing = findKing(board, color);
        if (myKing == null) {
            return;
        }
        // 找到对方将/帅
        Piece opponentKing = findKing(board, color.opposite());
        if (opponentKing == null) {
            return;
        }

        Position myPos = myKing.getPosition();
        Position oppPos = opponentKing.getPosition();

        // 将帅在同一列
        if (myPos.col() == oppPos.col()) {
            // 检查中间是否有棋子
            int minRow = Math.min(myPos.row(), oppPos.row());
            int maxRow = Math.max(myPos.row(), oppPos.row());
            boolean blocked = false;
            for (int row = minRow + 1; row < maxRow; row++) {
                if (board.isOccupied(new Position(myPos.col(), row))) {
                    blocked = true;
                    break;
                }
            }
            if (!blocked) {
                // 己方将帅可以飞将到对方将帅位置（吃掉对方将帅）
                // 但需要检查飞将后是否导致己方被将军
                Position target = oppPos;
                IBoard boardCopy = board.copy();
                Piece movedPiece = myKing.moveTo(target);
                boardCopy.removePiece(myPos);
                boardCopy.setPiece(movedPiece);

                if (!checkDetector.isInCheck(boardCopy, color)) {
                    Piece captured = board.getPiece(target);
                    Move flyingMove = new Move(myPos, target, captured);
                    // 避免重复添加
                    if (legalMoves.stream().noneMatch(m -> m.from().equals(myPos) && m.to().equals(target))) {
                        legalMoves.add(flyingMove);
                    }
                }
            }
        }
    }

    /**
     * 在棋盘上找到指定颜色的将/帅
     */
    private Piece findKing(IBoard board, Color color) {
        for (Piece piece : board.getPieces(color)) {
            if (piece.getType() == PieceType.KING) {
                return piece;
            }
        }
        return null;
    }
}
