package cn.chinesechess.ai;

import cn.chinesechess.core.IBoard;
import cn.chinesechess.core.Move;
import cn.chinesechess.core.Color;

/**
 * AI 引擎接口
 * <p>定义 AI 计算最佳走法的接口。</p>
 */
public interface IAIEngine {

    /**
     * 计算最佳走法
     * @param board 当前棋盘
     * @param color AI 所执颜色
     * @return 最佳走法
     */
    Move findBestMove(IBoard board, Color color);

    /**
     * 获取当前难度等级
     * @return 难度等级
     */
    DifficultyLevel getDifficulty();

    /**
     * 设置难度等级
     * @param level 难度等级
     */
    void setDifficulty(DifficultyLevel level);
}
