package open.dolphin.order.tablepanel;

import open.dolphin.order.MasterItem;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

/**
 * マスタアイテム Transferable クラス.
 * @author kazm
 */
public class MasterItemTransferable implements Transferable {

    public static final DataFlavor masterItemFlavor = new DataFlavor(MasterItem.class, "MasterItem");
    private static final DataFlavor[] flavors = { masterItemFlavor };
    private final MasterItem masterItem;

    public MasterItemTransferable(MasterItem masterItem) {
        this.masterItem = masterItem;
    }

    @Override
    public synchronized DataFlavor[] getTransferDataFlavors() {
        return flavors;
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return flavor.equals(masterItemFlavor);
    }

    @Override
    public synchronized Object getTransferData(DataFlavor flavor)
            throws UnsupportedFlavorException, IOException {

        if (flavor.equals(masterItemFlavor)) {
            return masterItem;
        } else {
            throw new UnsupportedFlavorException(flavor);
        }
    }
}
