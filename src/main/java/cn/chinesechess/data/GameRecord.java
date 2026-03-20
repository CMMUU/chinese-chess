package cn.chinesechess.data;

import cn.chinesechess.core.*;

import java.util.List;

/**
 * 棋谱记录
 * <p>保存一局游戏的完整信息，包括游戏模式、最终状态和走棋历史。
 * 用于 JSON 序列化存储和加载。</p>
 *
 * @param mode 游戏模式
 * @param state 最终游戏状态
 * @param moves 走棋历史记录
 */
public record GameRecord(
        GameMode mode,
        GameState state,
        List<Move> moves
) {

    /**
     * 从游戏引擎状态创建棋谱记录
     * @param mode 游戏模式
     * @param state 游戏状态
     * @param moves 走棋历史
     * @return 棋谱记录
     */
    public static GameRecord fromEngine(GameMode mode, GameState state, List<Move> moves) {
        return new GameRecord(mode, state, List.copyOf(moves));
    }
}
