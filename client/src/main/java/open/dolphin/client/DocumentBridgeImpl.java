package open.dolphin.client;

import open.dolphin.infomodel.DocInfoModel;
import open.dolphin.inspector.DocumentHistory;
import open.dolphin.ui.PNSScrollPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * 参照タブ画面を提供する Bridge クラス.
 * ChartImpl の ChartDocument plugin として登録されるのはこれ.
 * このクラスの scroller へカルテが表示される.
 * 実務をしているのは KarteDocumentViewer.
 *
 * @author kazushi Minagawa, Digital Globe, Inc.
 * @author pns
 */
public class DocumentBridgeImpl extends AbstractChartDocument {
    private static final String TITLE = "参 照";
    private final Logger logger = LoggerFactory.getLogger(DocumentBridgeImpl.class);

    // 文書表示クラスのインターフェイス
    private KarteDocumentViewer karteDocumentViewer;
    // Scroller
    private PNSScrollPane scroller;
    // 何も文書がないときは blank panel を出す
    private JPanel blankPanel;
    // エディタで編集した直後に呼ばれた場合，その docId を入れる
    private String editedDocId;

    public DocumentBridgeImpl() {
        initComponents();
    }

    private void initComponents() {
        setTitle(TITLE);

        // blankLabel をダブルクリックしたら，新規カルテ作成を呼ぶ
        blankPanel = new JPanel();
        blankPanel.setBackground(Color.white);
        blankPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    Chart context = getContext();
                    if (context instanceof ChartImpl) {
                        ((ChartImpl) context).newKarte();
                    }
                }
            }
        });
    }

    @Override
    public void start() {
        scroller = new PNSScrollPane();
        getUI().setLayout(new BorderLayout());
        getUI().add(scroller, BorderLayout.CENTER);

        // スクロールバーを常に表示しないと，スクロールバーが表示されるときにカルテがスクロールバー分伸びて尻切れになることがある
        scroller.setVerticalScrollBarPolicy(PNSScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        scroller.isPermanentScrollBar = true;

        // 文書履歴のプロパティ通知をリッスンする
        DocumentHistory h = getContext().getDocumentHistory();
        // 編集保存したとき
        h.addDocumentHistoryUpdateListener(this::documentHistoryUpdated);
        // 選択変更したとき
        h.addDocumentHistorySelectionListener(this::documentHistorySelected);

        karteDocumentViewer = new KarteDocumentViewer();
        karteDocumentViewer.setContext(getContext());
        karteDocumentViewer.start();
    }

    @Override
    public void stop() {
        if (karteDocumentViewer != null) {
            karteDocumentViewer.stop();
        }
    }

    @Override
    public void enter() {
        if (karteDocumentViewer != null) {
            // これによりメニューは viewer で制御される
            karteDocumentViewer.enter();
        } else {
            super.enter();
        }
    }

    public void documentHistoryUpdated(String docId) {
        // 編集直後に来た場合は，編集カルテの docId が入っている.
        editedDocId = docId;
        // 文書履歴の抽出期間が変更された場合
        if (karteDocumentViewer != null) {
            karteDocumentViewer.historyPeriodChanged();
        }   // null の代わりに blankPanel を出す
        scroller.setViewportView(blankPanel);
    }

    public void documentHistorySelected(DocInfoModel[] models) {
        // 文書履歴の選択変更処理
        if (models == null || models.length == 0) { return; }
        if (karteDocumentViewer != null) {
            karteDocumentViewer.showDocuments(models, editedDocId, scroller);
        }
    }

    public KarteViewer2 getBaseKarte() {
        if (karteDocumentViewer != null) {
            return karteDocumentViewer.getBaseKarte();
        }
        return null;
    }
}
