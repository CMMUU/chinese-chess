package cn.chinesechess.data;

import cn.chinesechess.ai.DifficultyLevel;
import cn.chinesechess.core.Color;
import cn.chinesechess.core.GameMode;

/**
 * 应用设置
 * <p>保存用户的偏好设置，支持 JSON 序列化。</p>
 *
 * @param gameMode 默认游戏模式
 * @param difficulty 默认 AI 难度
 * @param playerColor 玩家颜色（PVE 模式）
 * @param soundEnabled 是否开启音效
 * @param showNotation 是否显示中文记谱
 * @param windowWidth 窗口宽度
 * @param windowHeight 窗口高度
 */
public record AppSettings(
        GameMode gameMode,
        DifficultyLevel difficulty,
        Color playerColor,
        boolean soundEnabled,
        boolean showNotation,
        int windowWidth,
        int windowHeight
) {

    /**
     * 创建默认设置
     * @return 默认应用设置
     */
    public static AppSettings defaults() {
        return new AppSettings(
                GameMode.PVP,
                DifficultyLevel.MEDIUM,
                Color.RED,
                true,
                true,
                800,
                700
        );
    }

    /**
     * 更新游戏模式
     * @param mode 新的游戏模式
     * @return 新的设置实例
     */
    public AppSettings withGameMode(GameMode mode) {
        return new AppSettings(mode, difficulty, playerColor, soundEnabled,
                showNotation, windowWidth, windowHeight);
    }

    /**
     * 更新难度等级
     * @param level 新的难度等级
     * @return 新的设置实例
     */
    public AppSettings withDifficulty(DifficultyLevel level) {
        return new AppSettings(gameMode, level, playerColor, soundEnabled,
                showNotation, windowWidth, windowHeight);
    }
}
