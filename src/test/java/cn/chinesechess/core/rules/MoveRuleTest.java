package cn.chinesechess.core.rules;

import cn.chinesechess.core.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 走棋规则单元测试
 */
class MoveRuleTest {

    private Board board;

    @BeforeEach
    void setUp() {
        board = new Board();
    }

    // ==================== 车测试 ====================

    @Test
    void testRookHorizontalMove() {
        // 车在 (0,0)，可以横向走到 (8,0)
        board.setPiece(new Piece(PieceType.ROOK, Color.RED, new Position(0, 0)));
        RookMoveRule rule = new RookMoveRule();
        List<Position> moves = rule.getPseudoLegalMoves(new Position(0, 0), board, Color.RED);

        // 车在 (0,0) 空棋盘：右走 8 格 + 上走 8 格 = 16
        assertEquals(16, moves.size(), "车在空棋盘角上应能走到 16 个位置");
        assertTrue(moves.contains(new Position(8, 0)));
    }

    @Test
    void testRookBlockedByOwnPiece() {
        // 车在 (0,0)，己方兵在 (3,0)，车不能越过兵
        board.setPiece(new Piece(PieceType.ROOK, Color.RED, new Position(0, 0)));
        board.setPiece(new Piece(PieceType.PAWN, Color.RED, new Position(3, 0)));
        RookMoveRule rule = new RookMoveRule();
        List<Position> moves = rule.getPseudoLegalMoves(new Position(0, 0), board, Color.RED);

        assertTrue(moves.contains(new Position(2, 0)));
        assertFalse(moves.contains(new Position(3, 0)), "车不能走到己方棋子位置");
        assertFalse(moves.contains(new Position(4, 0)), "车不能越过己方棋子");
    }

    @Test
    void testRookCaptureEnemy() {
        // 车在 (0,0)，对方兵在 (3,0)，车可以吃兵但不能越过
        board.setPiece(new Piece(PieceType.ROOK, Color.RED, new Position(0, 0)));
        board.setPiece(new Piece(PieceType.PAWN, Color.BLACK, new Position(3, 0)));
        RookMoveRule rule = new RookMoveRule();
        List<Position> moves = rule.getPseudoLegalMoves(new Position(0, 0), board, Color.RED);

        assertTrue(moves.contains(new Position(3, 0)), "车可以吃对方棋子");
        assertFalse(moves.contains(new Position(4, 0)), "车吃子后不能继续移动");
    }

    // ==================== 马测试 ====================

    @Test
    void testKnightMoves() {
        // 马在 (1,0)，空棋盘，应有 3 个走法：(0,2)、(2,2)、(3,1)
        board.setPiece(new Piece(PieceType.KNIGHT, Color.RED, new Position(1, 0)));
        KnightMoveRule rule = new KnightMoveRule();
        List<Position> moves = rule.getPseudoLegalMoves(new Position(1, 0), board, Color.RED);

        assertTrue(moves.contains(new Position(0, 2)));
        assertTrue(moves.contains(new Position(2, 2)));
        assertTrue(moves.contains(new Position(3, 1)));
        assertEquals(3, moves.size());
    }

    @Test
    void testKnightBlocked() {
        // 马在 (4,0)，四周放棋子蹩腿，应无法走棋
        board.setPiece(new Piece(PieceType.KNIGHT, Color.RED, new Position(4, 0)));
        board.setPiece(new Piece(PieceType.PAWN, Color.RED, new Position(4, 1))); // 蹩上
        board.setPiece(new Piece(PieceType.PAWN, Color.RED, new Position(3, 0))); // 蹩左
        board.setPiece(new Piece(PieceType.PAWN, Color.RED, new Position(5, 0))); // 蹩右
        KnightMoveRule rule = new KnightMoveRule();
        List<Position> moves = rule.getPseudoLegalMoves(new Position(4, 0), board, Color.RED);

        assertTrue(moves.isEmpty(), "马腿被蹩时不能走棋");
    }

    // ==================== 炮测试 ====================

    @Test
    void testCannonMoveWithoutCapture() {
        // 炮在 (1,2)，无子阻挡，可以横向移动
        board.setPiece(new Piece(PieceType.CANNON, Color.RED, new Position(1, 2)));
        CannonMoveRule rule = new CannonMoveRule();
        List<Position> moves = rule.getPseudoLegalMoves(new Position(1, 2), board, Color.RED);

        // 可以横向：右7格 + 左1格 = 8
        // 可以纵向：上7格 + 下2格 = 9
        // 总共 8 + 9 = 17
        assertEquals(17, moves.size());
    }

