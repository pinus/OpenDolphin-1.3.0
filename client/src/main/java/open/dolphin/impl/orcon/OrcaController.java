package open.dolphin.impl.orcon;

import io.github.bonigarcia.wdm.WebDriverManager;
import open.dolphin.client.AbstractMainComponent;
import open.dolphin.client.ImageBox;
import open.dolphin.helper.WindowHolder;
import open.dolphin.impl.psearch.PatientSearchImpl;
import open.dolphin.impl.pvt.WaitingListImpl;
import open.dolphin.infomodel.PatientModel;
import open.dolphin.infomodel.PatientVisitModel;
import open.dolphin.stampbox.StampBoxPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.io.output.TeeOutputStream;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Callable;
import java.util.prefs.Preferences;

/**
 * オルコン: ORCA を遠隔操作する.
 * @author pns
 */
public class OrcaController extends AbstractMainComponent {
    private static final String NAME = "オルコン";
    private OrconPanel orconPanel;
    private OrconProperties orconProps;
    private OrconMacro orconMacro;
    private OrconKeyDispatcher keyDispatcher;
    private final Logger logger;

    public OrcaController() {
        setName(NAME);
        WebDriverManager.chromedriver().setup();
        logger = LoggerFactory.getLogger(OrcaController.class);
    }

    @Override
    public JPanel getUI() {
        return orconPanel.getPanel();
    }

    @Override
    public void start() {
        orconPanel = new OrconPanel();
        orconPanel.setLoginState(false);

        orconProps = new OrconProperties(orconPanel);
        orconProps.modelToView();
        orconMacro = new OrconMacro(orconPanel, orconProps);

        // set default button
        getContext().getFrame().getRootPane().setDefaultButton(orconPanel.getLoginButton());

        // key dispatcher の動き
        keyDispatcher = new OrconKeyDispatcher(orconMacro);
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(keyDispatcher);
        // login したら　enables する
        orconPanel.getLoginButton().addActionListener(e -> {
            orconMacro.login();
            keyDispatcher.setMode(OrconKeyDispatcher.Mode.FULL);
        });
        orconPanel.getCloseButton().addActionListener(e -> {
            orconMacro.close();
            keyDispatcher.setMode(OrconKeyDispatcher.Mode.DISABLE);
        });

        // window inactive になったら disable する, active になったら元に戻す
        getContext().getFrame().addWindowListener(new WindowAdapter() {
            private OrconKeyDispatcher.Mode oldMode
                = OrconKeyDispatcher.Mode.DISABLE;

            @Override
            public void windowActivated(WindowEvent e) {
                orconPanel.setActive(isEnabled());
                keyDispatcher.setMode(oldMode);
            }

            @Override
            public void windowDeactivated(WindowEvent e) {
                orconPanel.setActive(false);
                oldMode = keyDispatcher.getMode();
                keyDispatcher.setMode(OrconKeyDispatcher.Mode.DISABLE);
            }
        });
        // 他の panel に移ったら disable
        orconPanel.getPanel().addComponentListener(new ComponentListener() {
            @Override
            public void componentHidden(ComponentEvent e) {
                keyDispatcher.setMode(OrconKeyDispatcher.Mode.DISABLE);
            }

            @Override
            public void componentShown(ComponentEvent e) {}
            @Override
            public void componentResized(ComponentEvent e) {}
            @Override
            public void componentMoved(ComponentEvent e) {}
        });
    }

    /**
     * ORCA が立ち上がっているかどうか
     * @return true if orca is active
     */
    public boolean isEnabled() {
        return orconPanel.getCloseButton().isEnabled();
    }

    /**
     * OrconKeyDispatcher Stealth mode ON/OFF
     * @param enable set true for stealth mode
     */
    public void setStealth(boolean enable) {
        keyDispatcher.setMode(enable?
            OrconKeyDispatcher.Mode.STEALTH : OrconKeyDispatcher.Mode.DISABLE);
    }

