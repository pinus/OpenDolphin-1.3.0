package open.dolphin.client;

import java.awt.datatransfer.*;
import java.io.*;

/**
 * Transferable class of the Icon list.
 *
 * @author  Kazushi Minagawa, Digital Globe, Inc.
 */
public final class SchemaListTransferable implements Transferable, ClipboardOwner {

    /** Data Flavor of this class */
    public static DataFlavor schemaListFlavor = new DataFlavor(open.dolphin.client.SchemaList.class, "Schema List");
    private static final DataFlavor[] flavors = {SchemaListTransferable.schemaListFlavor};

    private final SchemaList list;

    /**
     * Creates new SchemaListTransferable.
     * @param list
     */
    public SchemaListTransferable(SchemaList list) {
        this.list = list;
    }

    @Override
    public synchronized DataFlavor[] getTransferDataFlavors() {
	return flavors;
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
	return flavor.equals(schemaListFlavor) ? true : false;
    }

    @Override
    public synchronized Object getTransferData(DataFlavor flavor)
	    throws UnsupportedFlavorException, IOException {

        if (flavor.equals(schemaListFlavor)) {
            return list;
        } else {
            throw new UnsupportedFlavorException(flavor);
        }
    }

    @Override
    public String toString() {
        return "Icon List Transferable";
    }

    @Override
    public void lostOwnership(Clipboard clipboard, Transferable contents) {
    }
}
