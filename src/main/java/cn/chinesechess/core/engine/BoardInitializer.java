package cn.chinesechess.core.engine;

import cn.chinesechess.core.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 棋盘初始化器
 * <p>创建标准中国象棋开局布局。</p>
 * <p>
 * 棋盘坐标系（红方视角，row从下往上增大）：<br>
 * - 红方：row 0-4（下方）<br>
 * - 黑方：row 5-9（上方）<br>
 * - 列：col 0-8（从左到右）
 * </p>
 */
public class BoardInitializer {

    private BoardInitializer() {
        // 工具类，禁止实例化
    }

    /**
     * 创建标准开局棋盘
     * @return 包含所有棋子的棋盘
     */
    public static Board createInitialBoard() {
        List<Piece> pieces = new ArrayList<>();

        // ==================== 红方（下方，row 0-4） ====================

        // 红方底线 (row 0)
        pieces.add(new Piece(PieceType.ROOK, Color.RED, new Position(0, 0)));     // 车
        pieces.add(new Piece(PieceType.KNIGHT, Color.RED, new Position(1, 0)));   // 马
        pieces.add(new Piece(PieceType.ELEPHANT, Color.RED, new Position(2, 0))); // 相
        pieces.add(new Piece(PieceType.ADVISOR, Color.RED, new Position(3, 0)));  // 仕
        pieces.add(new Piece(PieceType.KING, Color.RED, new Position(4, 0)));     // 帅
        pieces.add(new Piece(PieceType.ADVISOR, Color.RED, new Position(5, 0)));  // 仕
        pieces.add(new Piece(PieceType.ELEPHANT, Color.RED, new Position(6, 0))); // 相
        pieces.add(new Piece(PieceType.KNIGHT, Color.RED, new Position(7, 0)));   // 马
        pieces.add(new Piece(PieceType.ROOK, Color.RED, new Position(8, 0)));     // 车

        // 红方炮位 (row 2)
        pieces.add(new Piece(PieceType.CANNON, Color.RED, new Position(1, 2)));   // 炮
        pieces.add(new Piece(PieceType.CANNON, Color.RED, new Position(7, 2)));   // 炮

        // 红方兵位 (row 3)
        pieces.add(new Piece(PieceType.PAWN, Color.RED, new Position(0, 3)));     // 兵
        pieces.add(new Piece(PieceType.PAWN, Color.RED, new Position(2, 3)));     // 兵
        pieces.add(new Piece(PieceType.PAWN, Color.RED, new Position(4, 3)));     // 兵
        pieces.add(new Piece(PieceType.PAWN, Color.RED, new Position(6, 3)));     // 兵
        pieces.add(new Piece(PieceType.PAWN, Color.RED, new Position(8, 3)));     // 兵

        // ==================== 黑方（上方，row 5-9） ====================

        // 黑方底线 (row 9)
        pieces.add(new Piece(PieceType.ROOK, Color.BLACK, new Position(0, 9)));     // 車
        pieces.add(new Piece(PieceType.KNIGHT, Color.BLACK, new Position(1, 9)));   // 馬
        pieces.add(new Piece(PieceType.ELEPHANT, Color.BLACK, new Position(2, 9))); // 象
        pieces.add(new Piece(PieceType.ADVISOR, Color.BLACK, new Position(3, 9)));  // 士
        pieces.add(new Piece(PieceType.KING, Color.BLACK, new Position(4, 9)));     // 将
        pieces.add(new Piece(PieceType.ADVISOR, Color.BLACK, new Position(5, 9)));  // 士
        pieces.add(new Piece(PieceType.ELEPHANT, Color.BLACK, new Position(6, 9))); // 象
        pieces.add(new Piece(PieceType.KNIGHT, Color.BLACK, new Position(7, 9)));   // 馬
        pieces.add(new Piece(PieceType.ROOK, Color.BLACK, new Position(8, 9)));     // 車

        // 黑方炮位 (row 7)
        pieces.add(new Piece(PieceType.CANNON, Color.BLACK, new Position(1, 7)));   // 砲
        pieces.add(new Piece(PieceType.CANNON, Color.BLACK, new Position(7, 7)));   // 砲

        // 黑方卒位 (row 6)
        pieces.add(new Piece(PieceType.PAWN, Color.BLACK, new Position(0, 6)));     // 卒
        pieces.add(new Piece(PieceType.PAWN, Color.BLACK, new Position(2, 6)));     // 卒
        pieces.add(new Piece(PieceType.PAWN, Color.BLACK, new Position(4, 6)));     // 卒
        pieces.add(new Piece(PieceType.PAWN, Color.BLACK, new Position(6, 6)));     // 卒
        pieces.add(new Piece(PieceType.PAWN, Color.BLACK, new Position(8, 6)));     // 卒

        return new Board(pieces);
    }
}
