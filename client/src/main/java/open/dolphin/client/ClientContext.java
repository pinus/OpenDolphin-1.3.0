package open.dolphin.client;

import open.dolphin.infomodel.DepartmentModel;
import open.dolphin.infomodel.LicenseModel;

import javax.swing.*;
import java.awt.*;
import java.io.InputStream;

/**
 * ClientContextStub のインスタンスを static method で参照する.
 *
 * @author Kazushi Minagawa, Digital Globe, Inc.
 */
public class ClientContext {

    private static ClientContextStub stub;

    public static ClientContextStub getClientContextStub() {
        return stub;
    }

    public static void setClientContextStub(ClientContextStub s) {
        stub = s;
    }

    public static boolean isMac() { return stub.isMac(); }

    public static boolean isWin() {
        return stub.isWin();
    }

    public static String getLocation(String loc) {
        return stub.getLocation(loc);
    }

   public static String getPDFDirectory() {
        return stub.getPDFDirectory();
    }

    public static InputStream getResourceAsStream(String name) {
        return stub.getResourceAsStream(name);
    }

    public static String getString(String name) {
        return stub.getString(name);
    }

    public static String[] getStringArray(String name) {
        return stub.getStringArray(name);
    }

    public static boolean getBoolean(String name) {
        return stub.getBoolean(name);
    }

    public static int getInt(String name) {
        return stub.getInt(name);
    }

    public static int[] getIntArray(String name) {
        return stub.getIntArray(name);
    }

    public static Color getColor(String name) {
        return stub.getColor(name);
    }

    public static Color[] getColorArray(String name) { return stub.getColorArray(name); }

    public static ImageIcon getImageIcon(String name) { return stub.getImageIcon(name); }

    public static String getFrameTitle(String name) { return stub.getFrameTitle(name); }

    public static Dimension getDimension(String name) {
        return stub.getDimension(name);
    }

    public static NameValuePair[] getNameValuePair(String key) {
        return stub.getNameValuePair(key);
    }

    public static LicenseModel[] getLicenseModel() {
        return stub.getLicenseModel();
    }

    public static DepartmentModel[] getDepartmentModel() {
        return stub.getDepartmentModel();
    }

    public static String getDocumentDirectory() { return stub.getDocumentDirectory(); }
}
