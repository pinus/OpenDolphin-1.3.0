package open.dolphin.stampbox;

import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

public class TransferAction implements ActionListener {

    private JComponent comp;
    private TransferHandler handler;
    private Transferable tr;

    public TransferAction(JComponent comp, TransferHandler handler, Transferable tr) {
        this.comp = comp;
        this.handler = handler;
        this.tr = tr;
    }

    public void actionPerformed(ActionEvent e) {
        handler.importData(comp, tr);
    }
}
