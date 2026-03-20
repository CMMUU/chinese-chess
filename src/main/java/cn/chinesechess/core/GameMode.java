package cn.chinesechess.core;

/**
 * 游戏模式枚举
 */
public enum GameMode {

    /** 人机对战 */
    PVE("人机对战"),

    /** 双人对战 */
    PVP("双人对战");

    private final String name;

    GameMode(String name) {
        this.name = name;
    }

    /**
     * 获取模式名称
     * @return 模式中文名称
     */
    public String getName() {
        return name;
    }
}