    @Override
    public void enter() {
        logger.info("enter");
        // 入ってきたら key dispatcher enable
        keyDispatcher.setMode(isEnabled()?
            OrconKeyDispatcher.Mode.FULL : OrconKeyDispatcher.Mode.DISABLE);

        // オルコン操作中はウインドウをできるだけ隠す
        if (isEnabled()) {
            StampBoxPlugin stampBox = getContext().getPlugin(StampBoxPlugin.class);
            stampBox.getFrame().setState(Frame.ICONIFIED);
            ImageBox imageBox = getContext().getPlugin(ImageBox.class);
            if (imageBox != null) {
                imageBox.getFrame().setVisible(false);
            }
            WindowHolder.allCharts().forEach(c -> c.getFrame().setState(Frame.ICONIFIED));
        }

        // 患者番号を orconMacro に保存する
        String ptnum = "";
        // 開いている chart があれば, その患者番号を保存
        if (!WindowHolder.allCharts().isEmpty()) {
            ptnum = WindowHolder.allCharts().getFirst().getPatient().getPatientId();
            //logger.info("ptnum in windowholder = " + ptnum);
        }
        // ない場合は, PatientSearchImpl の選択患者番号を保存
        if (ptnum.isEmpty()) {
            PatientModel[] pm = getContext().getPlugin(PatientSearchImpl.class).getSelectedPatinet();
            if (pm != null && pm.length > 0) {
                ptnum = pm[0].getPatientId();
                //logger.info("ptnum in patientsearch = " + ptnum);
            }
        }
        // それもない場合は, WaitingList の選択患者番号を保存
        if (ptnum.isEmpty()) {
            PatientVisitModel[] pvt = getContext().getPlugin(WaitingListImpl.class).getSelectedPvt();
            if (pvt != null && pvt.length > 0) {
                ptnum = pvt[0].getPatientId();
                //logger.info("ptnum in waitinglist = " + ptnum);
            }
        }
        orconMacro.setPatientNumber(ptnum);
    }

    @Override
    public void stop() {}

    @Override
    public Callable<Boolean> getStoppingTask() {
        logger.info("OrcaController stopping task starts");
        return () -> {
            try {
                orconMacro.close();
            } catch (RuntimeException ex) {
                System.err.println(ex.getMessage());
            }
            return true;
        };
    }

    private static void redirectConsole() {
        try {
            String applicationSupportDir = System.getProperty("user.home") + "/Library/Application Support/OrcaController/";
            Path p = Paths.get(applicationSupportDir);
            if (!Files.exists(p)) {
                Files.createDirectory(p);
            }
            // tee で file と stdout 両方に出力する
            String logName = applicationSupportDir + "console.log";
            PrintStream fileStream = new PrintStream(new FileOutputStream(logName, true), true); // append, auto flush
            TeeOutputStream teeStream = new TeeOutputStream(System.out, fileStream);
            PrintStream tee = new PrintStream(teeStream);
            System.setOut(tee);
            System.setErr(tee);

        } catch (IOException ex) {
        }
    }

    public static void main(String[] args) {
        redirectConsole();
        OrcaController orcon = new OrcaController();
        orcon.start();
        Preferences prefs = Preferences.userNodeForPackage(OrconProperties.class);

        int x = prefs.getInt("JFLAME_X", 100);
        int y = prefs.getInt("JFLAME_Y", 100);
        int w = prefs.getInt("JFLAME_W", 720);
        int h = prefs.getInt("JFLAME_H", 240);
        System.out.println("prefs loaded");

        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.getRootPane().putClientProperty("apple.awt.transparentTitleBar", true);
        frame.setBounds(new Rectangle(x, y, w, h));
        frame.add(orcon.getUI());
        frame.getRootPane().setDefaultButton(orcon.orconPanel.getLoginButton());

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                prefs.putInt("JFLAME_X", frame.getBounds().x);
                prefs.putInt("JFLAME_Y", frame.getBounds().y);
                prefs.putInt("JFLAME_W", frame.getBounds().width);
                prefs.putInt("JFLAME_H", frame.getBounds().height);
                System.out.println("prefs saved");
                System.exit(0);
            }
        });

        Desktop desktop = Desktop.getDesktop();
        desktop.setQuitHandler((e, response) -> frame.dispose());

        frame.setVisible(true);
    }
}
