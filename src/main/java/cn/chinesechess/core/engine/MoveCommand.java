package cn.chinesechess.core.engine;

import cn.chinesechess.core.*;

/**
 * 走棋命令实现
 * <p>封装走棋操作，支持撤销（悔棋）。撤销时恢复被吃掉的棋子。</p>
 */
public class MoveCommand implements GameCommand {

    /** 走棋记录 */
    private final Move move;

    /** 移动的棋子（从 from 位置取出的棋子） */
    private final Piece movedPiece;

    /**
     * 构造走棋命令
     * @param move 走棋记录
     * @param movedPiece 被移动的棋子（包含起始位置）
     */
    public MoveCommand(Move move, Piece movedPiece) {
        this.move = move;
        this.movedPiece = movedPiece;
    }

    @Override
    public void execute(IBoard board) {
        board.movePiece(move);
    }

    @Override
    public void undo(IBoard board) {
        // 将棋子移回起始位置
        board.removePiece(move.to());
        // 恢复被吃掉的棋子（如果有）
        if (move.captured() != null) {
            board.setPiece(move.captured());
        }
        // 将棋子放回起始位置
        board.setPiece(movedPiece);
    }

    @Override
    public Move getMove() {
        return move;
    }
}
