package cn.chinesechess.core;

import java.util.Collections;
import java.util.List;

/**
 * 游戏状态快照，保存完整的游戏状态信息
 *
 * @param pieces 当前棋盘上所有棋子
 * @param currentTurn 当前回合方
 * @param result 游戏结果
 * @param moveHistory 走棋历史记录
 * @param moveCount 总步数
 */
public record GameState(
        List<Piece> pieces,
        Color currentTurn,
        GameResult result,
        List<Move> moveHistory,
        int moveCount
) {

    /**
     * 创建初始游戏状态
     * @param pieces 初始棋子列表
     * @return 初始游戏状态
     */
    public static GameState initial(List<Piece> pieces) {
        return new GameState(
                Collections.unmodifiableList(pieces),
                Color.RED,
                GameResult.IN_PROGRESS,
                Collections.emptyList(),
                0
        );
    }
}
