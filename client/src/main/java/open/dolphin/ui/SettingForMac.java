package open.dolphin.ui;

import com.apple.eawt.AppEvent.AboutEvent;
import com.apple.eawt.AppEvent.PreferencesEvent;
import com.apple.eawt.AppEvent.QuitEvent;
import com.apple.eawt.QuitResponse;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import open.dolphin.client.Dolphin;

/**
 * Mac + Quaqua 用のセッティング
 * @author pns
 */
public class SettingForMac {
    private SettingForMac() {}

    public static void set(final Dolphin context) {
        // JavaFX settings
        // Mac OS X needs this to avoid HeadlessException
        System.setProperty("java.awt.headless", "false");
        // necessary to initialize JavaFX Toolkit
        new JFXPanel();
        // JavaFX thread が SchemaEditor 終了後に shutdown してしまわないようにする
        Platform.setImplicitExit(false);

        // apple settings
        System.setProperty("apple.laf.useScreenMenuBar","true"); // ClientContextStub でも設定しているが，そこだと遅いようだ
        // quaqua のセットアップ
        setQuaqua(context);
        // com.apple.eawt.Application の設定
        setMacApplication(context);

        TouchpadTest.startListening();
    }

    public static void setQuaqua(Dolphin context) {
        // quaqua settings
        //System.setProperty("Quaqua.tabLayoutPolicy","wrap");
        //System.setProperty("Quaqua.enforceVisualMargin","true");
        //System.setProperty("Quaqua.sizeStyle","small");

        // Lion は自動的に leopard に設定されてしまう
        //System.setProperty("Quaqua.design","snowleopard");
        System.setProperty("Quaqua.design","mountainlion");

        //java.util.Set<String> includes = new java.util.HashSet<String>();
        java.util.Set<String> excludes = new java.util.HashSet<String>();
        //includes.add("Component");
        //includes.add("TabbedPane");
        //includes.add("Tree");
        //includes.add("ColorChooser");
        //includes.add("ComboBox");
        //includes.add("ToggleButton");
        //includes.add("");
        //excludes.add("Table");
        excludes.add("ScrollBar");
        excludes.add("Button");
        //excludes.add("ComboBox");
        //excludes.add("TabbedPane");
        excludes.add("PopupMenu");
        //ch.randelshofer.quaqua.QuaquaManager.setIncludedUIs(includes);
        ch.randelshofer.quaqua.QuaquaManager.setExcludedUIs(excludes);

        // set the Quaqua Look and Feel in the UIManager
        try {
            UIManager.setLookAndFeel("ch.randelshofer.quaqua.QuaquaLookAndFeel");
        } catch (ClassNotFoundException e) { System.out.println("Dolphin.java: " + e);
        } catch (IllegalAccessException e) { System.out.println("Dolphin.java: " + e);
        } catch (UnsupportedLookAndFeelException e) { System.out.println("Dolphin.java: " + e);
        } catch (InstantiationException e) { System.out.println("Dolphin.java: " + e);}

        //
        // java 1.7.0_55 から
        // setLookAndFeel が JFX Thread の ClassLoader を null にしてしまうようになった
        //
        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        System.out.println("ClassLoader in Swing Thread = " + contextClassLoader);

        Platform.runLater(new Runnable(){
            @Override
            public void run() {
                ClassLoader jfx = Thread.currentThread().getContextClassLoader();
                if (jfx == null) {
                    System.out.println("ClassLoader in JFX Thread is null; set " + contextClassLoader + " instead.");
                    Thread.currentThread().setContextClassLoader(contextClassLoader);
                }
            }
        });
    }

    private static void setMacApplication(final Dolphin context) {

        // Mac Application Menu
        com.apple.eawt.Application fApplication = com.apple.eawt.Application.getApplication();

        // ...について
        fApplication.setAboutHandler(new com.apple.eawt.AboutHandler() {
            @Override
            public void handleAbout(AboutEvent ae) {
                context.showAbout();
            }
        });
        // 終了
        fApplication.setQuitHandler(new com.apple.eawt.QuitHandler() {
            @Override
            public void handleQuitRequestWith(QuitEvent qe, QuitResponse qr) {
                context.processExit();
                qr.cancelQuit();
            }
        });
        // 環境設定
        fApplication.setPreferencesHandler(new com.apple.eawt.PreferencesHandler() {
            @Override
            public void handlePreferences(PreferencesEvent pe) {
                context.doPreference();
            }
        });
    }
}
