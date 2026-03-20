package cn.chinesechess.core;

/**
 * 游戏结果枚举
 */
public enum GameResult {

    /** 进行中 */
    IN_PROGRESS("进行中"),

    /** 红方获胜 */
    RED_WIN("红方获胜"),

    /** 黑方获胜 */
    BLACK_WIN("黑方获胜"),

    /** 和棋 */
    DRAW("和棋");

    private final String description;

    GameResult(String description) {
        this.description = description;
    }

    /**
     * 获取结果描述
     * @return 结果描述文字
     */
    public String getDescription() {
        return description;
    }
}
