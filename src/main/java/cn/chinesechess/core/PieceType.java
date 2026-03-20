package cn.chinesechess.core;

/**
 * 棋子类型枚举，包含所有 7 种棋子类型
 * 每种类型有红方和黑方两个中文名称
 */
public enum PieceType {

    /** 帥/将 */
    KING("帅", "将"),

    /** 仕/士 */
    ADVISOR("仕", "士"),

    /** 相/象 */
    ELEPHANT("相", "象"),

    /** 俥/車 */
    ROOK("俥", "車"),

    /** 傌/馬 */
    KNIGHT("傌", "馬"),

    /** 炮/砲 */
    CANNON("炮", "砲"),

    /** 兵/卒 */
    PAWN("兵", "卒");

    private final String redName;
    private final String blackName;

    PieceType(String redName, String blackName) {
        this.redName = redName;
        this.blackName = blackName;
    }

    /**
     * 获取红方名称
     * @return 红方中文名称
     */
    public String getRedName() {
        return redName;
    }

    /**
     * 获取黑方名称
     * @return 黑方中文名称
     */
    public String getBlackName() {
        return blackName;
    }

    /**
     * 根据颜色获取对应的中文名称
     * @param color 棋子颜色
     * @return 对应颜色的中文名称
     */
    public String getName(Color color) {
        return color == Color.RED ? redName : blackName;
    }
}