    @Test
    void testCannonCaptureOverScreen() {
        // 炮在 (1,2)，炮架在 (3,2)，对方车在 (5,2)
        board.setPiece(new Piece(PieceType.CANNON, Color.RED, new Position(1, 2)));
        board.setPiece(new Piece(PieceType.PAWN, Color.RED, new Position(3, 2))); // 炮架
        board.setPiece(new Piece(PieceType.ROOK, Color.BLACK, new Position(5, 2))); // 目标
        CannonMoveRule rule = new CannonMoveRule();
        List<Position> moves = rule.getPseudoLegalMoves(new Position(1, 2), board, Color.RED);

        assertTrue(moves.contains(new Position(5, 2)), "炮可以隔子吃对方棋子");
        assertFalse(moves.contains(new Position(3, 2)), "炮不能吃炮架");
    }

    // ==================== 兵测试 ====================

    @Test
    void testPawnForwardOnlyBeforeRiver() {
        // 兵在 (0,3)，未过河，只能前进
        board.setPiece(new Piece(PieceType.PAWN, Color.RED, new Position(0, 3)));
        PawnMoveRule rule = new PawnMoveRule();
        List<Position> moves = rule.getPseudoLegalMoves(new Position(0, 3), board, Color.RED);

        assertEquals(1, moves.size());
        assertTrue(moves.contains(new Position(0, 4)));
    }

    @Test
    void testPawnCanMoveSidewaysAfterRiver() {
        // 兵在 (4,6)，已过河，可以前进和左右
        board.setPiece(new Piece(PieceType.PAWN, Color.RED, new Position(4, 6)));
        PawnMoveRule rule = new PawnMoveRule();
        List<Position> moves = rule.getPseudoLegalMoves(new Position(4, 6), board, Color.RED);

        assertEquals(3, moves.size());
        assertTrue(moves.contains(new Position(4, 7))); // 前进
        assertTrue(moves.contains(new Position(3, 6))); // 左
        assertTrue(moves.contains(new Position(5, 6))); // 右
    }

    // ==================== 将帅测试 ====================

    @Test
    void testKingStayInPalace() {
        // 帅在 (4,1)，可以走到九宫内相邻位置
        board.setPiece(new Piece(PieceType.KING, Color.RED, new Position(4, 1)));
        KingMoveRule rule = new KingMoveRule();
        List<Position> moves = rule.getPseudoLegalMoves(new Position(4, 1), board, Color.RED);

        assertTrue(moves.contains(new Position(4, 0)));
        assertTrue(moves.contains(new Position(4, 2)));
        assertTrue(moves.contains(new Position(3, 1)));
        assertTrue(moves.contains(new Position(5, 1)));
        assertEquals(4, moves.size());
    }

    @Test
    void testKingCannotLeavePalace() {
        // 帅在 (4,2)，不能走到 (4,3)（出九宫）
        board.setPiece(new Piece(PieceType.KING, Color.RED, new Position(4, 2)));
        KingMoveRule rule = new KingMoveRule();
        List<Position> moves = rule.getPseudoLegalMoves(new Position(4, 2), board, Color.RED);

        assertFalse(moves.contains(new Position(4, 3)), "将帅不能离开九宫");
    }

    // ==================== 位置测试 ====================

    @Test
    void testPositionIsValid() {
        assertTrue(new Position(0, 0).isValid());
        assertTrue(new Position(8, 9).isValid());
        assertFalse(new Position(-1, 0).isValid());
        assertFalse(new Position(9, 0).isValid());
        assertFalse(new Position(0, 10).isValid());
    }

    @Test
    void testPositionInPalace() {
        assertTrue(new Position(4, 1).isInPalace(Color.RED));
        assertTrue(new Position(3, 0).isInPalace(Color.RED));
        assertTrue(new Position(4, 8).isInPalace(Color.BLACK));
        assertFalse(new Position(0, 0).isInPalace(Color.RED));
    }

    @Test
    void testPositionCrossedRiver() {
        assertTrue(new Position(0, 5).hasCrossedRiver(Color.RED));
        assertFalse(new Position(0, 4).hasCrossedRiver(Color.RED));
        assertTrue(new Position(0, 4).hasCrossedRiver(Color.BLACK));
        assertFalse(new Position(0, 5).hasCrossedRiver(Color.BLACK));
    }
}
