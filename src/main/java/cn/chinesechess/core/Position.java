package cn.chinesechess.core;

import java.util.Objects;

/**
 * 棋盘位置类，使用行列坐标表示
 * <p>
 * 坐标系：col(列) 0-8 从左到右，row(行) 0-9 从下到上（红方视角）
 * 红方在下方(row 0-4)，黑方在上方(row 5-9)
 * </p>
 *
 * @param col 列坐标，0-8（0为红方最右列/黑方最左列）
 * @param row 行坐标，0-9（0为红方底线，9为黑方底线）
 */
public record Position(int col, int row) {

    /**
     * 判断位置是否在棋盘范围内
     * @return 是否有效
     */
    public boolean isValid() {
        return col >= 0 && col <= 8 && row >= 0 && row <= 9;
    }

    /**
     * 判断位置是否在指定颜色方的九宫格内
     * 九宫格范围：列 3-5，红方行 0-2，黑方行 7-9
     * @param color 棋子颜色
     * @return 是否在九宫内
     */
    public boolean isInPalace(Color color) {
        if (col < 3 || col > 5) {
            return false;
        }
        if (color == Color.RED) {
            return row >= 0 && row <= 2;
        } else {
            return row >= 7 && row <= 9;
        }
    }

    /**
     * 判断指定颜色方的棋子是否已过河
     * 红方过河：row >= 5；黑方过河：row <= 4
     * @param color 棋子颜色
     * @return 是否已过河
     */
    public boolean hasCrossedRiver(Color color) {
        return color == Color.RED ? row >= 5 : row <= 4;
    }

    /**
     * 获取移动后的新位置
     * @param dCol 列偏移量
     * @param dRow 行偏移量
     * @return 新位置
     */
    public Position offset(int dCol, int dRow) {
        return new Position(col + dCol, row + dRow);
    }
}
