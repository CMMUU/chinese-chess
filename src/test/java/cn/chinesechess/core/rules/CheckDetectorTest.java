package cn.chinesechess.core.rules;

import cn.chinesechess.core.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 将军检测器单元测试
 */
class CheckDetectorTest {

    private Board board;
    private CheckDetector checkDetector;

    @BeforeEach
    void setUp() {
        board = new Board();
        checkDetector = new CheckDetector();
    }

    @Test
    void testNoCheck() {
        // 帅在九宫，不同列，无将军
        board.setPiece(new Piece(PieceType.KING, Color.RED, new Position(4, 1)));
        board.setPiece(new Piece(PieceType.KING, Color.BLACK, new Position(3, 8)));

        assertFalse(checkDetector.isInCheck(board, Color.RED));
        assertFalse(checkDetector.isInCheck(board, Color.BLACK));
    }

    @Test
    void testCheckByRook() {
        // 车在同一列将军
        board.setPiece(new Piece(PieceType.KING, Color.RED, new Position(4, 1)));
        board.setPiece(new Piece(PieceType.KING, Color.BLACK, new Position(4, 8)));
        board.setPiece(new Piece(PieceType.ROOK, Color.BLACK, new Position(4, 5)));

        assertTrue(checkDetector.isInCheck(board, Color.RED), "车将军");
    }

    @Test
    void testCheckBlocked() {
        // 车被己方棋子挡住，不构成将军
        board.setPiece(new Piece(PieceType.KING, Color.RED, new Position(4, 1)));
        board.setPiece(new Piece(PieceType.KING, Color.BLACK, new Position(4, 8)));
        board.setPiece(new Piece(PieceType.ROOK, Color.BLACK, new Position(4, 5)));
        board.setPiece(new Piece(PieceType.PAWN, Color.RED, new Position(4, 3))); // 挡住车

        assertFalse(checkDetector.isInCheck(board, Color.RED), "车被挡住，不构成将军");
    }
}
