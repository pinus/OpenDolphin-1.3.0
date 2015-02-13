package open.dolphin.client;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import javax.swing.ImageIcon;


/**
 * Transferable class of the ImageIcon.
 *
 * @author  Kazushi Minagawa, Digital Globe, Inc.
 */
public final class ImageIconTransferable implements Transferable, ClipboardOwner {

    /** Data Flavor of this class */
    public static DataFlavor imageIconFlavor = new DataFlavor(javax.swing.ImageIcon.class, "Image Icon");

    public static final DataFlavor[] flavors = {ImageIconTransferable.imageIconFlavor};

    private ImageIcon icon;

    /** Creates new ImgeIconTransferable */
    public ImageIconTransferable(ImageIcon icon) {
        this.icon = icon;
    }

    public synchronized DataFlavor[] getTransferDataFlavors() {
	return flavors;
    }

    public boolean isDataFlavorSupported(DataFlavor flavor) {
	return flavor.equals(imageIconFlavor) ? true : false;
    }

    public synchronized Object getTransferData(DataFlavor flavor)
	    throws UnsupportedFlavorException, IOException {

        if (flavor.equals(imageIconFlavor)) {
            return icon;
        } else {
            throw new UnsupportedFlavorException(flavor);
        }
    }

    @Override
    public String toString() {
        return "Image Icon Transferable";
    }

    public void lostOwnership(Clipboard clipboard, Transferable contents) {
    }
}
