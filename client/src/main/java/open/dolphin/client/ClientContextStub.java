package open.dolphin.client;

import open.dolphin.infomodel.DepartmentModel;
import open.dolphin.infomodel.LicenseModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.InputStream;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.util.Date;
import java.util.ResourceBundle;

/**
 * Dolphin Client のコンテキストクラス.
 *
 * @author Kazushi Minagawa, Digital Globe, Inc.
 */
public final class ClientContextStub {

    private final String RESOURCE_LOCATION = "/";
    private final String IMAGE_LOCATION = "/images/";
    private final String RESOURCE = "Dolphin_ja";
    private final ResourceBundle resBundle;
    private final Logger logger;
    private final String documentFolder;
    private boolean isMac, isWin, isLinux;

    /**
     * ClientContextStub オブジェクトを生成する.
     */
    public ClientContextStub() {

        // ResourceBundle を得る
        resBundle = ResourceBundle.getBundle(RESOURCE);

        // Logger を生成する
        logger = LoggerFactory.getLogger(ClientContextStub.class);

        // 基本情報を出力する
        logStartupInformation();

        // デフォルトの UI フォントを変更する
        setUIFonts();

        // OS情報
        String osname = System.getProperty("os.name").toLowerCase();
        isMac = osname.startsWith("mac");
        isWin = osname.startsWith("windows");
        isLinux = osname.startsWith("linux");

        // Document フォルダの場所
        documentFolder = isWin() ? "Z:\\" : "/Volumes/documents/";
    }

    private void logStartupInformation() {
        logger.info("起動時刻 = {}", DateFormat.getDateTimeInstance().format(new Date()));
        logger.info("os.name = {}", System.getProperty("os.name"));
        logger.info("os.arch = {}", System.getProperty("os.arch"));
        logger.info("java.version = {}", System.getProperty("java.version"));
        logger.info("java.vm.version = {}", System.getProperty("java.vm.version"));
        //logger.info("javafx.version = " + System.getProperty("javafx.version"));
        logger.info("dolphin.version = {}", System.getProperty("open.dolphin.build.project.version"));
        logger.info("dolphin.build.timestamp = {}", System.getProperty("open.dolphin.build.timestamp"));

        // garbage collector information
        for (GarbageCollectorMXBean gcMxBean : ManagementFactory.getGarbageCollectorMXBeans()) {
            logger.info("{}, {}", gcMxBean.getName(), gcMxBean.getObjectName());
        }
        // processor number
        logger.info("available processors = {}", Runtime.getRuntime().availableProcessors());
        logger.info("java2d opengl = {}", System.getProperty("sun.java2d.opengl"));
        logger.info("java2d metal = {}", System.getProperty("sun.java2d.metal"));
    }

    public String getDocumentDirectory() { return documentFolder; }

    public boolean isMac() { return isMac; }

    public boolean isWin() {
        return isWin;
    }

    public boolean isLinux() {
        return isLinux;
    }

