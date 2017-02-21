package open.dolphin.impl.care;

import java.awt.datatransfer.*;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import open.dolphin.infomodel.AppointmentModel;
import open.dolphin.ui.PNSTransferHandler;

/**
 * AppointLabel.
 * Draggable label.
 * @author Kauzshi Minagawa, Digital Globe, Inc.
 * @author pns
 */
public class AppointLabel extends JLabel {
    private static final long serialVersionUID = 2843710174202998473L;

    public AppointLabel(String text, Icon icon, int align) {
        super(text, icon, align);
        init();
    }

    private void init() {
        AppointTransferHandler th = new AppointTransferHandler();
        setTransferHandler(th);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                th.exportAsDrag(AppointLabel.this, e, AppointTransferHandler.COPY);
            }
        });
    }

    private class AppointTransferHandler extends PNSTransferHandler {
        private static final long serialVersionUID = 1L;

        public AppointTransferHandler() {
            super();
        }

        @Override
        protected Transferable createTransferable(JComponent c) {
            JLabel label = (JLabel)c;
            AppointmentModel appo = new AppointmentModel();
            appo.setName(label.getText());
            return new AppointEntryTransferable(appo);
        }

        @Override
        public int getSourceActions(JComponent c) {
            return COPY_OR_MOVE;
        }

        @Override
        public void exportAsDrag(JComponent comp, InputEvent e, int action) {
            Icon icon = ((JLabel)comp).getIcon();
            JLabel label = new JLabel(icon);
            label.setSize(icon.getIconWidth(), icon.getIconHeight());

            setDragImage(label);

            super.exportAsDrag(comp, e, action);
        }
    }
}
