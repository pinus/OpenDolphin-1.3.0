package open.dolphin.dnd;

import open.dolphin.client.ImageEntry;
import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.datatransfer.Transferable;

/**
 * シェーマ箱の TransferHandler.
 */
public class ImageEntryTransferHandler extends DolphinTransferHandler {
    // ドラッグするセル
    private int row = -1, col = -1;

    /**
     * ドラッグ対象のセルを設定.
     *
     * @param r row
     * @param c column
     */
    public void setTargetRowColumn(int r, int c) { row = r; col = c; }

    @Override
    protected Transferable createTransferable(JComponent c) {
        JTable imageTable = (JTable) c;
        //int row = imageTable.getSelectedRow(); // ここだと間に合わなくてずれることあり
        //int col = imageTable.getSelectedColumn();
        ImageEntry entry = (row == -1 || col == -1) ? null
            : (ImageEntry) imageTable.getValueAt(row, col);
        return new ImageEntryTransferable(entry);
    }

    @Override
    public int getSourceActions(JComponent c) {
        JTable table = (JTable) c;
        //int row = table.getSelectedRow();
        //int column = table.getSelectedColumn();
        TableCellRenderer r = table.getCellRenderer(row, col);
        Object value = table.getValueAt(row, col);
        JLabel draggedComp = (JLabel) r.getTableCellRendererComponent(table, value, false, true, row, col);
        draggedComp.setSize(table.getColumnModel().getColumn(0).getWidth(), table.getRowHeight(row));
        setDragImage(draggedComp);

        return COPY_OR_MOVE;
    }

    // TODO: 未実装
    @Override
    public boolean canImport(TransferSupport support) {
        return false;
    }

    public boolean importData(TransferSupport support) {
        return false;
    }
}
