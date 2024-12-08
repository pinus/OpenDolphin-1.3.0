package open.dolphin.ui;

import open.dolphin.client.ClientContext;
import open.dolphin.client.ClientContextStub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * IMEServer is responsible for managing interactions with the TISServer for
 * input method selection, specifically handling Japanese and Roman input modes.
 * It starts and stops the TISServer process, sends selection commands, and handles
 * server responses asynchronously.
 */
public class IMEServer {
    private final byte[] JAPANESE = "J\n".getBytes();
    private final byte[] ROMAN = "R\n".getBytes();
    private final String tisServer;
    private Process process;
    private OutputStream output;
    private final BlockingQueue<String> res = new ArrayBlockingQueue<>(1);
    private final Logger logger = LoggerFactory.getLogger(IMEServer.class);

    public IMEServer() {
        // TISServer のある directory を調べる
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
        tisServer = tisDir + "/TISServer";
    }

    /**
     * Initiates the start of the TIS server by first checking for the server's existence
     * and then launching the server process. This method sets up a virtual thread to
     * listen for output from the server process and queues any response received.
     * It also sets up a shutdown hook to ensure the server process is terminated
     * gracefully upon application termination.
     *
     * @return true if the TIS server was successfully started, false otherwise.
     */
    public boolean start() {
        // TISServer 実体がなければ false
        if (!Files.exists(Paths.get(tisServer))) {
            return false;
        }

        try {
            // TISServer 起動
            process = new ProcessBuilder(tisServer).start();
            output = process.getOutputStream();
            // Thread to receive OK response
            Thread.ofVirtual().start(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    while (true) {
                        String line = reader.readLine();
                        if (line != null) {
                            // offer server's OK response to res queue
                            if (line.equals("OK")) {
                                res.offer(line);
                            } else {
                                logger.info("server response: " + line);
                            }
                        }
                    }
                } catch (IOException e) {
                    logger.info("stdin reader closed");
                }
            });
            // 終了時 destroy する
            Runtime.getRuntime().addShutdownHook(new Thread(this::stop));

        } catch (IOException e) {
            e.printStackTrace(System.err);
            return false;
        }
        return true;
    }

    /**
     * Stops the server process associated with the IMEServer instance.
     * <p>
     * If the server process is currently running, this method will destroy
     * the process, effectively terminating it. It ensures that resources
     * associated with the process are released. This method should be called
     * when the server is no longer needed or before shutting down the
     * application to ensure a proper cleanup of system resources.
     */
    public void stop() {
        if (process != null) {
            process.destroy();
        }
    }

    /**
     * Selects the Japanese input method on the IMEServer instance.
     */
    public void selectJapanese() {
        select(JAPANESE);
    }

    /**
     * Selects the Roman input method on the IMEServer instance.
     */
    public void selectRoman() {
        select(ROMAN);
    }

    /**
     * Sends the specified language byte array to the output stream, flushes the stream,
     * and waits for a result from the server.
     *
     * @param lang a byte array representing the language selection to be sent to the server.
     */
    private void select(byte[] lang) {
        try {
            output.write(lang);
            output.flush();
            waitForResult();
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
    }

    /**
     * Waits for a response from the server through a specified response queue.
     * <p>
     * This method attempts to retrieve a response from the server, which is
     * expected to be queued in the response queue (res). It waits for a maximum
     * of 1 second to receive a response. If no response is received within this
     * period, it logs a timeout event, stops the server, and attempts to restart
     * it in order to ensure ongoing communication.
     * <p>
     * The server response and timeout events are logged using an injected logger.
     * Throws an InterruptedException in case of a timeout, which is caught and
     * handled by logging and restarting server processes.
     */
    private void waitForResult() {
        try {
            // receive server response through res queue
            if (res.poll(1, TimeUnit.SECONDS) == null) {
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

    public static void main(String[] argv) {
        IMEServer server = new IMEServer();
        if (server.start()) {
            server.selectJapanese();
        }
    }
}
