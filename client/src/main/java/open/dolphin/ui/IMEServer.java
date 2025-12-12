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
import java.nio.file.Paths;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/// The IMEServer class is responsible for managing an Input Method Editor (IME) server
/// that supports selection between Japanese and Roman input methods. It initializes
/// and controls the lifecycle of the TIS server process used for handling input method
/// commands and responses.
///
/// The class includes functionality to:
/// - Start and stop the TIS server process.
/// - Select between different language input modes.
/// - Handle server responses and timeout events reliably.
public class IMEServer {
    private final byte[] JAPANESE = "J\n".getBytes();
    private final byte[] ROMAN = "R\n".getBytes();
    private final byte[] ABC = "A\n".getBytes();
    private final byte[] US = "U\n".getBytes();
    private final byte[] US_EXT = "X\n".getBytes();
    private final String tisServer;
    private Process process;
    private OutputStream output;
    private final BlockingQueue<String> res = new ArrayBlockingQueue<>(1);
    private final Logger logger = LoggerFactory.getLogger(IMEServer.class);

    /// Constructs an instance of the IMEServer class.
    ///
    /// This constructor initializes the TIS server directory path based on the current
    /// working directory or the client context stub if available. If the application
    /// is being tested standalone and the directory does not include "client", it appends
    /// "/client" to the path to ensure proper server access.
    ///
    /// Additionally, the constructor sets up a shutdown hook that guarantees the server
    /// process is destroyed when the application exits. This ensures the cleanup of
    /// resources associated with the server.
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

        // 終了時 destroy する
        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
    }

    /// Starts the TISServer process if the server executable exists, setting up
    /// a background thread to receive server responses.
    /// This method checks for the existence of the TISServer executable's path.
    /// If found, it starts the server process and establishes an output stream
    /// to communicate with the server. A background thread is created to read
    /// and handle the server's responses continuously.
    /// If the executable is not found or the process fails to start, the method
    /// will return false.
    ///
    /// @return true if the server process started successfully, false otherwise
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
            Thread.ofPlatform().start(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    while (true) {
                        String line = reader.readLine();
                        if (line != null) {
                            // offer server's response to res queue
                            res.offer(line);
                            if (!line.equals("OK")) {
                                logger.info("server response: {}", line);
                            }
                        }
                    }
                } catch (IOException e) {
                    logger.info("stdin reader closed");
                }
            });
        } catch (IOException e) {
            e.printStackTrace(System.err);
            return false;
        }
        select("?\n".getBytes()); // show version as server response
        return true;
    }

    /// Stops the server process associated with the IMEServer instance.
    ///
    /// If the server process is currently running, this method will destroy
    /// the process, effectively terminating it. It ensures that resources
    /// associated with the process are released. This method should be called
    /// when the server is no longer needed or before shutting down the
    /// application to ensure a proper cleanup of system resources.
    public void stop() {
        if (process != null) {
            process.destroy();
        }
    }

    /// Selects the Japanese input method on the IMEServer instance.
    public void selectJapanese() { select(JAPANESE); }

    /// Selects the Roman input method on the IMEServer instance.
    public void selectRoman() {
        select(ROMAN);
    }

    /// Selects the com.apple.keylayout.ABC on the IMEServer instance.
    public void selectABC() {
        select(ABC);
    }

    /// Selects the com.apple.keylayout.US on the IMEServer instance.
    public void selectUS() {
        select(US);
    }

    /// Selects the com.apple.keylayout.USExtended on the IMEServer instance.
    public void selectUSExt() {
        select(US_EXT);
    }

    /// Sends the specified language byte array to the output stream, flushes the stream,
    /// and waits for a result from the server.
    ///
    /// @param lang a byte array representing the language selection to be sent to the server.
    private void select(byte[] lang) {
        try {
            // A brief interruption of the EDT
            // as a workaround for incomplete IME switching
            Thread.sleep(50);

            output.write(lang);
            output.flush();
            waitForResult();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace(System.err);
        }
    }

    /// Waits for a response from the server through a specified response queue.
    ///
    /// This method attempts to retrieve a response from the server, which is
    /// expected to be queued in the response queue (res). It waits for a maximum
    /// of 1 second to receive a response. If no response is received within this
    /// period, it logs a timeout event, stops the server, and attempts to restart
    /// it to ensure ongoing communication.
    ///
    /// The server response and timeout events are logged using an injected logger.
    /// Throws an InterruptedException in case of a timeout, which is caught and
    /// handled by logging and restarting server processes.
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

    static void main() {
        IMEServer server = new IMEServer();
        if (server.start()) {
            server.selectJapanese();
        }
    }
}
