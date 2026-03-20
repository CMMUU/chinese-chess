package cn.chinesechess.core;

import java.util.List;

/**
 * 棋盘操作接口，定义棋盘的基本操作
 */
public interface IBoard {

    /**
     * 获取指定位置的棋子
     * @param pos 位置
     * @return 棋子，如果该位置无棋子则返回 null
     */
    Piece getPiece(Position pos);

    /**
     * 在指定位置放置棋子
     * @param piece 棋子
     */
    void setPiece(Piece piece);

    /**
     * 移除指定位置的棋子
     * @param pos 位置
     * @return 被移除的棋子，如果该位置无棋子则返回 null
     */
    Piece removePiece(Position pos);

    /**
     * 执行走棋操作
     * @param move 走棋记录
     */
    void movePiece(Move move);

    /**
     * 获取指定颜色方的所有棋子
     * @param color 颜色
     * @return 棋子列表
     */
    List<Piece> getPieces(Color color);

    /**
     * 获取棋盘上所有棋子
     * @return 棋子列表
     */
    List<Piece> getAllPieces();

    /**
     * 判断指定位置是否为空
     * @param pos 位置
     * @return 是否为空
     */
    boolean isEmpty(Position pos);

    /**
     * 判断指定位置是否有棋子
     * @param pos 位置
     * @return 是否有棋子
     */
    boolean isOccupied(Position pos);

    /**
     * 判断指定位置是否有指定颜色的棋子
     * @param pos 位置
     * @param color 颜色
     * @return 是否有指定颜色的棋子
     */
    boolean isOccupiedBy(Position pos, Color color);

    /**
     * 获取棋盘的深拷贝
     * @return 棋盘副本
     */
    IBoard copy();
}
