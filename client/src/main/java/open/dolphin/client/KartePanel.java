package open.dolphin.client;

import javax.swing.*;

/// KartePanel abstract for KarteViewer and KarteEditor.
/// 実体は KartePanelFactory で作る.
///
/// @author pns
public abstract class KartePanel extends PrintablePanel {
    private String docId;

    public abstract KartePane getPKartePane();

    public abstract KartePane getSoaKartePane();

    public abstract JLabel getTimeStampLabel();

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

    public String getDocId() {
        return docId;
    }
}
