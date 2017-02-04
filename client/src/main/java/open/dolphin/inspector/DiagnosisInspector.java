package open.dolphin.inspector;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.dnd.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TooManyListenersException;
import javax.swing.*;
import open.dolphin.client.*;
import open.dolphin.helper.MenuActionManager;
import open.dolphin.helper.MenuActionManager.MenuAction;
import open.dolphin.infomodel.ModelUtils;
import open.dolphin.infomodel.RegisteredDiagnosisModel;
import open.dolphin.ui.MyJScrollPane;
import open.dolphin.ui.PNSBorder;
import org.apache.log4j.Logger;

/**
 * インスペクタに病名を表示するクラス.
 * @author pns
 */
public class DiagnosisInspector implements IInspector {
    public static final InspectorCategory CATEGORY = InspectorCategory.病名;

    /** 呼び元の ChartImpl */
    private final ChartImpl context;
    /** PatientInspector に返すパネル */
    private JPanel diagPanel;
    /** 病名を保持するリスト */
    private JList<RegisteredDiagnosisModel> diagList;
    /** 病名を保持するモデル */
    private DefaultListModel<RegisteredDiagnosisModel> listModel;
    /** DiagnosisDocument */
    private DiagnosisDocument doc;
    /** ListSelectionLisner 循環呼び出し lock */
    private boolean locked = false;

    private static final String SUSPECT = " 疑い";
    private final Logger logger;

    /**
     * ショートカットキー定義.
     * MenuSupport には依存せず，全て独自実装している.
     */
    private enum Shortcut{
        undo (KeyEvent.VK_Z, InputEvent.META_DOWN_MASK),
        redo (KeyEvent.VK_Z, InputEvent.META_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK),
        addLeft (KeyEvent.VK_S, InputEvent.SHIFT_DOWN_MASK), // sinistro
        addRight (KeyEvent.VK_D, InputEvent.SHIFT_DOWN_MASK), // destro
        addBoth (KeyEvent.VK_E, InputEvent.SHIFT_DOWN_MASK), // entrambi
        finish (KeyEvent.VK_F, 0),
        discontinue (KeyEvent.VK_D, 0),
        renew (KeyEvent.VK_R, 0),
        dropPrepos (KeyEvent.VK_T, 0), // togliere
        dropPostpos (KeyEvent.VK_T, InputEvent.SHIFT_DOWN_MASK), // togliere
        delete (KeyEvent.VK_BACK_SPACE, 0),
        sendClaim (KeyEvent.VK_L, InputEvent.META_DOWN_MASK),
        duplicate (KeyEvent.VK_D, InputEvent.META_DOWN_MASK),
        suspected (KeyEvent.VK_U, 0), // utagai
        mainDiag (KeyEvent.VK_U, InputEvent.SHIFT_DOWN_MASK),
        infection (KeyEvent.VK_I, 0),
        ;
        private final int key, mask;
        private Shortcut(int k, int m) { key = k; mask = m; }
        public int key() { return key; }
        public int mask() { return mask; }
    }

    /**
     * DiagnosisInspectorオブジェクトを生成する.
     * @param parent
     */
    public DiagnosisInspector(PatientInspector parent) {

        context = parent.getContext();
        logger = ClientContext.getBootLogger();
        initComponents();
    }