    public String getLocation(String dir) {
        StringBuilder sb = new StringBuilder();
        // sb.append(System.getProperty(getString("base.dir")));
        // AppBundler が base.dir を正しく返さないのの workaround
        try {
            Path path = Paths.get(getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
            if (path.getFileName().toString().contains(".jar")) {
                // jar ファイルや .app から立ち上げている場合
                sb.append(path.getParent().toString());

            } else {
                // netbeans から立ち上げている場合は従来通り
                sb.append(System.getProperty(getString("base.dir")));
            }

        } catch (URISyntaxException ex) {
            ex.printStackTrace(System.err);
        }

        return switch(dir) {
            case "base" -> sb.toString();
            case "lib" -> {
                sb.append(File.separator);
                if (isMac()) {
                    sb.append(getString("lib.mac.dir"));
                } else {
                    sb.append(getString("lib.dir"));
                }
                yield sb.toString();
            }
            case "dolphin.jar" -> {
                if (isMac()) {
                    sb.append(File.separator);
                    sb.append(getString("dolphin.jar.mac.dir"));
                }
                yield sb.toString();
            }
            case "security", "log", "setting", "schema", "plugins", "pdf" -> {
                sb.append(File.separator);
                sb.append(getString(dir + ".dir"));
                yield sb.toString();
            }
            default -> null;
        };
    }

    public String getBaseDirectory() {
        return getLocation("base");
    }

    public String getPDFDirectory() {
        return getLocation("pdf");
    }

    public String getFrameTitle(String title) {
        try {
            String resTitle = getString(title);
            if (resTitle != null) {
                title = resTitle;
            }
        } catch (Exception e) {
            // ここの exception は無害
            // System.out.println("ClientContextStub.java: " + e);
        }

        return String.format("%s-%s", title, getString("application.title"));
    }

    public URL getImageResource(String name) {
        if (!name.startsWith("/")) {
            name = IMAGE_LOCATION + name;
        }
        return this.getClass().getResource(name);
    }

    public InputStream getResourceAsStream(String name) {
        if (!name.startsWith("/")) {
            name = RESOURCE_LOCATION + name;
        }
        return this.getClass().getResourceAsStream(name);
    }

    public ImageIcon getImageIcon(String name) {
        return new ImageIcon(getImageResource(name));
    }

    public LicenseModel[] getLicenseModel() {
        String[] desc = getStringArray("licenseDesc");
        String[] code = getStringArray("license");
        String codeSys = getString("licenseCodeSys");
        LicenseModel[] ret = new LicenseModel[desc.length];
        LicenseModel model;
        for (int i = 0; i < desc.length; i++) {
            model = new LicenseModel();
            model.setLicense(code[i]);
            model.setLicenseDesc(desc[i]);
            model.setLicenseCodeSys(codeSys);
            ret[i] = model;
        }
        return ret;
    }

    public DepartmentModel[] getDepartmentModel() {
        String[] desc = getStringArray("departmentDesc");
        String[] code = getStringArray("department");
        String codeSys = getString("departmentCodeSys");
        DepartmentModel[] ret = new DepartmentModel[desc.length];
        DepartmentModel model;
        for (int i = 0; i < desc.length; i++) {
            model = new DepartmentModel();
            model.setDepartment(code[i]);
            model.setDepartmentDesc(desc[i]);
            model.setDepartmentCodeSys(codeSys);
            ret[i] = model;
        }
        return ret;
    }

    public NameValuePair[] getNameValuePair(String key) {
        NameValuePair[] ret;
        String[] code = getStringArray(key + ".value");
        String[] name = getStringArray(key + ".name");
        int len = code.length;
        ret = new NameValuePair[len];

        for (int i = 0; i < len; i++) {
            ret[i] = new NameValuePair(name[i], code[i]);
        }
        return ret;
    }

    public String getString(String key) {
        return resBundle.getString(key);
    }

    public String[] getStringArray(String key) {
        String line = getString(key);
        return line.split(",");
    }

    public int getInt(String key) {
        return Integer.parseInt(getString(key));
    }

    public int[] getIntArray(String key) {
        String[] obj = getStringArray(key);
        int[] ret = new int[obj.length];
        for (int i = 0; i < obj.length; i++) {
            ret[i] = Integer.parseInt(obj[i]);
        }
        return ret;
    }

    public boolean getBoolean(String key) {
        return Boolean.parseBoolean(getString(key));
    }

    public Dimension getDimension(String name) {
        int[] data = getIntArray(name);
        return new Dimension(data[0], data[1]);
    }

    public Color getColor(String key) {
        int[] data = getIntArray(key);
        return new Color(data[0], data[1], data[2]);
    }

    public Color[] getColorArray(String key) {
        int[] data = getIntArray(key);
        int cnt = data.length / 3;
        Color[] ret = new Color[cnt];
        for (int i = 0; i < cnt; i++) {
            int bias = i * 3;
            ret[i] = new Color(data[bias], data[bias + 1], data[bias + 2]);
        }
        return ret;
    }

    /**
     * Windows のデフォルトフォントを設定する.
     */
    private void setUIFonts() {

        if (isWin() || isLinux()) {
            int size = 12;
            if (isLinux()) {
                size = 13;
            }
            Font font = new Font("SansSerif", Font.PLAIN, size);
            UIManager.put("Label.font", font);
            UIManager.put("Button.font", font);
            UIManager.put("ToggleButton.font", font);
            UIManager.put("Menu.font", font);
            UIManager.put("MenuItem.font", font);
            UIManager.put("CheckBox.font", font);
            UIManager.put("CheckBoxMenuItem.font", font);
            UIManager.put("RadioButton.font", font);
            UIManager.put("RadioButtonMenuItem.font", font);
            UIManager.put("ToolBar.font", font);
            UIManager.put("ComboBox.font", font);
            UIManager.put("TabbedPane.font", font);
            UIManager.put("TitledBorder.font", font);
            UIManager.put("List.font", font);

            logger.info("デフォルトのフォントを変更しました");
        }
    }
}
