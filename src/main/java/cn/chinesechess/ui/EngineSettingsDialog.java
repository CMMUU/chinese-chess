package cn.chinesechess.ui;

import cn.chinesechess.ai.ChessEngine;
import cn.chinesechess.ai.EngineManager;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * 引擎设置对话框
 * <p>管理象棋引擎：添加、移除、设置默认引擎。</p>
 */
public class EngineSettingsDialog extends Dialog<EngineManager> {

    private final EngineManager engineManager;
    private final ListView<ChessEngine> engineListView;
    private final Stage owner;

    public EngineSettingsDialog(EngineManager engineManager, Stage owner) {
        this.engineManager = engineManager;
        this.owner = owner;

        setTitle("引擎设置");
        setHeaderText("管理象棋引擎");

        // 设置为非模态对话框
        initModality(Modality.NONE);

        // 引擎列表
        engineListView = new ListView<>();
        engineListView.getItems().addAll(engineManager.getEngines());

        // 选中当前引擎
        ChessEngine current = engineManager.getCurrentEngine();
        if (current != null) {
            engineListView.getSelectionModel().select(current);
        }

        // 按钮
        Button addButton = new Button("添加引擎...");
        addButton.setOnAction(e -> addEngine());

        Button removeButton = new Button("移除");
        removeButton.setOnAction(e -> removeEngine());

        Button refreshButton = new Button("刷新");
        refreshButton.setOnAction(e -> refreshEngines());

        Button downloadButton = new Button("下载引擎...");
        downloadButton.setOnAction(e -> downloadEngine());

        // 布局
        VBox buttonBox = new VBox(8, addButton, removeButton, refreshButton, downloadButton);
        buttonBox.setPadding(new Insets(0, 0, 0, 10));

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        Label infoLabel = new Label("已安装的引擎：");
        grid.add(infoLabel, 0, 0);

        grid.add(engineListView, 0, 1);
        grid.add(buttonBox, 1, 1);

        // 引擎详情
        Label detailLabel = new Label("引擎详情：");
        grid.add(detailLabel, 0, 2);

        TextArea detailArea = new TextArea();
        detailArea.setEditable(false);
        detailArea.setPrefRowCount(4);
        grid.add(detailArea, 0, 3, 2, 1);

        // 显示选中引擎的详情
        engineListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                StringBuilder sb = new StringBuilder();
                sb.append("名称: ").append(newVal.getName()).append("\n");
                sb.append("版本: ").append(newVal.getVersion()).append("\n");
                sb.append("类型: ").append(newVal.getType()).append("\n");
                sb.append("描述: ").append(newVal.getDescription()).append("\n");
                if (newVal.getPath() != null) {
                    sb.append("路径: ").append(newVal.getPath()).append("\n");
                }
                detailArea.setText(sb.toString());

                // 设置为默认引擎
                engineManager.setCurrentEngine(newVal);
            }
        });

        getDialogPane().setContent(grid);

        // 按钮
        ButtonType closeType = new ButtonType("关闭", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().add(closeType);
    }

    /**
     * 添加引擎
     */
    private void addEngine() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("选择引擎程序");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("可执行文件", "*.exe", "*", "*.app"),
                new FileChooser.ExtensionFilter("所有文件", "*.*")
        );

        // 设置初始目录为引擎目录
        Path engineDir = engineManager.getEngineDir();
        if (engineDir != null) {
            File dir = engineDir.toFile();
            if (dir.exists()) {
                chooser.setInitialDirectory(dir);
            }
        }

        File file = chooser.showOpenDialog(owner);
        if (file != null) {
            Path path = Paths.get(file.getAbsolutePath());
            engineManager.addUciEngine(path);
            refreshList();
        }
    }

    /**
     * 移除引擎
     */
    private void removeEngine() {
        ChessEngine selected = engineListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }

        if (selected.getType() == ChessEngine.EngineType.BUILTIN) {
            showAlert(Alert.AlertType.WARNING, "无法移除", "内置引擎不能被移除。");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("确认移除");
        alert.setHeaderText(null);
        alert.setContentText("确定要移除引擎 \"" + selected.getName() + "\" 吗？");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                engineManager.removeEngine(selected);
                refreshList();
            }
        });
    }

    /**
     * 刷新引擎列表
     */
    private void refreshEngines() {
        engineManager.refresh();
        refreshList();
    }

    /**
     * 刷新列表显示
     */
    private void refreshList() {
        engineListView.getItems().clear();
        engineListView.getItems().addAll(engineManager.getEngines());

        ChessEngine current = engineManager.getCurrentEngine();
        if (current != null) {
            engineListView.getSelectionModel().select(current);
        }
    }

    /**
     * 下载引擎
     */
    private void downloadEngine() {
        // 显示下载说明
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("下载引擎");
        alert.setHeaderText("下载皮卡鱼象棋引擎");
        alert.setContentText(
                "推荐下载：皮卡鱼象棋引擎 (https://github.com/adam888/pikafish)\n\n" +
                        "下载步骤：\n" +
                        "1. 访问 GitHub 下载最新版本\n" +
                        "2. 解压到 ~/.chinese-chess/engines/ 目录\n" +
                        "3. 点击「刷新」按钮\n\n" +
                        "引擎目录：" + engineManager.getEngineDir().toString()
        );
        alert.showAndWait();

        // 打开引擎目录
        try {
            java.awt.Desktop.getDesktop().open(engineManager.getEngineDir().toFile());
        } catch (Exception e) {
            // 忽略
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
