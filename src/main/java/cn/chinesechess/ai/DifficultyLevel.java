package cn.chinesechess.ai;

/**
 * AI 难度等级枚举
 * <p>不同难度对应不同的搜索深度，影响 AI 的计算能力和响应时间。</p>
 */
public enum DifficultyLevel {

    /** 简单：搜索深度 2，适合初学者 */
    EASY("简单", 2),

    /** 中等：搜索深度 4，有一定挑战性 */
    MEDIUM("中等", 4),

    /** 困难：搜索深度 6，较强的计算能力 */
    HARD("困难", 6);

    private final String name;
    private final int searchDepth;

    DifficultyLevel(String name, int searchDepth) {
        this.name = name;
        this.searchDepth = searchDepth;
    }

    /**
     * 获取难度名称
     * @return 中文名称
     */
    public String getName() {
        return name;
    }

    /**
     * 获取搜索深度
     * @return 搜索深度
     */
    public int getSearchDepth() {
        return searchDepth;
    }
}
