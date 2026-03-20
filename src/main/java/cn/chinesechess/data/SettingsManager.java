package cn.chinesechess.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 设置管理器
 * <p>提供应用设置的保存和加载功能。
 * 配置文件路径：~/.chinese-chess/settings.json</p>
 */
public class SettingsManager {

    /** 配置文件路径 */
    private static final Path SETTINGS_PATH = Path.of(
            System.getProperty("user.home"), ".chinese-chess", "settings.json"
    );

    /** Gson 实例 */
    private final Gson gson;

    public SettingsManager() {
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
    }

    /**
     * 加载应用设置
     * <p>如果配置文件不存在或解析失败，返回默认设置。</p>
     * @return 应用设置
     */
    public AppSettings loadSettings() {
        if (!Files.exists(SETTINGS_PATH)) {
            return AppSettings.defaults();
        }

        try {
            String json = Files.readString(SETTINGS_PATH, StandardCharsets.UTF_8);
            AppSettings settings = gson.fromJson(json, AppSettings.class);
            return settings != null ? settings : AppSettings.defaults();
        } catch (IOException e) {
            // 读取失败时返回默认设置
            return AppSettings.defaults();
        }
    }

    /**
     * 保存应用设置
     * @param settings 应用设置
     * @throws IOException 文件写入失败
     */
    public void saveSettings(AppSettings settings) throws IOException {
        // 确保目录存在
        Files.createDirectories(SETTINGS_PATH.getParent());

        String json = gson.toJson(settings);
        Files.writeString(SETTINGS_PATH, json, StandardCharsets.UTF_8);
    }

    /**
     * 重置为默认设置
     * @return 默认设置
     * @throws IOException 文件写入失败
     */
    public AppSettings resetToDefaults() throws IOException {
        AppSettings defaults = AppSettings.defaults();
        saveSettings(defaults);
        return defaults;
    }
}
