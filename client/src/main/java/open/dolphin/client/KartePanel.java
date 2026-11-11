package open.dolphin.client;

import javax.swing.*;
import java.awt.event.MouseListener;

/// KartePanel abstract for KarteViewer and KarteEditor.
/// 実体は KartePanelFactory で作る.
///
/// @author pns
public abstract class KartePanel extends PrintablePanel {
    private String docId;
    private MouseListener mouseListener;

    public abstract KartePane getPKartePane();

    public abstract KartePane getSoaKartePane();

    public abstract JLabel getTimeStampLabel();

    public void initKartePane(boolean editable, ChartMediator mediator) {
        getSoaKartePane().init(editable, mediator);
        getPKartePane().init(editable, mediator);
    }

    public void setSelected(boolean selected) {
        getTimeStampLabel().setFont(selected
                ? KartePanelFactory.TIMESTAMP_FONT_BOLD
                : KartePanelFactory.TIMESTAMP_FONT);
    }

    public void setTimeStamp(String label) {
        getTimeStampLabel().setText(label);
    }

    public void setParent(ChartDocument parent) {
        getSoaKartePane().setParent(parent);
        getPKartePane().setParent(parent);
    }

    public void setDocId(String id) {
        docId = id;
        getSoaKartePane().setDocId(id);
        getPKartePane().setDocId(id);
    }

    ///  If the listener is null, no exception is thrown and no action is performed.
    public void setupMouseListener(MouseListener newListener) {
        getSoaKartePane().getTextPane().removeMouseListener(mouseListener);
        getPKartePane().getTextPane().removeMouseListener(mouseListener);
        getSoaKartePane().getTextPane().addMouseListener(newListener);
        getPKartePane().getTextPane().addMouseListener(newListener);
        mouseListener = newListener;
    }

    public void removeMouseListener() {
        getSoaKartePane().getTextPane().removeMouseListener(mouseListener);
        getPKartePane().getTextPane().removeMouseListener(mouseListener);
        mouseListener = null;
    }

    public void stopKartePane() {
        getSoaKartePane().stop();
        getPKartePane().stop();
    }
}
