package cn.chinesechess.ui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import cn.chinesechess.ai.DifficultyLevel;
import cn.chinesechess.ai.EngineManager;
import cn.chinesechess.core.GameMode;
import cn.chinesechess.core.engine.GameEngine;
import cn.chinesechess.core.engine.IGameEngine;

import java.util.Optional;

/**
 * 中国象棋 JavaFX 应用入口
 * <p>创建主窗口，组装棋盘视图、控制面板和菜单栏。</p>
 */
public class ChineseChessApp extends Application {

    /** 引擎管理器 */
    private EngineManager engineManager;

    @Override
    public void start(Stage primaryStage) {
        // 初始化引擎管理器
        engineManager = new EngineManager();

        // 创建游戏引擎
        IGameEngine gameEngine = new GameEngine();

        // 创建棋盘视图
        BoardView boardView = new BoardView();

        // 创建游戏控制器
        GameController controller = new GameController(gameEngine, boardView, engineManager);

        // 创建布局
        BorderPane root = new BorderPane();
        root.setCenter(boardView);

        // 创建菜单栏
        MenuBar menuBar = createMenuBar(controller, primaryStage);
        root.setTop(menuBar);

        // 创建工具栏
        ToolBar toolBar = createToolBar(controller, primaryStage);

        // 菜单栏和工具栏垂直排列
        javafx.scene.layout.VBox topBox = new javafx.scene.layout.VBox(menuBar, toolBar);
        root.setTop(topBox);

        // 创建状态栏
        Label statusLabel = controller.getStatusLabel();
        ToolBar statusBar = new ToolBar(statusLabel);
        root.setBottom(statusBar);

        // 创建场景
        Scene scene = new Scene(root);
        // 加载 CSS 样式
        scene.getStylesheets().add(
                getClass().getResource("/css/style.css").toExternalForm()
        );

        // 注册键盘快捷键
        registerKeyboardShortcuts(scene, controller, primaryStage);

        primaryStage.setTitle("中国象棋");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();

        // 显示模式选择对话框并开始游戏
        showNewGameDialog(controller, primaryStage);
    }

