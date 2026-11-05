package open.dolphin.client;

import open.dolphin.dnd.KartePaneTransferHandler;
import open.dolphin.helper.GUIFactory;
import open.dolphin.infomodel.IInfoModel;
import open.dolphin.inspector.IInspector;
import open.dolphin.ui.PNSScrollPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;

/// KartePanel をつくるファクトリー.
/// KartePanel contains a TimeStamp panel on top and KartePane for SOA and P panes.
/// Each of KartePane contains JTextPane.
///
/// @author pns
public class KartePanelFactory {
    // タイムスタンプの高さ
    private static final int TIMESTAMP_PANEL_HEIGHT = 22; // 22で固定
    // タイムスタンプの foreground カラー
    private static final Color TIMESTAMP_FORE = Color.BLUE;
    // タイムスタンプのフォントサイズ
    private static final int TIMESTAMP_FONT_SIZE = 12;
    // タイムスタンプフォント
    public static final Font TIMESTAMP_FONT = GUIFactory.getFont(TIMESTAMP_FONT_SIZE);
    public static final Font TIMESTAMP_FONT_BOLD = GUIFactory.getBoldFont(TIMESTAMP_FONT_SIZE);
    // カルテフォント
    private static final Font KARTE_FONT = GUIFactory.getFont(Dolphin.forMac ? 13 : 12);
    // Text Pane のデフォルト幅
    private static final int TEXT_PANE_DEFAULT_WIDTH = 340;
    // Text Pane のマージン
    private static final Insets TEXT_PANE_MARGIN = new Insets(10, 10, 10, 10);
    // soaKartePane と pKartePane の隙間
    private static final int TEXT_PANE_GAP = 2;
    // KartePanel のデフォルト幅
    private static final int KARTE_PANE_DEFAULT_WIDTH = TEXT_PANE_DEFAULT_WIDTH * 2 + TEXT_PANE_GAP;

    private final KartePanel kartePanel;
    private KartePane soaKartePane;
    private KartePane pKartePane;
    private JLabel timeStampLabel;
    private JPanel textPanePanel;

    private final Logger logger = LoggerFactory.getLogger(KartePanelFactory.class);

    /// Construct KartePanel either for Editor or Viewer
    private KartePanelFactory(boolean editor) {
        kartePanel = (editor) ? new EditorPanel() : new ViewerPanel();
    }

    /// create KartePanel for Viewer
    public static KartePanel createViewerPanel() {
        return new KartePanelFactory(false).getProduct();
    }

    /// create KartePanel for Editor
    public static KartePanel createEditorPanel() {
        return new KartePanelFactory(true).getProduct();
    }

    /// できあがった KartePanel を返す
    private KartePanel getProduct() {
        return kartePanel;
    }

    /// Editor と Viewer の共通項目の初期化
    private void initComponents() {
        JTextPane soaTextPane = new JTextPane();
        soaTextPane.setFont(KARTE_FONT);
        soaTextPane.setEditorKit(new WrapEditorKit());
        soaTextPane.setMargin(TEXT_PANE_MARGIN);
        soaTextPane.setMinimumSize(new Dimension(TEXT_PANE_DEFAULT_WIDTH, 1));
        soaTextPane.setSize(TEXT_PANE_DEFAULT_WIDTH, Short.MAX_VALUE); // preferred size 計算に幅が必要
        soaKartePane = new KartePane(soaTextPane);
        soaKartePane.getTextPane().setTransferHandler(new KartePaneTransferHandler(soaKartePane));
        soaKartePane.setRole(IInfoModel.ROLE_SOA);

        JTextPane pTextPane = new JTextPane();
        pTextPane.setFont(KARTE_FONT);
        pTextPane.setEditorKit(new WrapEditorKit());
        pTextPane.setMargin(TEXT_PANE_MARGIN);
        pTextPane.setMinimumSize(new Dimension(TEXT_PANE_DEFAULT_WIDTH, 1));
        pTextPane.setSize(TEXT_PANE_DEFAULT_WIDTH, Short.MAX_VALUE);
        pKartePane = new KartePane(pTextPane);
        pKartePane.getTextPane().setTransferHandler(new KartePaneTransferHandler(pKartePane));
        pKartePane.setRole(IInfoModel.ROLE_P);

        timeStampLabel = new JLabel() {
            @Override
            public void paintBorder(Graphics g) {
                super.paintBorder(g);
                g.setColor(IInspector.BORDER_COLOR);
                g.drawLine(0, 0, getWidth() - 1, 0);
                g.drawLine(0, getHeight() - 1, getWidth() - 1, getHeight() - 1);
            }
        };
        timeStampLabel.setOpaque(true);
        timeStampLabel.setPreferredSize(new Dimension(KARTE_PANE_DEFAULT_WIDTH, TIMESTAMP_PANEL_HEIGHT));
        timeStampLabel.setBackground(IInspector.BACKGROUND);
        timeStampLabel.setHorizontalAlignment(SwingConstants.CENTER);
        timeStampLabel.setForeground(TIMESTAMP_FORE);
        timeStampLabel.setFont(TIMESTAMP_FONT);

        textPanePanel = new JPanel();
        textPanePanel.setOpaque(true);
        textPanePanel.setLayout(new GridLayout(1, TEXT_PANE_GAP, TEXT_PANE_GAP, 0));
        textPanePanel.setPreferredSize(new Dimension(KARTE_PANE_DEFAULT_WIDTH, 1));
    }

