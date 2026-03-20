package cn.chinesechess.core.engine;

import cn.chinesechess.core.*;

/**
 * 游戏事件监听器接口（观察者模式）
 * <p>UI 层实现此接口以响应游戏状态变化。</p>
 */
public interface GameEventListener {

    /**
     * 走棋成功事件
     * @param move 走棋记录
     * @param result 走棋结果
     */
    void onMoveMade(Move move, MoveResult result);

    /**
     * 悔棋事件
     * @param move 被撤销的走棋记录
     */
    void onMoveUndone(Move move);

    /**
     * 游戏结束事件
     * @param result 游戏结果
     */
    void onGameOver(GameResult result);

    /**
     * 当前回合变化事件
     * @param currentTurn 当前回合方
     */
    void onTurnChanged(Color currentTurn);

    /**
     * 将军事件
     * @param attacker 发起将军的一方
     */
    void onCheck(Color attacker);

    /**
     * 游戏状态更新事件（通用通知）
     * @param state 最新的游戏状态
     */
    void onGameStateChanged(GameState state);
}
