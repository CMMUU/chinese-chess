package cn.chinesechess.ui;

import cn.chinesechess.ai.DifficultyLevel;
import cn.chinesechess.core.Color;
import cn.chinesechess.core.GameMode;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;

/**
 * 游戏模式选择对话框
 * <p>在新游戏开始时弹出，让用户选择对战模式（PVP/PVE）、先手方和 AI 难度。</p>
 */
public class GameModeDialog extends Dialog<GameModeDialog.GameModeConfig> {

    /** 游戏模式配置结果 */
    public record GameModeConfig(GameMode mode, DifficultyLevel difficulty, Color firstHand) {}

    /** 游戏模式选择 */
    private final ToggleGroup modeGroup;

    /** 先手方选择 */
    private final ToggleGroup firstHandGroup;

    /** 难度选择 */
    private final ComboBox<DifficultyLevel> difficultyCombo;

    /** 玩家颜色选择（PVE模式用） */
    private final ToggleGroup playerColorGroup;

    public GameModeDialog() {
        setTitle("新游戏设置");
        setHeaderText("请选择游戏模式");

        // 设置为非模态对话框，允许同时操作菜单栏
        initModality(Modality.NONE);

        // 创建模式选择
        modeGroup = new ToggleGroup();
        RadioButton pvpRadio = new RadioButton("双人对战");
        pvpRadio.setToggleGroup(modeGroup);
        pvpRadio.setUserData(GameMode.PVP);
        pvpRadio.setSelected(true);

        RadioButton pveRadio = new RadioButton("人机对战");
        pveRadio.setToggleGroup(modeGroup);
        pveRadio.setUserData(GameMode.PVE);

        // 先手方选择
        firstHandGroup = new ToggleGroup();
        RadioButton redFirstRadio = new RadioButton("红方（先手）");
        redFirstRadio.setToggleGroup(firstHandGroup);
        redFirstRadio.setUserData(Color.RED);
        redFirstRadio.setSelected(true);

        RadioButton blackFirstRadio = new RadioButton("黑方（先手）");
        blackFirstRadio.setToggleGroup(firstHandGroup);
        blackFirstRadio.setUserData(Color.BLACK);

        // 玩家颜色选择（PVE模式）
        playerColorGroup = new ToggleGroup();
        RadioButton playRedRadio = new RadioButton("执红");
        playRedRadio.setToggleGroup(playerColorGroup);
        playRedRadio.setUserData(Color.RED);
        playRedRadio.setSelected(true);

        RadioButton playBlackRadio = new RadioButton("执黑");
        playBlackRadio.setToggleGroup(playerColorGroup);
        playBlackRadio.setUserData(Color.BLACK);

        // 难度选择
        difficultyCombo = new ComboBox<>();
        difficultyCombo.getItems().addAll(DifficultyLevel.values());
        difficultyCombo.setValue(DifficultyLevel.MEDIUM);
        difficultyCombo.setDisable(true);

        // PVE 模式下启用难度和玩家颜色选择
        pveRadio.selectedProperty().addListener((obs, oldVal, newVal) -> {
            difficultyCombo.setDisable(!newVal);
            playRedRadio.setDisable(!newVal);
            playBlackRadio.setDisable(!newVal);
        });

        // 布局
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(12);
        grid.setPadding(new Insets(20));

        grid.add(new Label("对战模式："), 0, 0);
        VBox modeBox = new VBox(8, pvpRadio, pveRadio);
        grid.add(modeBox, 1, 0);

        grid.add(new Label("先手方："), 0, 1);
        VBox firstHandBox = new VBox(8, redFirstRadio, blackFirstRadio);
        grid.add(firstHandBox, 1, 1);

        grid.add(new Label("AI 难度："), 0, 2);
        grid.add(difficultyCombo, 1, 2);

        grid.add(new Label("玩家执子："), 0, 3);
        VBox playerColorBox = new VBox(8, playRedRadio, playBlackRadio);
        grid.add(playerColorBox, 1, 3);
        // 默认禁用玩家颜色选择
        playRedRadio.setDisable(true);
        playBlackRadio.setDisable(true);

        getDialogPane().setContent(grid);

        // 按钮
        ButtonType startType = new ButtonType("开始游戏", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(startType, ButtonType.CANCEL);

        // 结果转换
        setResultConverter(buttonType -> {
            if (buttonType == startType) {
                GameMode mode = (GameMode) modeGroup.getSelectedToggle().getUserData();
                Color firstHand = (Color) firstHandGroup.getSelectedToggle().getUserData();
                DifficultyLevel difficulty = (mode == GameMode.PVE)
                        ? difficultyCombo.getValue()
                        : DifficultyLevel.MEDIUM;

                // PVE 模式下，玩家颜色 = 先手方（玩家先手）或 对手方（玩家后手）
                // 这里返回先手方，GameController会根据mode决定如何处理
                return new GameModeConfig(mode, difficulty, firstHand);
            }
            return null;
        });
    }
}
