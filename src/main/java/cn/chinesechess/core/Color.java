package cn.chinesechess.core;

/**
 * 棋子颜色枚举，表示红方或黑方
 */
public enum Color {

    /** 红方 */
    RED("红方"),

    /** 黑方 */
    BLACK("黑方");

    private final String name;

    Color(String name) {
        this.name = name;
    }

    /**
     * 获取颜色的中文名称
     * @return 中文名称
     */
    public String getName() {
        return name;
    }

    /**
     * 获取对方颜色
     * @return 对方颜色
     */
    public Color opposite() {
        return this == RED ? BLACK : RED;
    }
}
