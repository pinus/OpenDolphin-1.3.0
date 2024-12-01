package open.dolphin.ui;

import open.dolphin.client.ClientContext;
import open.dolphin.client.ClientContextStub;
import open.dolphin.project.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

/**
 * Mac で IME on/off を切り替える.
 * <ul>
 * <li>ver 1: AppleScript で on/off するバージョン: 遅すぎてストレスたまる
 * <li>ver 2: InputContext.selectInputMethod バージョン: 調子よかったが，1.6.0_29 で使えなくなる
 * <li>ver 3: Robot version 切り替わったかどうか判定するために event queue システム導入
 * <li>ver 4: enableInputMethod(true/false) バージョン: short-cut が効かなくなったり不安定
 * <li>ver 5: Robot version 復活. 物理キーが押されていると誤動作するのでキー入力でフォーカスが当たるところには使えない
 * <li>ver 6: key combination での robot 入力うまくいかず, F12, F13 キーで切り替えるように ATOK 側で設定することにした
 * <li>ver 7: im-select 呼び出し法 (https://github.com/daipeihust/im-select)
 * <li>ver 8: FocusManger で一元管理バージョン
 * <li>ver 9: TISServer バージョン</li>
 * </ul>
 *
 * @author pns
 */
public class IMEControl {
    private final Logger logger = LoggerFactory.getLogger(IMEControl.class);

    public IMEControl() {
        Process tisServerProcess;
        OutputStream tisServerOutputstream;
        String tisDir = System.getProperty("user.dir");

        // TISServer のある directory を調べる
        ClientContextStub stub = ClientContext.getClientContextStub();
        if (stub != null) {
            tisDir = stub.getBaseDirectory(); // jar の場合 /Resources が返る
        } else {
            // IMEControl 単独でテストするとき client が付かないので付ける
            if (!tisDir.contains("client")) { tisDir = tisDir + "/client"; }
        }

        try {
            // TISServer 起動
            tisServerProcess = new ProcessBuilder(tisDir + "/TISServer").start();
            tisServerOutputstream = tisServerProcess.getOutputStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // 終了時に TISServer を destroy する
        Runtime.getRuntime().addShutdownHook(new Thread(() -> tisServerProcess.destroy()));

        KeyboardFocusManager.getCurrentKeyboardFocusManager().addPropertyChangeListener("permanentFocusOwner", e -> {
            if (Objects.nonNull(e.getNewValue())
                && e.getNewValue() instanceof JTextComponent c
                && !(c instanceof JPasswordField)
                && Objects.isNull(c.getClientProperty(Project.ATOK_ROMAN_KEY))) {
                try {
                    tisServerOutputstream.write("J\n".getBytes());
                    tisServerOutputstream.flush();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            } else {
                try {
                    tisServerOutputstream.write("R\n".getBytes());
                    tisServerOutputstream.flush();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
    }

    public static void main(String[] argv) {
        new IMEControl();

        JFrame f = new JFrame();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JLabel l1 = new JLabel("TF1");
        JLabel l2 = new JLabel("TF2");
        JLabel l3 = new JLabel("TF3");

        JTextField tf1 = new JTextField(30);
        JPasswordField tf2 = new JPasswordField(30);
        JTextField tf3 = new JTextField(30);
        tf3.putClientProperty(Project.ATOK_ROMAN_KEY, true);

        JPanel p1 = new JPanel();
        p1.setLayout(new BoxLayout(p1, BoxLayout.X_AXIS));
        p1.add(l1);
        p1.add(tf1);
        JPanel p2 = new JPanel();
        p2.setLayout(new BoxLayout(p2, BoxLayout.X_AXIS));
        p2.add(l2);
        p2.add(tf2);
        JPanel p3 = new JPanel();
        p3.setLayout(new BoxLayout(p3, BoxLayout.X_AXIS));
        p3.add(l3);
        p3.add(tf3);

        f.getRootPane().setLayout(new BoxLayout(f.getRootPane(), BoxLayout.Y_AXIS));
        f.getRootPane().add(p1);
        f.getRootPane().add(p2);
        f.getRootPane().add(p3);
        f.pack();
        f.setLocation(200,100);
        f.setVisible(true);
    }
}