    /**
     * GUI コンポーネントを初期化する.
     */
    private void initComponents() {

        listModel = new DefaultListModel<>();

        diagList = new JList<RegisteredDiagnosisModel>(listModel) {
            private static final long serialVersionUID = 1L;
            @Override
            public String getToolTipText(MouseEvent e) {
                int index = locationToIndex(e.getPoint());
                RegisteredDiagnosisModel rd = listModel.getElementAt(index);
                String startDate = rd.getStartDate();
                String endDate = rd.getEndDate();
                String text = "開始日 " + ModelUtils.toNengo(startDate);
                if (endDate != null) {
                    text += String.format(" (終了日 %s)", ModelUtils.toNengo(endDate));
                }

                super.getToolTipText();
                return text;
            }
        };

        diagList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        diagList.putClientProperty("Quaqua.List.style", "striped");
        diagList.setCellRenderer(new DiagnosisListCellRenderer());
        diagList.setFixedCellHeight(GUIConst.DEFAULT_LIST_ROW_HEIGHT);
        diagList.setTransferHandler(new DiagnosisInspectorTransferHandler(context));

        // 複数選択しているとき，focus の変更で１つの項目しか repaint されないのの workaround
        diagList.addFocusListener(new FocusListener(){
            @Override
            public void focusGained(FocusEvent e) {
                diagList.repaint();
            }
            @Override
            public void focusLost(FocusEvent e) {
                diagList.repaint();
            }
        });

        // 右クリックでポップアップを表示する
        // ダブルクリックでエディタを立ち上げる
        if (!context.isReadOnly()) {
            diagList.addMouseListener(new MouseAdapter(){
                @Override
                public void mousePressed(MouseEvent e) {
                    Point mousePoint = e.getPoint();
                    int index = diagList.locationToIndex(mousePoint);
                    if (index == -1) {
                        // 診断が一つもないときはこっちに入る
                        // ダブルクリックで，エディタを立ち上げる
                        if (e.getClickCount() == 2) {
                            doc.openEditor2();
                        }
                        return;
                    }

                    Point indexPoint = diagList.indexToLocation(index);
                    // あまりマウスが離れたところをクリックしてたらクリアする
                    if (indexPoint != null && Math.abs((indexPoint.y+6) - mousePoint.y) > 12) {
                        diagList.clearSelection();
                    }
                    // ポップアップメニュー
                    if (e.isPopupTrigger()) { maybeShowPopup(e); }

                    // 診断が一つでもある場合はこちらに入る
                    // ダブルクリックならエディタを立ち上げる
                    else if (e.getClickCount() == 2) {

                        int sel = diagList.getSelectedIndex();
                        if (sel < 0) {
                            // 項目のないところダブルクリックした場合
                            doc.openEditor2();
                        } else {
                            // 項目があるところをダブルクリックした場合
                            RegisteredDiagnosisModel model = listModel.get(sel);
                            doc.openEditor3(model);
                        }
                    }
                }
                @Override
                public void mouseReleased(MouseEvent e) {
                    if (e.isPopupTrigger()) { maybeShowPopup(e); }
                }
                private void maybeShowPopup(MouseEvent e) {
                    if ((e.getModifiers() & InputEvent.ALT_MASK) != 0) {
                        // option キーを押していたら category：主病名，疑い病名
                        doc.getDiagnosisDocumentPopup().getCategoryPopup().show(diagList, e.getX(), e.getY());
                    } else {
                        // 右端の方が押されていたら outcome：全治，中止
                        if (e.getX() > diagList.getWidth() - 48) {
                            doc.getDiagnosisDocumentPopup().getOutcomePopup().show(diagList, e.getX(), e.getY());
                        }
                        // それ以外は病名修飾
                        else {
                            doc.getDiagnosisDocumentPopup().getDiagPopup().show(diagList, e.getX(), e.getY());
                        }
                    }
                }
            });
        }

        // ショートカットキー登録
        ActionMap map = MenuActionManager.getActionMap(this);
        InputMap im = diagList.getInputMap();
        ActionMap am = diagList.getActionMap();

        Arrays.asList(Shortcut.values()).forEach(shortcut -> {
            im.put(KeyStroke.getKeyStroke(shortcut.key(), shortcut.mask()), shortcut.name());
            am.put(shortcut.name(), map.get(shortcut.name()));
        });

        // GUI 形成
        diagPanel = new DropPanel(new BorderLayout());
        diagPanel.setName(CATEGORY.name());
        diagPanel.setPreferredSize(new Dimension(DEFAULT_WIDTH, 100));
        diagPanel.setMinimumSize(new Dimension(DEFAULT_WIDTH, 100));

        MyJScrollPane scrollPane = new MyJScrollPane(diagList);
        scrollPane.putClientProperty("JComponent.sizeVariant", "small");
        diagPanel.add(scrollPane);

        // タイトル部分のダブルクリックで新規病名入力
        diagPanel.addMouseListener(new MouseAdapter(){
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    doc.openEditor2();
                }
            }
        });

        // ここで普通に ChartImpl#getDiagnosisDocument() を呼ぶと，遅延形成される ChartImpl#loadDocuments() が終了していないため
        // null が返ってきてしまう. そこで，ChartImpl#getDiagnosisDocument() に loadDocuments() が終了するまで待ってもらうように
        // ロックをかけるようにして，こちらの呼び出しもスレッドにして止まって待てるようにしている.

        Thread t = new Thread(){
            @Override
            public void run() {
                // ここで初めて DiagnosisDocument の実体が現れる
                doc = context.getDiagnosisDocument();

                // DiagnosisInspector の list と DiagnosisDocument の table の選択範囲を一致させる
                DiagnosisDocumentTable table = doc.getDiagnosisTable();
                DiagnosisDocumentTableModel model = (DiagnosisDocumentTableModel) table.getModel();
                ListSelectionModel selectionModel = table.getSelectionModel();

                diagList.addListSelectionListener(e -> {
                    if (locked) { return; }
                    locked = true;

                    selectionModel.clearSelection();

                    diagList.getSelectedValuesList().forEach(o -> {
                        for(int i=0; i<model.getObjectCount(); i++) {
                            if (model.getObject(i).equals(o)) {
                                int row = table.convertRowIndexToView(i);
                                selectionModel.addSelectionInterval(row,row);
                            }
                        }
                    });
                    locked = false;
                });
                selectionModel.addListSelectionListener(e -> {
                    if (locked) { return; }
                    locked = true;

                    diagList.clearSelection();

                    int[] rows = table.getSelectedRows();
                    for (int view : rows) {
                        int row = table.convertRowIndexToModel(view);
                        for (int i=0; i<diagList.getModel().getSize(); i++) {
                            if (diagList.getModel().getElementAt(i).equals(model.getObject(row))) {
                                diagList.addSelectionInterval(i, i);
                            }
                        }
                    }
                    locked = false;
                });
            }
        };
        t.start();
    }

    /**
     * PaientInspector にレイアウト用のパネルを返す.
     * @return レイアウトパネル
     */
    @Override
    public JPanel getPanel() {
        return diagPanel;
    }

    @Override
    public String getName() {
        return CATEGORY.name();
    }

    @Override
    public String getTitle() {
        return CATEGORY.title();
    }

    public JList<RegisteredDiagnosisModel> getList() {
        return diagList;
    }

    /**
     * このインスペクタは DiagnosisDocument に依存しているので，自分で update できない.
     */
    @Override
    public void update() {}

    /**
     * データのアップデート.
     * DiagnosisDocument から呼ばれる.
     * @param model
     */
    public void update(DiagnosisDocumentTableModel model) {
        // model から，endDate の有無でリストを分ける
        List<RegisteredDiagnosisModel> active = new ArrayList<>();
        List<RegisteredDiagnosisModel> ended = new ArrayList<>();

        model.getObjectList().forEach(o -> {
            RegisteredDiagnosisModel rd = o;
            if (rd.getEndDate() == null) { active.add(rd); }
            else { ended.add(rd); }
        });
        // 選択を保存　hashCode を保存しておく
        List<Integer> selected = new ArrayList<>();
        for (int r : diagList.getSelectedIndices()) {
            selected.add(System.identityHashCode(listModel.get(r)));
        }
        // listModel にセット
        listModel.clear();
        for (int i=0; i < active.size(); i++) { listModel.addElement(active.get(i)); }
        for (int i=0; i < ended.size(); i++) { listModel.addElement(ended.get(i)); }

        // 選択を復元　hashCode で同じオブジェクトを判定
        selected.forEach(h -> {
            for(int i=0; i<listModel.getSize(); i++) {
                if (h == System.identityHashCode(listModel.get(i))) {
                    diagList.addSelectionInterval(i, i);
                }
            }
        });
    }

    private class DiagnosisListCellRenderer extends DefaultListCellRenderer {
        private static final long serialVersionUID = 1L;

        @Override
        public Component getListCellRendererComponent(
            JList<?> list,           // the diagList
            Object value,            // value to display
            int index,               // cell index
            boolean isSelected,      // is the cell selected
            boolean isFocused)  { // does the cell have focus

            RegisteredDiagnosisModel rd = (RegisteredDiagnosisModel) value;
            String diagName = rd.getDiagnosis();
            // 疑いの場合
            if (DiagnosisDocument.SUSPECTED_DIAGNOSIS.equals(rd.getCategoryDesc())) {
                diagName += SUSPECT;
            }

            boolean deleted = DiagnosisDocument.DELETED_RECORD.equals(rd.getStatus());
            boolean ended = rd.getEndDate() != null;
            boolean ikou = DiagnosisDocument.IKOU_BYOMEI_RECORD.equals(rd.getStatus());

            if (isSelected) {
                // foreground
                if (deleted || ended) {
                    int rgb = list.getSelectionForeground().getRGB();
                    int adjust = 0x2f2f2f;
                    if ((rgb & 0x00ffffff) > adjust) { rgb -= adjust; }
                    if ((rgb & 0x00ffffff) < adjust) { rgb += adjust; }
                    setForeground(new Color(rgb));
                } else if (ikou) {
                    setForeground(DiagnosisDocument.IKOU_BYOMEI_COLOR);
                } else {
                    setForeground(list.getSelectionForeground());
                }
                // background
                setBackground(list.getSelectionBackground());
            } else {
                // foreground
                if (deleted) { setForeground(DiagnosisDocument.DELETED_COLOR); }
                else if (ended) { setForeground(DiagnosisDocument.ENDED_COLOR); }
                else if (ikou) { setForeground(DiagnosisDocument.IKOU_BYOMEI_COLOR); }
                else { setForeground(list.getForeground()); }
                // background
                setBackground(list.getBackground());
            }

            this.setText(" " + diagName);

            return this;
        }

        // 罫線を入れる
        @Override
        public void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics.create();

            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
            g.setColor(Color.WHITE);
            // Retina 対応
            //g.drawLine(0, getHeight()-1, getWidth(), getHeight()-1);
            g.drawLine(0, getHeight(), getWidth(), getHeight());
            g.dispose();
        }
    }

    /**
     * ドロップするとき，Border にフィードバックを出すパネル.
     */
    private class DropPanel extends JPanel {
        private static final long serialVersionUID = 1L;

        private boolean showFeedback = false;

        public DropPanel(LayoutManager layout) {
            super(layout);

            try {
                DropTarget dt = diagList.getDropTarget();
                dt.addDropTargetListener(new DropTargetListener() {
                    @Override
                    public void dragEnter(DropTargetDragEvent dtde) {
                        // ALT キーを押していると「疑い」が入力される
                        doc.setDropAction(dtde.getDropAction());
                        showFeedback = true;
                        repaint();
                    }

                    @Override
                    public void dropActionChanged(DropTargetDragEvent dtde) {
                        doc.setDropAction(dtde.getDropAction());
                    }

                    @Override
                    public void drop(DropTargetDropEvent dtde) {
                        showFeedback = false;
                        repaint();
                    }
                    @Override
                    public void dragOver(DropTargetDragEvent dtde) {}
                    @Override
                    public void dragExit(DropTargetEvent dte) {
                        showFeedback = false;
                        repaint();
                    }
                });
                dt.setActive(true);
            } catch (TooManyListenersException e) {}
        }

        @Override
        public void paintBorder(Graphics graphics) {
            super.paintBorder(graphics);
            if (showFeedback) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f));
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // cut and try
                int x = 4;
                int y = 16;
                int width = getWidth() - 2*x;
                int height = getHeight() - y - 3;
                PNSBorder.drawSelectedBlueRoundRect(this, g, x, y, width, height,10,10);
                g.dispose();
            }
        }
    }

    @MenuAction
    public void undo() {
        doc.undo();
    }
    @MenuAction
    public void redo() {
        doc.redo();
    }
    @MenuAction
    public void addLeft() {
        doc.getDiagnosisDocumentPopup().doClickDiagPopup("左");
    }
    @MenuAction
    public void addRight() {
        doc.getDiagnosisDocumentPopup().doClickDiagPopup("右");
    }
    @MenuAction
    public void addBoth() {
        doc.getDiagnosisDocumentPopup().doClickDiagPopup("両");
    }
    @MenuAction
    public void finish() {
        doc.getDiagnosisDocumentPopup().doClickOutcomePopup("全治");
    }
    @MenuAction
    public void discontinue() {
        doc.getDiagnosisDocumentPopup().doClickOutcomePopup("中止");
    }
    @MenuAction
    public void renew() {
        doc.getDiagnosisDocumentPopup().doClickOutcomePopup("");
    }
    @MenuAction
    public void delete() {
        doc.delete();
    }
    @MenuAction
    public void sendClaim() {
        doc.sendClaim();
    }
    @MenuAction
    public void duplicate(){
        doc.duplicateDiagnosis();
    }
    @MenuAction
    public void dropPrepos(){
        doc.getDiagnosisDocumentPopup().dropPreposition();
    }
    @MenuAction
    public void dropPostpos(){
        doc.getDiagnosisDocumentPopup().dropPostposition();
    }
    @MenuAction
    public void suspected() {
        doc.getDiagnosisDocumentPopup().doClickCategoryPopup("疑い病名");
    }
    @MenuAction
    public void mainDiag() {
        doc.getDiagnosisDocumentPopup().doClickCategoryPopup("主病名");
    }
    @MenuAction
    public void infection() {
        doc.getDiagnosisDocumentPopup().doClickDiagPopup("の二次感染");
    }
}
