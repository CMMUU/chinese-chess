package cn.chinesechess.ai;

import cn.chinesechess.core.Color;

import java.nio.file.Path;
import java.util.List;

/**
 * 象棋引擎接口
 * <p>支持内置引擎和外部 UCI 引擎。</p>
 */
public interface ChessEngine {

    /**
     * 获取引擎名称
     * @return 引擎名称
     */
    String getName();

    /**
     * 获取引擎版本
     * @return 版本号
     */
    String getVersion();

    /**
     * 获取引擎描述
     * @return 描述信息
     */
    String getDescription();

    /**
     * 获取引擎类型
     * @return 引擎类型
     */
    EngineType getType();

    /**
     * 获取引擎文件路径（外部引擎）
     * @return 路径，可能为 null
     */
    Path getPath();

    /**
     * 初始化引擎
     */
    void initialize();

    /**
     * 关闭引擎
     */
    void shutdown();

    /**
     * 设置思考深度
     * @param depth 深度
     */
    void setDepth(int depth);

    /**
     * 设置行棋方
     * @param color 行棋方
     */
    void setPosition(Color color);

    /**
     * 设置棋盘局面（FEN 格式）
     * @param fen FEN 字符串
     */
    void setFen(String fen);

    /**
     * 设置走棋历史
     * @param moves 走棋记录
     */
    void setMoves(List<String> moves);

    /**
     * 开始思考
     */
    void startThinking();

    /**
     * 停止思考
     */
    void stopThinking();

    /**
     * 获取最佳走法
     * @return UCI 格式的走法，如 "moves h2e2"
     */
    String getBestMove();

    /**
     * 是否正在思考
     * @return 是否在思考
     */
    boolean isThinking();

    /**
     * 引擎是否可用
     * @return 是否可用
     */
    boolean isAvailable();

    /**
     * 引擎类型枚举
     */
    enum EngineType {
        /** 内置引擎 */
        BUILTIN,
        /** UCI 引擎 */
        UCI
    }
}
