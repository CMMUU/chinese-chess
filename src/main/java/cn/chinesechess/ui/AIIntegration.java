package cn.chinesechess.ui;

import cn.chinesechess.ai.DifficultyLevel;
import cn.chinesechess.ai.IAIEngine;
import cn.chinesechess.ai.MinimaxEngine;
import cn.chinesechess.core.*;

import javafx.application.Platform;
import javafx.concurrent.Task;

import java.util.function.Consumer;

/**
 * AI 集成组件
 * <p>在后台线程中执行 AI 计算，避免阻塞 JavaFX 主线程。
 * 使用 javafx.concurrent.Task 实现异步计算。</p>
 */
public class AIIntegration {

    /** AI 引擎 */
    private final IAIEngine aiEngine;

    /** AI 所执颜色 */
    private Color aiColor;

    /** 是否启用 AI */
    private boolean enabled;

    /** AI 正在计算的标记 */
    private volatile boolean computing;

    /** 状态回调 */
    private Consumer<String> statusCallback;

    public AIIntegration() {
        this.aiEngine = new MinimaxEngine();
        this.aiColor = Color.BLACK;
        this.enabled = false;
        this.computing = false;
    }

    /**
     * 设置是否启用 AI
     * @param enabled 是否启用
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * 是否启用 AI
     * @return 是否启用
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * 设置 AI 颜色
     * @param color AI 所执颜色
     */
    public void setAiColor(Color color) {
        this.aiColor = color;
    }

    /**
     * 获取 AI 颜色
     * @return AI 颜色
     */
    public Color getAiColor() {
        return aiColor;
    }

    /**
     * 设置难度等级
     * @param level 难度等级
     */
    public void setDifficulty(DifficultyLevel level) {
        aiEngine.setDifficulty(level);
    }

    /**
     * 设置状态回调
     * @param callback 回调函数
     */
    public void setStatusCallback(Consumer<String> callback) {
        this.statusCallback = callback;
    }

    /**
     * 是否正在计算
     * @return 是否在计算
     */
    public boolean isComputing() {
        return computing;
    }

    /**
     * 在后台线程中计算 AI 走法
     * @param board 当前棋盘
     * @param onComplete 计算完成后的回调（在 UI 线程执行）
     */
    public void calculateMove(IBoard board, Consumer<Move> onComplete) {
        if (!enabled || computing) {
            return;
        }

        computing = true;
        updateStatus("AI 思考中...");

        Task<Move> task = new Task<>() {
            @Override
            protected Move call() throws Exception {
                return aiEngine.findBestMove(board, aiColor);
            }
        };

        task.setOnSucceeded(event -> {
            computing = false;
            Move bestMove = task.getValue();
            updateStatus("AI 计算完成");
            if (bestMove != null && onComplete != null) {
                onComplete.accept(bestMove);
            }
        });

        task.setOnFailed(event -> {
            computing = false;
            updateStatus("AI 计算失败");
            Throwable ex = task.getException();
            if (ex != null) {
                ex.printStackTrace();
            }
        });

        Thread thread = new Thread(task, "AI-Engine");
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * 更新状态显示
     */
    private void updateStatus(String status) {
        if (statusCallback != null) {
            statusCallback.accept(status);
        }
    }
}
