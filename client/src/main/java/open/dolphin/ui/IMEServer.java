package open.dolphin.ui;

import open.dolphin.client.ClientContext;
import open.dolphin.client.ClientContextStub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class IMEServer {
    private static final String JAPANESE = "J";
    private static final String ROMAN = "R";
    private static final String ABC = "A";
    private static final String US = "U";
    private static final String US_EXT = "X";
    private static final BlockingQueue<String> RESPONSE_QUEUE = new ArrayBlockingQueue<>(1);
    private static final Logger logger = LoggerFactory.getLogger(IMEServer.class);
    private static Process PROCESS;
    private static OutputStream OUTPUT;

    public IMEServer() {
    }

    public static boolean start() {
        // 外部 TISServer ファイルがあるかどうか
        String tisDir = System.getProperty("user.dir");
        ClientContextStub stub = ClientContext.getClientContextStub();
        if (stub != null) {
            tisDir = stub.getBaseDirectory(); // jar の場合 /Resources が返る
        } else {
            // 単独でテストするとき client が付かないので付ける
            if (!tisDir.contains("client")) {
                tisDir = tisDir + "/client";
            }
        }
        String tisServer = tisDir + "/TISServer";

        if (Files.exists(Paths.get(tisServer))) {
            // 外部 TISServer
            try {
                PROCESS = new ProcessBuilder(tisServer).start();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        } else {
            // 内部 TISServer
            var command = ProcessHandle.current().info().command().orElseThrow();
            if (command.toLowerCase().contains("java")) {
                logger.info("called from java");
                try {
                    PROCESS = new ProcessBuilder(
                        command, "-cp", System.getProperty("java.class.path"), "open.dolphin.helper.TISServer"
                    ).start();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            } else {
                IO.println("called from application: " + command);
                Path path = Paths.get(command).getParent();
                var tisServerPath = path.resolve("tis-server");
                logger.info("starting tis-server: " + tisServerPath);
                try {
                    PROCESS = new ProcessBuilder(tisServerPath.toString()).start();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

        }

        OUTPUT = PROCESS.getOutputStream();

        // Thread to receive OK response
        Thread.ofPlatform().start(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(PROCESS.getInputStream()))) {
                while (true) {
                    String line = reader.readLine();
                    if (line != null) {
                        // offer server's response to res queue
                        RESPONSE_QUEUE.offer(line);
                        if (!line.equals("OK")) {
                            logger.info("server response: {}", line);
                        }
                    }
                }
            } catch (IOException e) {
                logger.info("stdin reader closed");
            }
        });
        return true;
    }

    public static void stop() {
        if (PROCESS != null) {
            PROCESS.destroy();
        }
    }

    public static void selectJapanese() {
        select(JAPANESE);
    }

    public static void selectRoman() {
        select(ROMAN);
    }

    public static void selectABC() {
        select(ABC);
    }

    public static void selectUS() {
        select(US);
    }

    public static void selectUSExt() {
        select(US_EXT);
    }

    private static void select(String lang) {
        try {
            //logger.info("selecting language: {}", lang);
            OUTPUT.write((lang + "\n").getBytes());
            OUTPUT.flush();
            waitForResult();
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
    }

    private static void waitForResult() {
        try {
            // receive server response through res queue
            if (RESPONSE_QUEUE.poll(1, TimeUnit.SECONDS) == null) {
                throw new InterruptedException("timeout");
            }
            //logger.info("server responded");

        } catch (InterruptedException ex) {
            logger.info("timeout");
            // restart server
            stop();
            start();
        }
    }

    static void main() {
        if (IMEServer.start()) {
            selectJapanese();
        }
    }
}
