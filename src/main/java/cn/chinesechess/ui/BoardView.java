package cn.chinesechess.ui;

import cn.chinesechess.core.*;

import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.List;

/**
 * 棋盘绘制视图
 * <p>使用 Canvas 绘制棋盘网格、九宫格、楚河汉界、坐标标注和棋子。
 * 支持高亮选中棋子和合法目标位置。</p>
 */
public class BoardView extends StackPane {

    /** 棋盘边距（像素） */
    private static final int PADDING = 40;

    /** 每格宽度（像素） */
    private static final int CELL_SIZE = 65;

    /** 棋盘宽度（像素）= 8列 * 格宽 + 2*边距 */
    private static final int BOARD_WIDTH = 8 * CELL_SIZE + 2 * PADDING;

    /** 棋盘高度（像素）= 9行 * 格宽 + 2*边距 */
    private static final int BOARD_HEIGHT = 9 * CELL_SIZE + 2 * PADDING;

    /** 棋子半径 */
    private static final int PIECE_RADIUS = 28;

    /** Canvas 绘图表面 */
    private final Canvas canvas;

    /** 棋盘模型 */
    private IBoard board;

    /** 当前选中的位置 */
    private Position selectedPosition;

    /** 合法目标位置列表 */
    private List<Position> legalTargets;

    /** 是否翻转棋盘（当前执棋方在下方） */
    private boolean flipped = false;

    public BoardView() {
        canvas = new Canvas(BOARD_WIDTH, BOARD_HEIGHT);
        getChildren().add(canvas);
        setPadding(new Insets(10));
        setStyle("-fx-background-color: #DEB887;");
    }

    /**
     * 绑定棋盘数据
     * @param board 棋盘
     */
    public void setBoard(IBoard board) {
        this.board = board;
        repaint();
    }

    /**
     * 设置选中的位置和合法目标
     * @param selected 选中位置，null 表示取消选中
     * @param targets 合法目标位置列表
     */
    public void setSelected(Position selected, List<Position> targets) {
        this.selectedPosition = selected;
        this.legalTargets = targets;
        repaint();
    }

    /**
     * 取消选中状态
     */
    public void clearSelection() {
        this.selectedPosition = null;
        this.legalTargets = null;
        repaint();
    }

    /**
     * 设置棋盘翻转状态
     * @param flipped true 表示翻转（当前执棋方在下方）
     */
    public void setFlipped(boolean flipped) {
        this.flipped = flipped;
        repaint();
    }

    /**
     * 获取棋盘翻转状态
     * @return true 表示已翻转
     */
    public boolean isFlipped() {
        return flipped;
    }

    /**
     * 翻转棋盘
     */
    public void flip() {
        this.flipped = !this.flipped;
        repaint();
    }

    /**
     * 将棋盘坐标转换为画布坐标
     * @param col 棋盘列索引
     * @param row 棋盘行索引
     * @return 画布坐标数组 [x, y]
     */
    private double[] boardToCanvas(int col, int row) {
        int displayCol = flipped ? 8 - col : col;
        int displayRow = flipped ? 9 - row : row;
        return new double[]{
            PADDING + displayCol * CELL_SIZE,
            PADDING + displayRow * CELL_SIZE
        };
    }

