package cn.chinesechess.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 棋谱管理器
 * <p>提供棋谱的保存和加载功能，使用 Gson 进行 JSON 序列化。
 * 存储目录：~/.chinese-chess/records/</p>
 */
public class GameRecordManager {

    /** 默认存储目录 */
    private static final Path DEFAULT_DIR = Path.of(
            System.getProperty("user.home"), ".chinese-chess", "records"
    );

    /** Gson 实例 */
    private final Gson gson;

    public GameRecordManager() {
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
    }

    /**
     * 保存棋谱到文件
     * @param record 棋谱记录
     * @param filePath 文件路径
     * @throws IOException 文件写入失败
     */
    public void saveRecord(GameRecord record, Path filePath) throws IOException {
        // 确保父目录存在
        if (filePath.getParent() != null) {
            Files.createDirectories(filePath.getParent());
        }

        String json = gson.toJson(record);
        Files.writeString(filePath, json, StandardCharsets.UTF_8);
    }

    /**
     * 从文件加载棋谱
     * @param filePath 文件路径
     * @return 棋谱记录
     * @throws IOException 文件读取失败或 JSON 解析失败
     */
    public GameRecord loadRecord(Path filePath) throws IOException {
        String json = Files.readString(filePath, StandardCharsets.UTF_8);
        return gson.fromJson(json, GameRecord.class);
    }

    /**
     * 保存棋谱到默认目录
     * @param record 棋谱记录
     * @param filename 文件名（不含路径）
     * @return 实际保存的文件路径
     * @throws IOException 文件写入失败
     */
    public Path saveToDefaultDir(GameRecord record, String filename) throws IOException {
        Path filePath = DEFAULT_DIR.resolve(filename);
        saveRecord(record, filePath);
        return filePath;
    }

    /**
     * 获取默认存储目录
     * @return 默认目录路径
     */
    public Path getDefaultDir() {
        return DEFAULT_DIR;
    }
}
