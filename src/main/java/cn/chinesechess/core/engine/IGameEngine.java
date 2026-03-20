package cn.chinesechess.core.engine;

import cn.chinesechess.core.*;

import java.util.List;

/**
 * 游戏引擎接口
 * <p>管理游戏核心逻辑：走棋、悔棋、认输、求和、新游戏。</p>
 */
public interface IGameEngine {

    /**
     * 开始新游戏
     */
    void newGame();

    /**
     * 执行走棋
     * @param from 起始位置
     * @param to 目标位置
     * @return 走棋结果
     */
    MoveResult makeMove(Position from, Position to);

    /**
     * 悔棋（撤销最后一步）
     * @return 是否成功悔棋
     */
    boolean undoMove();

    /**
     * 认输
     * @param color 认输方
     */
    void resign(Color color);

    /**
     * 求和
     * @param color 求和方
     * @return 是否接受和棋
     */
    boolean offerDraw(Color color);

    /**
     * 接受和棋
     */
    void acceptDraw();

    /**
     * 获取当前回合方
     * @return 当前回合方
     */
    Color getCurrentTurn();

    /**
     * 获取游戏是否已结束
     * @return 游戏是否结束
     */
    boolean isGameOver();

    /**
     * 获取游戏结果
     * @return 游戏结果
     */
    GameResult getGameResult();

    /**
     * 获取当前棋盘
     * @return 棋盘
     */
    IBoard getBoard();

    /**
     * 获取走棋历史
     * @return 走棋记录列表
     */
    List<Move> getMoveHistory();

    /**
     * 获取指定位置棋子的所有合法目标位置
     * @param from 起始位置
     * @return 合法目标位置列表
     */
    List<Position> getLegalTargets(Position from);

    /**
     * 获取当前游戏状态快照
     * @return 游戏状态
     */
    GameState getState();

    /**
     * 添加游戏事件监听器
     * @param listener 监听器
     */
    void addEventListener(GameEventListener listener);

    /**
     * 移除游戏事件监听器
     * @param listener 监听器
     */
    void removeEventListener(GameEventListener listener);

    /**
     * 获取游戏模式
     * @return 游戏模式
     */
    GameMode getGameMode();

    /**
     * 设置游戏模式
     * @param mode 游戏模式
     */
    void setGameMode(GameMode mode);

    /**
     * 设置先手方
     * @param firstHand 先手方
     */
    void setFirstHand(Color firstHand);
}
