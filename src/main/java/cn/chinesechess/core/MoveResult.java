package cn.chinesechess.core;

/**
 * 走棋结果枚举
 */
public enum MoveResult {

    /** 走棋成功 */
    SUCCESS,

    /** 无效走法 */
    INVALID,

    /** 将军 */
    CHECK,

    /** 将死（绝杀） */
    CHECKMATE,

    /** 困毙（无子可走） */
    STALEMATE,

    /** 和棋 */
    DRAW,

    /** 游戏已结束 */
    GAME_OVER
}
