package cn.chinesechess.ai;

import cn.chinesechess.core.Color;

import java.io.*;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * UCI 象棋引擎适配器
 * <p>通过 UCI 协议与外部引擎通信。</p>
 */
public class UciEngine implements ChessEngine {

    private final Path path;
    private final String name;
    private final String version;

    private Process process;
    private BufferedWriter writer;
    private BufferedReader reader;

    private int depth = 3;
    private Color color = Color.RED;
    private String fen;
    private final List<String> moves = new CopyOnWriteArrayList<>();

    private final AtomicBoolean thinking = new AtomicBoolean(false);
    private final AtomicReference<String> bestMove = new AtomicReference<>();

    public UciEngine(Path path) {
        this.path = path;
        this.name = path.getFileName().toString();
        this.version = "未知";
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public String getDescription() {
        return "UCI 引擎 - " + path.toString();
    }

    @Override
    public EngineType getType() {
        return EngineType.UCI;
    }

    @Override
    public Path getPath() {
        return path;
    }

    @Override
    public void initialize() {
        try {
            ProcessBuilder pb = new ProcessBuilder(path.toString());
            pb.redirectErrorStream(true);
            process = pb.start();

            writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            // 发送 UCI 命令
            sendCommand("uci");

            // 读取引擎名称
            String line;
            while ((line = readLine()) != null) {
                if (line.startsWith("id name ")) {
                    // 可以解析版本
                    break;
                }
                if (line.equals("uciok")) {
                    break;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void shutdown() {
        try {
            if (thinking.get()) {
                sendCommand("stop");
            }
            sendCommand("quit");
            if (process != null) {
                process.destroy();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setDepth(int depth) {
        this.depth = depth;
    }

    @Override
    public void setPosition(Color color) {
        this.color = color;
    }

    @Override
    public void setFen(String fen) {
        this.fen = fen;
    }

    @Override
    public void setMoves(List<String> moves) {
        this.moves.clear();
        this.moves.addAll(moves);
    }

    @Override
    public void startThinking() {
        if (thinking.get()) {
            return;
        }
        thinking.set(true);

        Thread readerThread = new Thread(() -> {
            try {
                String line;
                while ((line = readLine()) != null) {
                    if (line.startsWith("bestmove ")) {
                        String move = line.substring(9).trim().split(" ")[0];
                        bestMove.set(move);
                        thinking.set(false);
                        break;
                    }
                }
            } catch (Exception e) {
                thinking.set(false);
            }
        }, "UciReader");
        readerThread.start();

        try {
            // 设置局面
            if (fen != null) {
                sendCommand("position fen " + fen);
            } else {
                sendCommand("position startpos");
            }

            // 添加走棋历史
            if (!moves.isEmpty()) {
                sendCommand("moves " + String.join(" ", moves));
            }

            // 开始思考
            sendCommand("go depth " + depth);

        } catch (IOException e) {
            thinking.set(false);
            e.printStackTrace();
        }
    }

    @Override
    public void stopThinking() {
        if (thinking.get()) {
            try {
                sendCommand("stop");
            } catch (IOException e) {
                e.printStackTrace();
            }
            thinking.set(false);
        }
    }

    @Override
    public String getBestMove() {
        return bestMove.get();
    }

    @Override
    public boolean isThinking() {
        return thinking.get();
    }

    @Override
    public boolean isAvailable() {
        return process != null && process.isAlive();
    }

    private void sendCommand(String cmd) throws IOException {
        writer.write(cmd);
        writer.newLine();
        writer.flush();
    }

    private String readLine() {
        try {
            return reader.readLine();
        } catch (IOException e) {
            return null;
        }
    }
}
