package open.dolphin.impl.orcon;

import io.github.bonigarcia.wdm.WebDriverManager;
import open.dolphin.client.AbstractMainComponent;
import open.dolphin.client.Dolphin;
import open.dolphin.client.GUIConst;
import open.dolphin.client.ImageBox;
import open.dolphin.event.BadgeEvent;
import open.dolphin.event.BadgeListener;
import open.dolphin.helper.WindowHolder;
import open.dolphin.stampbox.StampBoxPlugin;
import open.dolphin.ui.PNSBadgeTabbedPane;
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
    private WindowListener windowListener;
    private BadgeListener badgeListener;
    private BadgeEvent badgeEvent;
    private final Logger logger;

    public OrcaController() {
        setName(NAME);
        logger = LoggerFactory.getLogger(OrcaController.class);
        Thread.ofVirtual().start(() -> {
            logger.info("Setting up chrome driver...");
            WebDriverManager.chromedriver().setup();
            logger.info("Chrome driver setting up done");
        });
    }

    public OrconPanel getOrconPanel() { return orconPanel; }

    public OrconProperties getOrconProps() { return orconProps; }

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
        orconMacro = new OrconMacro(this);

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

        // 他の panel に移ったら disable
        orconPanel.getPanel().addComponentListener(new ComponentListener() {
            @Override
            public void componentHidden(ComponentEvent e) {
                if (keyDispatcher.getMode() == OrconKeyDispatcher.Mode.FULL) {
                    keyDispatcher.setMode(badgeEvent.getBadgeNumber() == -1
                        ? OrconKeyDispatcher.Mode.STEALTH : OrconKeyDispatcher.Mode.DISABLE);
                }
                getContext().getFrame().removeWindowListener(windowListener);
                getContext().enableAction(GUIConst.ACTION_STEALTH_ORCON, true);
            }
            @Override
            public void componentShown(ComponentEvent e) {
                // mode は enter で処理
                getContext().getFrame().addWindowListener(windowListener);
                getContext().enableAction(GUIConst.ACTION_STEALTH_ORCON, false);
            }
            @Override
            public void componentResized(ComponentEvent e) {}
            @Override
            public void componentMoved(ComponentEvent e) {}
        });
        // orcon パネルが表示されている間だけ有効な window listener. mode: FULL/DISABLE のみ
        windowListener = new WindowAdapter() {
            @Override
            public void windowActivated(WindowEvent e) {
                orconPanel.setActive(isEnabled());
                keyDispatcher.setMode(isEnabled()? OrconKeyDispatcher.Mode.FULL : OrconKeyDispatcher.Mode.DISABLE);
            }
            @Override
            public void windowDeactivated(WindowEvent e) {
                orconPanel.setActive(false);
                keyDispatcher.setMode(OrconKeyDispatcher.Mode.DISABLE);
            }
        };
        // バッジ関連
        PNSBadgeTabbedPane pane = ((Dolphin) getContext()).getTabbedPane();
        badgeListener = pane::setBadge;
        badgeEvent = new BadgeEvent(this);
        badgeEvent.setTabIndex(3);
    }

    /**
     * ORCA が立ち上がっているかどうか
     * @return true if orca is active
     */
    public boolean isEnabled() {
        return orconPanel.getCloseButton().isEnabled();
    }

    /**
     * ORCA を操作しやすくなるように, できるだけウインドウを隠す.
     * @param hide to hide windows
     */
    private void hideWindowsAsPossible(boolean hide) {
        StampBoxPlugin stampBox = getContext().getPlugin(StampBoxPlugin.class);
        stampBox.getFrame().setState(hide ? Frame.ICONIFIED : Frame.NORMAL);
        ImageBox imageBox = getContext().getPlugin(ImageBox.class);
        if (imageBox != null && imageBox.getFrame().isVisible()) {
            imageBox.getFrame().setState(hide ? Frame.ICONIFIED : Frame.NORMAL);
        }
        WindowHolder.allCharts().forEach(c -> c.getFrame().setState(hide ? Frame.ICONIFIED : Frame.NORMAL));
        SwingUtilities.invokeLater(() -> getContext().getFrame().toFront());
    }

    /**
     * ステルスモードを toggle する.
     */
    public void toggleStealth() {
        if (isEnabled()) {
            switch(keyDispatcher.getMode()) {
                case OrconKeyDispatcher.Mode.DISABLE -> {
                    keyDispatcher.setMode(OrconKeyDispatcher.Mode.STEALTH);
                    hideWindowsAsPossible(true);
                    badgeEvent.setBadgeNumber(-1);
                    badgeListener.badgeChanged(badgeEvent);
                }
                case OrconKeyDispatcher.Mode.STEALTH -> {
                    keyDispatcher.setMode(OrconKeyDispatcher.Mode.DISABLE);
                    hideWindowsAsPossible(false);
                    badgeEvent.setBadgeNumber(0);
                    badgeListener.badgeChanged(badgeEvent);
                }
            }
        }
    }

    /**
     * オルコンパネルが選択された時の入り口.
     */
    @Override
    public void enter() {
        logger.info("enter");
        // 入ってきたら key dispatcher enable
        if (isEnabled()) {
            keyDispatcher.setMode(OrconKeyDispatcher.Mode.FULL);
            // オルコン操作中はウインドウをできるだけ隠す
            hideWindowsAsPossible(isEnabled());
        } else {
            keyDispatcher.setMode(OrconKeyDispatcher.Mode.DISABLE);
        }
        orconPanel.setActive(isEnabled());
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
