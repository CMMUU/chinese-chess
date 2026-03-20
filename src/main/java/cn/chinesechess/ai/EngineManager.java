package cn.chinesechess.ai;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * 象棋引擎管理器
 * <p>管理所有可用引擎，支持添加、移除、下载、更新引擎。</p>
 */
public class EngineManager {

    /** 引擎列表 */
    private final List<ChessEngine> engines = new CopyOnWriteArrayList<>();

    /** 当前选中的引擎 */
    private ChessEngine currentEngine;

    /** 引擎目录 */
    private final Path engineDir;

    /** 默认引擎 */
    private static final String DEFAULT_ENGINE_NAME = "内置引擎";

    public EngineManager() {
        // 创建引擎目录
        String userHome = System.getProperty("user.home");
        this.engineDir = Paths.get(userHome, ".chinese-chess", "engines");
        try {
            Files.createDirectories(engineDir);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 添加默认内置引擎
        addBuiltInEngine();

        // 扫描已安装的外部引擎
        scanExternalEngines();
    }

    /**
     * 添加内置引擎
     */
    private void addBuiltInEngine() {
        MinimaxEngine minimax = new MinimaxEngine();
        BuiltInEngine builtIn = new BuiltInEngine(DEFAULT_ENGINE_NAME, "1.0.0", minimax);
        engines.add(builtIn);
        currentEngine = builtIn;
    }

    /**
     * 扫描外部引擎目录
     */
    private void scanExternalEngines() {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(engineDir)) {
            for (Path path : stream) {
                if (Files.isExecutable(path)) {
                    String name = path.getFileName().toString();
                    // 跳过常见非引擎文件
                    if (name.contains(".")) {
                        String ext = name.substring(name.lastIndexOf('.') + 1).toLowerCase();
                        if (ext.equals("exe") || ext.equals("") || ext.equals("app")) {
                            addUciEngine(path);
                        }
                    }
                }
            }
        } catch (IOException e) {
            // 忽略扫描错误
        }
    }

    /**
     * 添加 UCI 引擎
     */
    public void addUciEngine(Path path) {
        // 检查是否已存在
        for (ChessEngine engine : engines) {
            if (engine.getPath() != null && engine.getPath().equals(path)) {
                return;
            }
        }

        try {
            UciEngine uci = new UciEngine(path);
            uci.initialize();
            engines.add(uci);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 移除引擎
     */
    public void removeEngine(ChessEngine engine) {
        if (engine.getType() == ChessEngine.EngineType.BUILTIN) {
            return; // 不能移除内置引擎
        }
        engine.shutdown();
        engines.remove(engine);

        if (currentEngine == engine) {
            currentEngine = getBuiltInEngine();
        }
    }

    /**
     * 获取引擎列表
     */
    public List<ChessEngine> getEngines() {
        return new ArrayList<>(engines);
    }

    /**
     * 获取当前引擎
     */
    public ChessEngine getCurrentEngine() {
        return currentEngine;
    }

    /**
     * 设置当前引擎
     */
    public void setCurrentEngine(ChessEngine engine) {
        if (engine != null && engine.isAvailable()) {
            this.currentEngine = engine;
        }
    }

    /**
     * 获取内置引擎
     */
    public ChessEngine getBuiltInEngine() {
        for (ChessEngine engine : engines) {
            if (engine.getType() == ChessEngine.EngineType.BUILTIN) {
                return engine;
            }
        }
        return null;
    }

    /**
     * 获取引擎目录
     */
    public Path getEngineDir() {
        return engineDir;
    }

    /**
     * 关闭所有引擎
     */
    public void shutdown() {
        for (ChessEngine engine : engines) {
            engine.shutdown();
        }
    }

    /**
     * 刷新引擎列表
     */
    public void refresh() {
        scanExternalEngines();
    }
}