    /**
     * 重绘整个棋盘
     */
    public void repaint() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, BOARD_WIDTH, BOARD_HEIGHT);

        drawBoard(gc);
        drawCoordinates(gc);
        drawPieces(gc);
        drawHighlights(gc);
    }

    /**
     * 绘制棋盘网格、九宫格和楚河汉界
     */
    private void drawBoard(GraphicsContext gc) {
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1.5);

        // 绘制横线（10行 = 9条线，加上下边界）
        for (int row = 0; row <= 9; row++) {
            double y = PADDING + row * CELL_SIZE;
            // 楚河汉界区域（row 4-5 之间），中间不画线
            double x1 = PADDING;
            double x2 = PADDING + 8 * CELL_SIZE;
            gc.strokeLine(x1, y, x2, y);
        }

        // 绘制竖线
        // 边缘竖线(col 0和8)连续绘制，形成完整边框
        // 中间竖线(col 1-7)在楚河汉界区域断开
        for (int col = 0; col <= 8; col++) {
            double x = PADDING + col * CELL_SIZE;

            if (col == 0 || col == 8) {
                // 边线：连续绘制（中间不断开）
                gc.strokeLine(x, PADDING, x, PADDING + 9 * CELL_SIZE);
            } else {
                // 中间竖线：在楚河汉界处断开
                // 上半部分
                gc.strokeLine(x, PADDING, x, PADDING + 4 * CELL_SIZE);
                // 下半部分
                gc.strokeLine(x, PADDING + 5 * CELL_SIZE, x, PADDING + 9 * CELL_SIZE);
            }
        }

        // 绘制九宫格对角线
        // 红方九宫（col 3-5, row 0-2）
        gc.strokeLine(
                PADDING + 3 * CELL_SIZE, PADDING,
                PADDING + 5 * CELL_SIZE, PADDING + 2 * CELL_SIZE
        );
        gc.strokeLine(
                PADDING + 5 * CELL_SIZE, PADDING,
                PADDING + 3 * CELL_SIZE, PADDING + 2 * CELL_SIZE
        );

        // 黑方九宫（col 3-5, row 7-9）
        gc.strokeLine(
                PADDING + 3 * CELL_SIZE, PADDING + 7 * CELL_SIZE,
                PADDING + 5 * CELL_SIZE, PADDING + 9 * CELL_SIZE
        );
        gc.strokeLine(
                PADDING + 5 * CELL_SIZE, PADDING + 7 * CELL_SIZE,
                PADDING + 3 * CELL_SIZE, PADDING + 9 * CELL_SIZE
        );

        // 绘制兵/卒位置的标记（斜线）
        drawPositionMarkers(gc, 3, Color.RED);   // 红方兵位 row=3
        drawPositionMarkers(gc, 6, Color.BLACK); // 黑方位位 row=6

        // 绘制炮位标记
        drawCannonMarkers(gc, 2, Color.RED);     // 红方炮位 row=2
        drawCannonMarkers(gc, 7, Color.BLACK);   // 黑方砲位 row=7

        // 绘制楚河汉界
        gc.setFill(Color.rgb(0, 0, 0, 0.6));
        gc.setFont(Font.font("楷体", FontWeight.BOLD, 28));
        double riverY = PADDING + 4.7 * CELL_SIZE;
        gc.fillText("楚 河", PADDING + CELL_SIZE, riverY);
        gc.fillText("汉 界", PADDING + 5.2 * CELL_SIZE, riverY);
    }

    /**
     * 绘制兵/卒位斜线标记
     */
    private void drawPositionMarkers(GraphicsContext gc, int row, Color color) {
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);

        int[] cols = {0, 2, 4, 6, 8};
        for (int col : cols) {
            double x = PADDING + col * CELL_SIZE;
            double y = PADDING + (9 - row) * CELL_SIZE; // 注意：绘制时 row 反转

            // 转换为画布坐标
            double canvasY = PADDING + row * CELL_SIZE;

            // 左上角（col > 0, row < 9）
            if (col > 0 && row < 9) {
                gc.strokeLine(x, canvasY, x - CELL_SIZE / 4, canvasY + CELL_SIZE / 4);
            }
            // 右上角（col < 8, row < 9）
            if (col < 8 && row < 9) {
                gc.strokeLine(x, canvasY, x + CELL_SIZE / 4, canvasY + CELL_SIZE / 4);
            }
            // 左下角（col > 0, row > 0）
            if (col > 0 && row > 0) {
                gc.strokeLine(x, canvasY, x - CELL_SIZE / 4, canvasY - CELL_SIZE / 4);
            }
            // 右下角（col < 8, row > 0）
            if (col < 8 && row > 0) {
                gc.strokeLine(x, canvasY, x + CELL_SIZE / 4, canvasY - CELL_SIZE / 4);
            }
        }
    }

    /**
     * 绘制炮位斜线标记
     */
    private void drawCannonMarkers(GraphicsContext gc, int row, Color color) {
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);

        int[] cols = {1, 7};
        for (int col : cols) {
            double x = PADDING + col * CELL_SIZE;
            double canvasY = PADDING + row * CELL_SIZE;

            // 左上角
            gc.strokeLine(x, canvasY, x - CELL_SIZE / 4, canvasY + CELL_SIZE / 4);
            // 右上角
            gc.strokeLine(x, canvasY, x + CELL_SIZE / 4, canvasY + CELL_SIZE / 4);
            // 左下角
            gc.strokeLine(x, canvasY, x - CELL_SIZE / 4, canvasY - CELL_SIZE / 4);
            // 右下角
            gc.strokeLine(x, canvasY, x + CELL_SIZE / 4, canvasY - CELL_SIZE / 4);
        }
    }

    /**
     * 绘制坐标标注
     */
    private void drawCoordinates(GraphicsContext gc) {
        gc.setFill(Color.rgb(0, 0, 0, 0.5));
        gc.setFont(Font.font("宋体", 12));

        // 列标注（红方视角，从右到左 1-9）
        String[] redCols = {"九", "八", "七", "六", "五", "四", "三", "二", "一"};
        // 列标注（黑方视角，从左到右 1-9）
        String[] blackCols = {"1", "2", "3", "4", "5", "6", "7", "8", "9"};

        for (int i = 0; i < 9; i++) {
            double x = PADDING + i * CELL_SIZE;
            // 红方在上方标注
            gc.fillText(redCols[i], x - 4, PADDING - 10);
            // 黑方在下方标注
            gc.fillText(blackCols[i], x - 4, PADDING + 9 * CELL_SIZE + 18);
        }

        // 行标注（数字）
        for (int row = 0; row <= 9; row++) {
            double y = PADDING + row * CELL_SIZE;
            gc.fillText(String.valueOf(row), PADDING - 22, y + 4);
        }
    }

    /**
     * 绘制棋子
     */
    private void drawPieces(GraphicsContext gc) {
        if (board == null) {
            return;
        }

        for (Piece piece : board.getAllPieces()) {
            Position pos = piece.getPosition();
            double[] coords = boardToCanvas(pos.col(), pos.row());
            double x = coords[0];
            double y = coords[1];

            // 绘制棋子背景圆
            gc.setFill(Color.rgb(255, 248, 220)); // 米黄色
            gc.fillOval(x - PIECE_RADIUS, y - PIECE_RADIUS,
                    PIECE_RADIUS * 2, PIECE_RADIUS * 2);

            // 绘制棋子边框
            if (piece.getColor() == cn.chinesechess.core.Color.RED) {
                gc.setStroke(Color.rgb(180, 0, 0)); // 暗红色
            } else {
                gc.setStroke(Color.rgb(30, 30, 30)); // 近黑色
            }
            gc.setLineWidth(2);
            gc.strokeOval(x - PIECE_RADIUS, y - PIECE_RADIUS,
                    PIECE_RADIUS * 2, PIECE_RADIUS * 2);

            // 绘制内圈
            gc.setLineWidth(1);
            gc.strokeOval(x - PIECE_RADIUS + 4, y - PIECE_RADIUS + 4,
                    PIECE_RADIUS * 2 - 8, PIECE_RADIUS * 2 - 8);

            // 绘制棋子文字
            if (piece.getColor() == cn.chinesechess.core.Color.RED) {
                gc.setFill(Color.rgb(180, 0, 0));
            } else {
                gc.setFill(Color.rgb(30, 30, 30));
            }
            gc.setFont(Font.font("楷体", FontWeight.BOLD, 26));
            String text = piece.getDisplayName();

            // 计算文字居中位置
            double textX = x - 12;
            double textY = y + 9;
            gc.fillText(text, textX, textY);
        }
    }

    /**
     * 绘制选中高亮和合法位置标记
     */
    private void drawHighlights(GraphicsContext gc) {
        if (board == null) {
            return;
        }

        // 绘制选中棋子的高亮外圈
        if (selectedPosition != null) {
            double[] coords = boardToCanvas(selectedPosition.col(), selectedPosition.row());
            double x = coords[0];
            double y = coords[1];

            gc.setStroke(Color.rgb(0, 180, 0));
            gc.setLineWidth(3);
            gc.strokeOval(x - PIECE_RADIUS - 3, y - PIECE_RADIUS - 3,
                    PIECE_RADIUS * 2 + 6, PIECE_RADIUS * 2 + 6);
        }

        // 绘制合法目标位置标记
        if (legalTargets != null) {
            for (Position target : legalTargets) {
                double[] coords = boardToCanvas(target.col(), target.row());
                double x = coords[0];
                double y = coords[1];

                if (board.isOccupied(target)) {
                    // 有棋子的位置：绘制吃子标记（红色外圈）
                    gc.setStroke(Color.rgb(255, 0, 0));
                    gc.setLineWidth(3);
                    gc.strokeOval(x - PIECE_RADIUS - 2, y - PIECE_RADIUS - 2,
                            PIECE_RADIUS * 2 + 4, PIECE_RADIUS * 2 + 4);
                } else {
                    // 空位：绘制绿色圆点
                    gc.setFill(Color.rgb(0, 180, 0, 0.5));
                    gc.fillOval(x - 8, y - 8, 16, 16);
                }
            }
        }
    }

    /**
     * 将画布坐标转换为棋盘位置
     * @param canvasX 画布 X 坐标
     * @param canvasY 画布 Y 坐标
     * @return 棋盘位置，如果点击不在有效范围内返回 null
     */
    public Position canvasToPosition(double canvasX, double canvasY) {
        // 转换为棋盘网格坐标
        int col = (int) Math.round((canvasX - PADDING) / CELL_SIZE);
        int row = (int) Math.round((canvasY - PADDING) / CELL_SIZE);

        // 根据翻转状态转换坐标
        if (flipped) {
            col = 8 - col;
            row = 9 - row;
        }

        Position pos = new Position(col, row);
        return pos.isValid() ? pos : null;
    }

    /**
     * 获取 Canvas
     * @return Canvas 对象
     */
    public Canvas getCanvas() {
        return canvas;
    }
}
