package cn.chinesechess.core;

/**
 * 游戏命令接口，用于支持命令模式（悔棋功能）
 * <p>每个走棋操作封装为一个命令对象，可执行和撤销。</p>
 */
public interface GameCommand {

    /**
     * 执行命令
     * @param board 棋盘
     */
    void execute(IBoard board);

    /**
     * 撤销命令
     * @param board 棋盘
     */
    void undo(IBoard board);

    /**
     * 获取关联的走棋记录
     * @return 走棋记录
     */
    Move getMove();
}
