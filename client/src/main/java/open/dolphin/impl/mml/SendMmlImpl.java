
package open.dolphin.impl.mml;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import open.dolphin.client.ClientContext;
import open.dolphin.client.MainWindow;
import open.dolphin.client.MmlMessageEvent;
import open.dolphin.client.MmlMessageListener;
import open.dolphin.infomodel.SchemaModel;
import open.dolphin.project.Project;

import org.apache.log4j.Logger;

/**
 * MML 送信サービス.
 *
 * @author  Kazushi Minagawa, Digital Globe, Inc.
 */
public class SendMmlImpl implements MmlMessageListener {

    // CSGW への書き込みパス
    private String csgwPath;

    // MML Encoding
    private String encoding;

    // Work Queue
    private LinkedBlockingQueue queue;

    private Kicker kicker;

    private Thread sendThread;

    private Logger logger;

    private MainWindow context;

    private String name;

    /** Creates new SendMmlService */
    public SendMmlImpl() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public MainWindow getContext() {
        return context;
    }

    public void setContext(MainWindow context) {
        this.context = context;
    }

    public String getCSGWPath() {
        return csgwPath;
    }

    private Logger getLogger() {
        if (logger == null) {
            logger = ClientContext.getPart11Logger();
        }
        return logger;
    }

    public void setCSGWPath(String val) {
        csgwPath = val;
        File directory = new File(csgwPath);
        if (! directory.exists()) {
            if (directory.mkdirs()) {
                getLogger().debug("MMLファイル出力先のディレクトリを作成しました");
            } else {
                getLogger().warn("MMLファイル出力先のディレクトリを作成できません");
            }
        }
    }

    public void stop() {
        try {
            Thread moribund = sendThread;
            sendThread = null;
            moribund.interrupt();
            logDump();
            getLogger().info("Send MML stopped");

        } catch (Exception e) {
            e.printStackTrace();
            getLogger().warn("Exception while stopping the send MML");
            getLogger().warn(e.getMessage());
        }
    }

    public void start() {

        // CSGW 書き込みパスを設定する
        setCSGWPath(Project.getCSGWPath());
        encoding = Project.getMMLEncoding();

        // 送信キューを生成する
        queue = new LinkedBlockingQueue();
        kicker = new Kicker();
        sendThread = new Thread(kicker);
        sendThread.start();
        getLogger().info("Send MML statered with CSGW = " + getCSGWPath());
    }

    public void mmlMessageEvent(MmlMessageEvent e) {
        queue.offer(e);
    }

    public Object getMML() throws InterruptedException {
        return queue.take();
    }

    public void logDump() {

        synchronized (queue) {

            int size = queue.size();

            if (size != 0) {
                for (int i = 0; i < size; i++) {
                    try {
                        MmlMessageEvent evt = (MmlMessageEvent) queue.take();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    protected String getCSGWPathname(String fileName, String ext) {
        StringBuffer buf = new StringBuffer();
        buf.append(csgwPath);
        buf.append(File.separator);
        buf.append(fileName);
        buf.append(".");
        buf.append(ext);
        return buf.toString();
    }

    protected class Kicker implements Runnable {

        public void run() {


            Thread thisThread = Thread.currentThread();
            BufferedOutputStream writer = null;

            while (thisThread == sendThread) {

                try {
                    // MML パッケージを取得
                    MmlMessageEvent mevt = (MmlMessageEvent) getMML();
                    getLogger().debug("MMLファイルをコンシュームしました");
                    String groupId = mevt.getGroupId();
                    String instance = mevt.getMmlInstance();
                    List<SchemaModel> schemas = mevt.getSchema();

                    // ファイル名を生成する
                    String dest = getCSGWPathname(groupId, "xml");
                    String temp = getCSGWPathname(groupId, "xml.tmp");
                    File f = new File(temp);

                    // インスタンスをUTF8で書き込む
                    writer = new BufferedOutputStream(new FileOutputStream(f));
                    byte[] bytes = instance.getBytes(encoding);
                    writer.write(bytes);
                    writer.flush();
                    writer.close();

                    // 書き込み終了後にリネームする (.tmp -> .xml)
                    f.renameTo(new File(dest));
                    getLogger().debug("MMLファイルを書き込みました");

                    // 画像を送信する
                    if (schemas != null) {
                        for (SchemaModel schema : schemas) {
                            dest = csgwPath + File.separator + schema.getExtRef().getHref();
                            temp = dest + ".tmp";
                            f = new File(temp);
                            writer = new BufferedOutputStream(new FileOutputStream(f));
                            writer.write(schema.getJpegByte());
                            writer.flush();
                            writer.close();

                            // Renameする
                            f.renameTo(new File(dest));
                            getLogger().debug("画像ファイルを書き込みました");
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    getLogger().warn("IOException while send MML");
                    getLogger().warn(e.getMessage());

                } catch (InterruptedException ie) {
                    getLogger().warn("InterruptedException while send MML");
                    break;
                }
            }

        }
    }
}
