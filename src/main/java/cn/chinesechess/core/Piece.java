package cn.chinesechess.core;

import java.util.Objects;

/**
 * 棋子类，表示棋盘上的一枚棋子
 * <p>棋子为不可变对象，移动时需创建新实例</p>
 */
public class Piece {

    private final PieceType type;
    private final Color color;
    private final Position position;

    /**
     * 构造棋子
     * @param type 棋子类型
     * @param color 棋子颜色
     * @param position 棋子位置
     */
    public Piece(PieceType type, Color color, Position position) {
        this.type = Objects.requireNonNull(type, "棋子类型不能为null");
        this.color = Objects.requireNonNull(color, "棋子颜色不能为null");
        this.position = Objects.requireNonNull(position, "棋子位置不能为null");
    }

    /**
     * 获取棋子类型
     * @return 棋子类型
     */
    public PieceType getType() {
        return type;
    }

    /**
     * 获取棋子颜色
     * @return 棋子颜色
     */
    public Color getColor() {
        return color;
    }

    /**
     * 获取棋子位置
     * @return 棋子位置
     */
    public Position getPosition() {
        return position;
    }

    /**
     * 获取棋子的中文名称
     * @return 中文名称
     */
    public String getDisplayName() {
        return type.getName(color);
    }

    /**
     * 创建移动到新位置的棋子副本
     * @param newPosition 新位置
     * @return 新的棋子实例
     */
    public Piece moveTo(Position newPosition) {
        return new Piece(type, color, newPosition);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Piece piece = (Piece) o;
        return type == piece.type && color == piece.color && Objects.equals(position, piece.position);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, color, position);
    }

    @Override
    public String toString() {
        return color.getName() + getDisplayName() + "@" + position;
    }
}
