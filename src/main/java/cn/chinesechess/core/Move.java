package cn.chinesechess.core;

import java.util.Objects;

/**
 * 走棋记录类，记录一步棋的起始位置、目标位置和被吃棋子
 *
 * @param from 起始位置
 * @param to 目标位置
 * @param captured 被吃掉的棋子，若无则为null
 */
public record Move(Position from, Position to, Piece captured) {

    /**
     * 构造走棋记录（无吃子）
     * @param from 起始位置
     * @param to 目标位置
     */
    public Move(Position from, Position to) {
        this(from, to, null);
    }

    /**
     * 生成中文记谱法
     * <p>格式：棋子名 + 起始列号 + 方向(进/退/平) + 目标列号或行距</p>
     * <p>红方列号用中文数字（1-9对应一到九），黑方用阿拉伯数字（1-9）</p>
     * @param movedPiece 被移动的棋子
     * @return 中文记谱字符串
     */
    public String toChineseNotation(Piece movedPiece) {
        Color color = movedPiece.getColor();
        String pieceName = movedPiece.getDisplayName();

        // 获取列号显示（红方用中文数字，黑方用阿拉伯数字）
        String fromColStr = colToNotation(from.col(), color);
        String toColStr = colToNotation(to.col(), color);

        // 判断方向
        int rowDiff = to.row() - from.row();
        int colDiff = to.col() - from.col();
        String direction;

        if (color == Color.RED) {
            // 红方视角：row增大为"进"
            if (rowDiff > 0) {
                direction = "进";
            } else if (rowDiff < 0) {
                direction = "退";
            } else {
                direction = "平";
            }
        } else {
            // 黑方视角：row减小为"进"
            if (rowDiff < 0) {
                direction = "进";
            } else if (rowDiff > 0) {
                direction = "退";
            } else {
                direction = "平";
            }
        }

        // 目标位置：纵向移动显示行距，横向移动显示目标列号
        String target;
        if (colDiff == 0) {
            // 纵向移动，显示行距
            int rowDistance = Math.abs(rowDiff);
            target = color == Color.RED ? chineseDigit(rowDistance) : String.valueOf(rowDistance);
        } else {
            target = toColStr;
        }

        return pieceName + fromColStr + direction + target;
    }

    /**
     * 将列坐标转换为记谱用的数字字符串
     * @param col 列坐标（0-8）
     * @param color 棋子颜色
     * @return 记谱用的列号字符串
     */
    private String colToNotation(int col, Color color) {
        if (color == Color.RED) {
            // 红方：从右到左为一到九
            return chineseDigit(9 - col);
        } else {
            // 黑方：从左到右为1到9
            return String.valueOf(col + 1);
        }
    }

    /**
     * 数字转中文数字（1-9）
     * @param num 数字
     * @return 中文数字
     */
    private String chineseDigit(int num) {
        String[] digits = {"零", "一", "二", "三", "四", "五", "六", "七", "八", "九"};
        if (num >= 0 && num <= 9) {
            return digits[num];
        }
        return String.valueOf(num);
    }

    @Override
    public String toString() {
        return from + " -> " + to + (captured != null ? " (吃" + captured.getDisplayName() + ")" : "");
    }
}
