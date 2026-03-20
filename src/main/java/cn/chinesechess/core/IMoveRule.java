package cn.chinesechess.core;

import java.util.List;

/**
 * 走棋规则策略接口
 * <p>每种棋子类型对应一个走棋规则实现，采用策略模式</p>
 */
public interface IMoveRule {

    /**
     * 判断此规则是否适用于指定棋子类型
     * @param type 棋子类型
     * @return 是否适用
     */
    boolean isApplicable(PieceType type);

    /**
     * 获取指定位置棋子的所有合法目标位置（伪合法，不检查自检）
     * @param from 起始位置
     * @param board 棋盘
     * @param color 棋子颜色
     * @return 合法目标位置列表
     */
    List<Position> getPseudoLegalMoves(Position from, IBoard board, Color color);
}
