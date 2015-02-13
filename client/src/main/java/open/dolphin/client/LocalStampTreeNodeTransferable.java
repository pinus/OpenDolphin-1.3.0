package open.dolphin.client;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

/**
 * Tranferable class of the StampTreeNode.
 *
 * @author  Kazushi Minagawa
 */
public class LocalStampTreeNodeTransferable implements Transferable {

    /** Data Flavor of this class */
    public static DataFlavor localStampTreeNodeFlavor;
    static {
    	try {
            localStampTreeNodeFlavor = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType + ";class=open.dolphin.client.StampTreeNode");
    	} catch (ClassNotFoundException e) {
            System.out.println("LocalStampTreeNodeTransferable.java: " + e);
    	}
    };
    	
    public static final DataFlavor[] flavors = {LocalStampTreeNodeTransferable.localStampTreeNodeFlavor};

    private StampTreeNode node;

    /** Creates new StampTreeTransferable */
    public LocalStampTreeNodeTransferable(StampTreeNode node) {
        this.node = node;
    }

    public synchronized DataFlavor[] getTransferDataFlavors() {
        return flavors;
    }

    public boolean isDataFlavorSupported(DataFlavor flavor)  {
        return flavor.equals(localStampTreeNodeFlavor) ? true : false;
    }

    public synchronized Object getTransferData(DataFlavor flavor)
	    throws UnsupportedFlavorException, IOException {

        if (flavor.equals(localStampTreeNodeFlavor)) {
            return node;
        } else {
            throw new UnsupportedFlavorException(flavor);
        }
    }
}
