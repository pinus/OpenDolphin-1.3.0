package open.dolphin.client;

import open.dolphin.helper.TextComponentUndoManager;
import open.dolphin.ui.CompletableSearchField;

import javax.swing.*;
import java.awt.*;
import java.util.prefs.Preferences;

/**
 * The ChartSearchPanel class is a graphical component for searching within a
 * medical chart application. This panel allows users to search for diagnoses
 * or documents within the chart, depending on the selected mode.
 *
 * @author pns
 */
public class ChartSearchPanel extends JPanel {
    public enum Mode {
        STAMP("病名検索", Preferences.userNodeForPackage(ChartToolBar.class).node(ChartImpl.class.getName())),
        KARTE("カルテ検索", Preferences.userNodeForPackage(KarteDocumentViewer.class).node(KarteDocumentViewer.class.getName()));
        private final String label;
        private final Preferences pref;
        Mode(String label, Preferences pref) {
            this.label = label;
            this.pref = pref;
        }
    }

    private Mode mode;
    private final ChartImpl chart;
    private FindAndView findAndView;
    private JPanel scrollerPanel;
    private CompletableSearchField searchField;

    /**
     * Constructs a ChartSearchPanel with the given chart context. This initializes
     * the components needed for the search panel and establishes necessary connections.
     *
     * @param ctx the ChartImpl instance providing context for the search panel
     */
    public ChartSearchPanel(ChartImpl ctx) {
        chart = ctx;
        initComponents();
        connect();
    }

    /**
     * Initializes the components for the ChartSearchPanel.
     * This method creates and configures the search field, adds undo functionality,
     * and sets its size preferences. It then adds the search field to the panel and
     * sets the initial mode of the panel to Mode.KARTE.
     *
     * The search field is an instance of CompletableSearchField, initialized with
     * a column width of 15. An undoable edit listener is attached to manage text
     * changes, allowing for undo operations. The preferred size of the search field
     * is set, although the width is determined by the column setting of the JTextField.
     */
    private void initComponents() {
        searchField = new CompletableSearchField(15);
        searchField.getDocument().addUndoableEditListener(TextComponentUndoManager.createManager(searchField));
        searchField.setPreferredSize(new Dimension(300, 26)); // width は JTextField の columns が優先される
        add(searchField);
        setMode(Mode.KARTE);
    }

    /**
     * Establishes action listeners and input mappings for the search functionality within the ChartSearchPanel.
     * This method binds actions to the search field, allowing for interactions based on user input. It also
     * configures key combinations to trigger search functions.
     *
     * The connection is conditional based on the current mode of the panel:
     * - In STAMP mode, it creates and displays a diagnosis popup based on the search pattern derived from user input.
     * - In KARTE mode, it processes options for searching specific aspects of the chart document, such as including
     *   subjective/objective (s) and plan (p) sections, and then triggers an appropriate search display.
     *
     * Additionally, adjusts the input map of the search field to treat Ctrl+Enter as equivalent to Enter, allowing
     * for consistent input handling.
     */
    private void connect() {
        searchField.addActionListener(e -> {
            String keyWord = searchField.getText();
            if (keyWord == null || keyWord.isEmpty()) { return; }

            if (mode == Mode.STAMP) {
                String pattern = ".*" + keyWord.trim() + ".*";
                JPopupMenu popup = chart.getChartMediator().createDiagnosisPopup(pattern, ev -> {
                    JComponent c = chart.getDiagnosisDocument().getDiagnosisTable();
                    TransferHandler handler = c.getTransferHandler();
                    handler.importData(new TransferHandler.TransferSupport(c, ev.getTransferable()));
                    // transfer 後にキーワードフィールドをクリアする
                    searchField.setText("");
                    // ATOK のフォーカスが残るのを防ぐ
                    KeyboardFocusManager.getCurrentKeyboardFocusManager().clearGlobalFocusOwner();
                });
                if (popup.getComponentCount() != 0) {
                    popup.show(searchField,0, searchField.getHeight());
                }
            } else { // Mode.KARTE
                String[] option = keyWord.split(":");
                boolean searchSoa = true;
                boolean searchP = true;
                if (option.length == 2) {
                    searchSoa = option[0].trim().startsWith("s") || option[0].trim().startsWith("S");
                    searchP = option[0].trim().startsWith("p") || option[0].trim().startsWith("P");
                    keyWord = option[1];
                }
                findAndView.showFirst(keyWord.trim(), searchSoa, searchP, scrollerPanel);
            }
        });

        // ctrl-return でもリターンキーの notify-field-accept が発生するようにする
        InputMap map = searchField.getInputMap();
        Object value = map.get(KeyStroke.getKeyStroke("ENTER"));
        map.put(KeyStroke.getKeyStroke("ctrl ENTER"), value);
    }

    /**
     * Sets the current mode of the ChartSearchPanel. This method updates the
     * search field's label and preferences based on the new mode, and applies
     * a visual effect if the search field contains text.
     *
     * @param mode The new mode to set. This determines which label and preferences
     *             are applied to the search field.
     */
    public void setMode(Mode mode) {
        if (mode == this.mode) { return; }
        this.mode = mode;
        searchField.setLabel(mode.label);
        searchField.setPreferences(mode.pref);
        searchField.setToolTipText(mode.label);
        // 視覚効果
        String text = searchField.getText();
        if (text != null && !text.isEmpty()) {
            showVisualEffect();
        }
    }

    /**
     * This method is intended to create a visual effect that enhances user feedback,
     * particularly by showing a tooltip without the usual delay.
     */
    private void showVisualEffect() {
        ToolTipManager tip = ToolTipManager.sharedInstance();
        int initDelay = tip.getInitialDelay();
        tip.setInitialDelay(0); // 遅延をゼロに
        tip.mouseMoved(
            new java.awt.event.MouseEvent(
                searchField, java.awt.event.MouseEvent.MOUSE_MOVED, System.currentTimeMillis(),
                0, 0, 10, 0, false
            )
        );
        tip.setInitialDelay(initDelay); // 遅延を戻す
    }

    /**
     * Returns the CompletableSearchField used for enabling search functionality
     * within the ChartSearchPanel.
     *
     * @return the CompletableSearchField that displays a search icon and label
     * when no input is provided and hides them once input starts.
     */
    public CompletableSearchField getSearchField() {
        return searchField;
    }

    /**
     * カルテ検索用のパラメータを設定する.
     * @param fav FindAndView
     * @param scroller JPanel
     */
    public void setParams(FindAndView fav, JPanel scroller) {
        findAndView = fav;
        scrollerPanel = scroller;
    }
}
