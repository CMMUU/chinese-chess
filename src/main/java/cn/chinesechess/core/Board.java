package cn.chinesechess.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 棋盘实现类，使用 Map 存储棋子位置
 * <p>
 * 坐标系：col(列) 0-8 从左到右，row(行) 0-9 从下到上（红方视角）
 * </p>
 */
public class Board implements IBoard {

    /** 使用 Map 存储棋子，key 为位置 */
    private final Map<Position, Piece> pieces;

    /**
     * 创建空棋盘
     */
    public Board() {
        this.pieces = new HashMap<>();
    }

    /**
     * 从已有棋子创建棋盘
     * @param pieceList 棋子列表
     */
    public Board(List<Piece> pieceList) {
        this.pieces = new HashMap<>();
        for (Piece piece : pieceList) {
            pieces.put(piece.getPosition(), piece);
        }
    }

    @Override
    public Piece getPiece(Position pos) {
        return pieces.get(pos);
    }

    @Override
    public void setPiece(Piece piece) {
        Objects.requireNonNull(piece, "棋子不能为null");
        pieces.put(piece.getPosition(), piece);
    }

    @Override
    public Piece removePiece(Position pos) {
        return pieces.remove(pos);
    }

    @Override
    public void movePiece(Move move) {
        Objects.requireNonNull(move, "走棋记录不能为null");
        Piece piece = pieces.remove(move.from());
        if (piece == null) {
            throw new IllegalArgumentException("起始位置没有棋子: " + move.from());
        }
        // 移除目标位置的棋子（如果有）
        pieces.remove(move.to());
        // 将棋子移动到目标位置
        Piece movedPiece = piece.moveTo(move.to());
        pieces.put(move.to(), movedPiece);
    }

    @Override
    public List<Piece> getPieces(Color color) {
        List<Piece> result = new ArrayList<>();
        for (Piece piece : pieces.values()) {
            if (piece.getColor() == color) {
                result.add(piece);
            }
        }
        return result;
    }

    @Override
    public List<Piece> getAllPieces() {
        return new ArrayList<>(pieces.values());
    }

    @Override
    public boolean isEmpty(Position pos) {
        return !pieces.containsKey(pos);
    }

    @Override
    public boolean isOccupied(Position pos) {
        return pieces.containsKey(pos);
    }

    @Override
    public boolean isOccupiedBy(Position pos, Color color) {
        Piece piece = pieces.get(pos);
        return piece != null && piece.getColor() == color;
    }

    @Override
    public IBoard copy() {
        Board copy = new Board();
        for (Map.Entry<Position, Piece> entry : pieces.entrySet()) {
            Piece original = entry.getValue();
            Piece pieceCopy = new Piece(original.getType(), original.getColor(), original.getPosition());
            copy.pieces.put(entry.getKey(), pieceCopy);
        }
        return copy;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int row = 9; row >= 0; row--) {
            sb.append("第").append(row).append("行: ");
            for (int col = 0; col <= 8; col++) {
                Piece piece = pieces.get(new Position(col, row));
                if (piece != null) {
                    sb.append(piece.getDisplayName());
                } else {
                    sb.append("十");
                }
                sb.append(" ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