    /// Viewer 用の KartePanel.
    /// soaTextaPenel と soaTextPanel の中身の実際の高さの高い方を preferredSize として返す
    private class ViewerPanel extends KartePanel {

        public ViewerPanel() {
            initComponents();

            // workaround http://ron.shoutboot.com/2010/05/23/swing-jscrollpane-scrolls-to-bottom/
            soaKartePane.getTextPane().setCaret(new MyCaret());
            soaKartePane.getTextPane().setEditable(false);

            pKartePane.getTextPane().setCaret(new MyCaret());
            pKartePane.getTextPane().setEditable(false);

            textPanePanel.add(soaKartePane.getTextPane());
            textPanePanel.add(pKartePane.getTextPane());

            setLayout(new BorderLayout(0, 0));
            add(timeStampLabel, BorderLayout.NORTH);
            add(textPanePanel, BorderLayout.CENTER);
        }

        @Override
        public Dimension getPreferredSize() {
            Dimension d = super.getPreferredSize();
            d.height = Math.max(soaKartePane.getTextPane().getPreferredSize().height, pKartePane.getTextPane().getPreferredSize().height)
                    + TIMESTAMP_PANEL_HEIGHT + 10;
            return d;
        }

        @Override
        public KartePane getPKartePane() {
            return pKartePane;
        }

        @Override
        public KartePane getSoaKartePane() {
            return soaKartePane;
        }

        @Override
        public JLabel getTimeStampLabel() {
            return timeStampLabel;
        }
    }

    /// Editor 用の KartePanel.
    private class EditorPanel extends KartePanel {

        public EditorPanel() {
            initComponents();

            PNSScrollPane soaScrollPane = new PNSScrollPane();
            soaScrollPane.setViewportView(soaKartePane.getTextPane());
            soaScrollPane.setHorizontalScrollBarPolicy(PNSScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            soaScrollPane.setVerticalScrollBarPolicy(PNSScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

            PNSScrollPane pScrollPane = new PNSScrollPane();
            pScrollPane.setViewportView(pKartePane.getTextPane());
            pScrollPane.setHorizontalScrollBarPolicy(PNSScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            pScrollPane.setVerticalScrollBarPolicy(PNSScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

            textPanePanel.add(soaScrollPane);
            textPanePanel.add(pScrollPane);

            setLayout(new BorderLayout(0, 0));
            // Editor では timeStampPanel 廃止
            //add(timeStampPanel, BorderLayout.NORTH);
            add(textPanePanel, BorderLayout.CENTER);
        }

        @Override
        public KartePane getPKartePane() {
            return pKartePane;
        }

        @Override
        public KartePane getSoaKartePane() {
            return soaKartePane;
        }

        @Override
        public JLabel getTimeStampLabel() {
            return timeStampLabel;
        }
    }

    /// workaround caret moving to the end on opening
    /// [...](http://ron.shoutboot.com/2010/05/23/swing-jscrollpane-scrolls-to-bottom/)
    private static class MyCaret extends DefaultCaret {
        public MyCaret() {
            super();
            // EDT でなくても Caret を常に更新する
            setUpdatePolicy(ALWAYS_UPDATE);
        }
        @Override
        protected void adjustVisibility(Rectangle r) { }
    }

    /// workaround for long word wrapping
    /// [...](http://stackoverflow.com/questions/8666727/wrap-long-words-in-jtextpane-java-7)
    private class WrapEditorKit extends StyledEditorKit {
                ViewFactory defaultFactory = new WrapColumnFactory();

        @Override
        public ViewFactory getViewFactory() {
            return defaultFactory;
        }
    }

    private class WrapColumnFactory implements ViewFactory {
        @Override
        public View create(Element elem) {
            return switch(elem.getName()) {
                case AbstractDocument.ContentElementName -> new WrapLabelView(elem);
                case AbstractDocument.ParagraphElementName -> new ParagraphView(elem);
                case AbstractDocument.SectionElementName -> new BoxView(elem, View.Y_AXIS);
                case StyleConstants.ComponentElementName -> new ComponentView(elem);
                case StyleConstants.IconElementName -> new IconView(elem);
                case null, default -> new LabelView(elem);
            };
        }
    }

    private class WrapLabelView extends LabelView {
        public WrapLabelView(Element elem) {
            super(elem);
        }

        @Override
        public float getMinimumSpan(int axis) {
            return switch(axis) {
                case View.X_AXIS -> 0;
                case View.Y_AXIS -> super.getMinimumSpan(axis);
                default -> throw new IllegalArgumentException("Invalid axis: " + axis);
            };
        }
    }
}