    /**
     * 创建菜单栏
     */
    private MenuBar createMenuBar(GameController controller, Stage owner) {
        // 游戏菜单
        Menu gameMenu = new Menu("游戏(_G)");
        MenuItem newGameItem = new MenuItem("新局(_N)");
        newGameItem.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN));
        newGameItem.setOnAction(e -> showNewGameDialog(controller, owner));

        MenuItem undoItem = new MenuItem("悔棋(_U)");
        undoItem.setAccelerator(new KeyCodeCombination(KeyCode.Z, KeyCombination.CONTROL_DOWN));
        undoItem.setOnAction(e -> controller.undo());

        MenuItem resignItem = new MenuItem("认输(_R)");
        resignItem.setOnAction(e -> controller.resign());

        MenuItem drawItem = new MenuItem("求和(_D)");
        drawItem.setOnAction(e -> controller.offerDraw());

        gameMenu.getItems().addAll(newGameItem, new SeparatorMenuItem(),
                undoItem, resignItem, drawItem, new SeparatorMenuItem());

        MenuItem exitItem = new MenuItem("退出(_X)");
        exitItem.setOnAction(e -> owner.close());
        gameMenu.getItems().add(exitItem);

        // 文件菜单
        Menu fileMenu = new Menu("文件(_F)");
        MenuItem saveItem = new MenuItem("保存棋谱(_S)");
        saveItem.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN));
        saveItem.setOnAction(e -> controller.saveGameRecord(owner));

        MenuItem loadItem = new MenuItem("加载棋谱(_O)");
        loadItem.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN));
        loadItem.setOnAction(e -> controller.loadGameRecord(owner));

        fileMenu.getItems().addAll(saveItem, loadItem);

        // 视图菜单
        Menu viewMenu = new Menu("视图(_V)");

        CheckMenuItem flipItem = new CheckMenuItem("翻转棋盘(_F)");
        flipItem.setOnAction(e -> controller.setFlipped(flipItem.isSelected()));

        MenuItem rotateItem = new MenuItem("旋转180度(_R)");
        rotateItem.setOnAction(e -> {
            controller.flipBoard();
            flipItem.setSelected(controller.isBoardFlipped());
        });

        viewMenu.getItems().addAll(flipItem, rotateItem);

        // 引擎菜单
        Menu engineMenu = new Menu("引擎(_E)");
        MenuItem engineSettingsItem = new MenuItem("引擎设置(_S)...");
        engineSettingsItem.setOnAction(e -> showEngineSettings(owner));
        engineMenu.getItems().add(engineSettingsItem);

        // 帮助菜单
        Menu helpMenu = new Menu("帮助(_H)");
        MenuItem aboutItem = new MenuItem("关于(_A)");
        aboutItem.setOnAction(e -> showAboutDialog(owner));
        helpMenu.getItems().add(aboutItem);

        return new MenuBar(gameMenu, fileMenu, viewMenu, engineMenu, helpMenu);
    }

    /**
     * 注册键盘快捷键
     */
    private void registerKeyboardShortcuts(Scene scene, GameController controller, Stage owner) {
        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN),
                () -> showNewGameDialog(controller, owner)
        );
        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.Z, KeyCombination.CONTROL_DOWN),
                () -> controller.undo()
        );
        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN),
                () -> controller.saveGameRecord(owner)
        );
        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN),
                () -> controller.loadGameRecord(owner)
        );
        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.ESCAPE),
                () -> controller.cancelSelection()
        );
    }

    /**
     * 显示关于对话框
     */
    private void showAboutDialog(Stage owner) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.initOwner(owner);
        alert.setTitle("关于");
        alert.setHeaderText("中国象棋");
        alert.setContentText(
                "版本：1.0.0\n" +
                "作者：Chinese Chess Team\n" +
                "技术栈：Java 21 + JavaFX\n\n" +
                "支持人机对战和双人对战模式\n" +
                "AI 引擎基于 Alpha-Beta 剪枝算法"
        );
        alert.showAndWait();
    }

    /**
     * 显示新游戏模式选择对话框
     */
    private void showNewGameDialog(GameController controller, Stage owner) {
        GameModeDialog dialog = new GameModeDialog();
        dialog.initOwner(owner);
        Optional<GameModeDialog.GameModeConfig> result = dialog.showAndWait();

        if (result.isPresent()) {
            GameModeDialog.GameModeConfig config = result.get();
            controller.configureGame(config.mode(), config.difficulty(), config.firstHand());
        }
        // 如果取消对话框，直接开始默认 PVP 游戏
        controller.newGame();
    }

    /**
     * 显示引擎设置对话框
     */
    private void showEngineSettings(Stage owner) {
        EngineSettingsDialog dialog = new EngineSettingsDialog(engineManager, owner);
        dialog.initOwner(owner);
        dialog.showAndWait();
    }

    /**
     * 创建工具栏
     */
    private ToolBar createToolBar(GameController controller, Stage owner) {
        Button newGameBtn = new Button("新局");
        newGameBtn.setOnAction(e -> showNewGameDialog(controller, owner));

        Button undoBtn = new Button("悔棋");
        undoBtn.setOnAction(e -> controller.undo());

        Button resignBtn = new Button("认输");
        resignBtn.setOnAction(e -> controller.resign());

        Button drawBtn = new Button("求和");
        drawBtn.setOnAction(e -> controller.offerDraw());

        Button saveBtn = new Button("保存棋谱");
        saveBtn.setOnAction(e -> controller.saveGameRecord(owner));

        Button loadBtn = new Button("加载棋谱");
        loadBtn.setOnAction(e -> controller.loadGameRecord(owner));

        return new ToolBar(newGameBtn, new Separator(), undoBtn, resignBtn, drawBtn,
                new Separator(), saveBtn, loadBtn);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
