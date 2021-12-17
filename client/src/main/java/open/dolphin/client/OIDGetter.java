package open.dolphin.client;

import open.dolphin.JsonConverter;
import open.dolphin.helper.Task;
import open.dolphin.project.Project;
import open.dolphin.service.SystemService;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import javax.security.auth.login.LoginException;
import javax.swing.*;
import javax.ws.rs.client.ClientBuilder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * OIDRequester
 *
 * @author Minagawa, Kazushi
 */
public class OIDGetter extends JPanel {

    public static final String NEXT_OID_PROP = "nextOidProp";
    private static final long serialVersionUID = 1666003906485274645L;
    private static final int MAX_ESTIMATION = 30 * 1000;
    private static final int DELAY = 200;
    private static final String PROGRESS_NOTE = "通信テストをしています...";
    private static final String SUCCESS_NOTE = "通信に成功しました。次項ボタンをクリックし次に進むことができます。";
    private static final String TASK_TITLE = "通信テスト";

    private String helloReply;
    private PropertyChangeSupport boundSupport = new PropertyChangeSupport(this);

    private JButton comTest = new JButton(TASK_TITLE);

    public OIDGetter() {
        initialize();
        connect();
    }

    public String getHelloReply() {
        return helloReply;
    }

    public void setHelloReply(String oid) {
        helloReply = oid;
        boundSupport.firePropertyChange(NEXT_OID_PROP, "", helloReply);
    }

    public void addOidPropertyListener(PropertyChangeListener l) {
        boundSupport.addPropertyChangeListener(NEXT_OID_PROP, l);
    }

    public void removeOidPropertyListener(PropertyChangeListener l) {
        boundSupport.removePropertyChangeListener(NEXT_OID_PROP, l);
    }

    private void initialize() {

        try {
            InputStream in = ClientContext.getResourceAsStream("account-make-info.txt");
//masuda^   UTF-8に変更
            //BufferedReader reader = new BufferedReader(new InputStreamReader(in, "SHIFT_JIS"));
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
//masuda$
            String line = null;
            StringBuilder sb = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
//masuda^
            reader.close();
            in.close();
//masuda$
            JTextArea infoArea = new JTextArea();
            infoArea.setEditable(false);
            infoArea.setLineWrap(true);
            infoArea.setMargin(new Insets(10, 10, 10, 10));
            infoArea.setText(sb.toString());
            JScrollPane scroller = new JScrollPane(infoArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

            JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            btnPanel.add(new JLabel("次のボタンをクリックし、通信できるかどうか確認してください。"));
            btnPanel.add(comTest);

            this.setLayout(new BorderLayout());
            this.add(scroller, BorderLayout.CENTER);
            this.add(btnPanel, BorderLayout.SOUTH);

        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    private void connect() {
        comTest.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doTest();
            }
        });
    }

    private void doTest() {

        String message = "通信テスト";
        Component c = SwingUtilities.getWindowAncestor(this);

        OidTask task = new OidTask(c, message);

        //task.setMillisToPopup(DELAY);
        task.execute();
    }

    private class OidTask extends Task<String> {

        public OidTask(Component c, Object message) {
            super(c, message, PROGRESS_NOTE, MAX_ESTIMATION);
        }

        @Override
        protected String doInBackground() throws Exception {

            // Resteasy
            ResteasyClient client = (ResteasyClient) ClientBuilder.newClient();
            JsonConverter jsonConverter = new JsonConverter();
            client.register(jsonConverter);

            String hostAddr = Project.getHostAddress();
            String restUrl = String.format(("http://%s/dolphin"), hostAddr);
            ResteasyWebTarget target = client.target(restUrl);

            SystemService service = target.proxy(SystemService.class);

            String result = service.hello();
            return result;

        }

        @Override
        protected void succeeded(String result) {
            //logger.debug("Task succeeded");
            Window myParent = SwingUtilities.getWindowAncestor(OIDGetter.this);
            String title = ClientContext.getFrameTitle(TASK_TITLE);
            JOptionPane.showMessageDialog(myParent, SUCCESS_NOTE, title, JOptionPane.INFORMATION_MESSAGE);
            setHelloReply(result);
        }

        @Override
        protected void failed(Throwable cause) {

            String errMsg = null;

            if (cause instanceof javax.ejb.EJBAccessException) {
                StringBuilder sb = new StringBuilder();
                sb.append("システム設定エラー");
                sb.append("\n");
                sb.append(appendExceptionInfo(cause));
                errMsg = sb.toString();

            } else if (cause instanceof javax.naming.CommunicationException) {
                StringBuilder sb = new StringBuilder();
                sb.append("ASPサーバに接続できません。");
                sb.append("\n");
                sb.append("ファイヤーウォール等がサービスを利用できない設定になっている可能性があります。");
                sb.append("\n");
                sb.append(appendExceptionInfo(cause));
                errMsg = sb.toString();

            } else if (cause instanceof javax.naming.NamingException) {
                StringBuilder sb = new StringBuilder();
                sb.append("アプリケーションエラー");
                sb.append("\n");
                sb.append(appendExceptionInfo(cause));
                errMsg = sb.toString();

            } else if (cause instanceof LoginException) {
                cause.printStackTrace(System.err);
                StringBuilder sb = new StringBuilder();
                sb.append("セキュリティエラーが生じました。");
                sb.append("\n");
                sb.append("クライアントの環境が実行を許可されない設定になっている可能性があります。");
                sb.append("\n");
                sb.append(appendExceptionInfo(cause));
                errMsg = sb.toString();

            } else if (cause instanceof Exception) {
                StringBuilder sb = new StringBuilder();
                sb.append("予期しないエラー");
                sb.append("\n");
                sb.append(appendExceptionInfo(cause));
                errMsg = sb.toString();
            }

            Window myParent = SwingUtilities.getWindowAncestor(OIDGetter.this);
            String title = ClientContext.getFrameTitle(TASK_TITLE);
            JOptionPane.showMessageDialog(myParent, errMsg, title, JOptionPane.WARNING_MESSAGE);
            setHelloReply(null);

        }

        private String appendExceptionInfo(Throwable cause) {
            StringBuilder sb = new StringBuilder();
            sb.append("例外クラス: ");
            sb.append(cause.getClass().getName());
            sb.append("\n");
            if (cause.getCause() != null) {
                sb.append("原因: ");
                sb.append(cause.getCause().getMessage());
                sb.append("\n");
            }
            if (cause.getMessage() != null) {
                sb.append("内容: ");
                sb.append(cause.getMessage());
                sb.append("\n");
            }
            return sb.toString();
        }
    }
}
