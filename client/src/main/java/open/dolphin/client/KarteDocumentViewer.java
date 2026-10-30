package open.dolphin.client;

import open.dolphin.delegater.DocumentDelegater;
import open.dolphin.delegater.OrcaDelegater;
import open.dolphin.helper.ChartTask;
import open.dolphin.infomodel.DocInfoModel;
import open.dolphin.infomodel.DocumentModel;
import open.dolphin.infomodel.IInfoModel;
import open.dolphin.project.Project;
import open.dolphin.ui.CompletableSearchField;
import open.dolphin.ui.Focuser;
import open.dolphin.ui.PNSOptionPane;
import open.dolphin.ui.PNSScrollPane;
import open.dolphin.ui.sheet.JSheet;
import open.dolphin.util.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;
import java.util.prefs.Preferences;

/// KarteDocumentViewer.
/// KarteViewer2 をまとめて DocumentBridge の scroller に入れる.
///
/// @author Minagawa, Kazushi
/// @author pns
public class KarteDocumentViewer extends AbstractChartDocument {
    private static final String TITLE = "参 照";
    private final Logger logger = LoggerFactory.getLogger(KarteDocumentViewer.class);
    // このアプリケーションは文書履歴を複数選択することができる
    // このリストはそれに対応した KarteViewer(2号カルテ)を保持している
    // このリストの内容（KarteViewer)が一枚のパネルに並べて表示される
    private List<KarteViewer2> karteViewerList;
    // 上記パネル内でマウスで選択されているカルテ(karteViewer)
    // 前回処方を適用した新規カルテはこの選択されたカルテが元になる
    private KarteViewer2 selectedKarteViewer;
    // 選択された History の DocId をキーとした Map
    private Map<String, DocInfoModel> selectedHistoryMap;
    // karteViewer を並べたパネル
    private JPanel scrollerPanel;
    // 文書履を昇順で表示する場合に true
    private boolean ascending;
    // 文書の修正履歴を表示する場合に true
    private boolean showModified;
    // このクラスの状態マネージャ
    private StateMgr stateMgr;
    // 今日の日付 2008-02-12 形式
    private String todayDateAsString;
    // 検索用
    private FindAndView findAndView;
    // showDocument の Worker
    private KarteDocumentViewerWorker currentTask;

    /// DocumentViewerオブジェクトを生成する.
    public KarteDocumentViewer() {
        super();
        initComponents();
    }

    private void initComponents() {
        setTitle(TITLE);
        todayDateAsString = LocalDate.now().format(DateUtils.ISO_DATE_FORMATTER);
        Preferences prefs = Project.getPreferences();
        boolean isVerticalScroll = prefs.getBoolean(Project.KARTE_SCROLL_DIRECTION, true);

        scrollerPanel = new JPanel();
        scrollerPanel.setLayout(new BoxLayout(scrollerPanel, isVerticalScroll? BoxLayout.Y_AXIS : BoxLayout.X_AXIS));
        selectedHistoryMap = new HashMap<>();
    }

    @Override
    public void setContext(Chart ctx) {
        super.setContext(ctx);

        // カルテ検索のためのパラメータセット
        ChartImpl chart = (ChartImpl) ctx;
        ChartSearchPanel panel = chart.getChartSearchPanel();
        findAndView = new FindAndView(chart);
        panel.setParams(findAndView, scrollerPanel);
    }

    /// 表示されているカルテの中身を検索する
    public void findFirst() {
        ChartImpl chart = (ChartImpl) getContext();
        ChartSearchPanel panel = chart.getChartSearchPanel();
        panel.setMode(ChartSearchPanel.Mode.KARTE);

        CompletableSearchField searchField = panel.getSearchField();
        //searchField.setText("");
        Focuser.requestFocus(searchField);
    }

    public void findNext() {
        findAndView.showNext(scrollerPanel);
    }

    public void findPrevious() {
        findAndView.showPrevious(scrollerPanel);
    }

    /// スタンプ標準表示/簡易表示を切り替える.
    public void simplifyStamp() {
        //toggleSimplifyStamp(selectedKarte.getPPane().getDocument());
        karteViewerList.stream().map(KarteViewer2::getPPane).map(KartePane::getDocument).forEach(this::toggleSimplifyStamp);
    }

    /// "全てを選択" の動作.
    public void selectAll() {
        this.getContext().getDocumentHistory().selectAll();
    }

    @Override
    public void start() {
        // 文書履歴に昇順／降順，修正履歴表示の設定をする
        // この値の初期値はデフォルト値であり，個々のドキュメント（画面）単位にメニューで変更できる. （適用されるのは個々のドキュメントのみ）
        // デフォルト値の設定は環境設定で行う.
        ascending = getContext().getDocumentHistory().isAscending();
        showModified = getContext().getDocumentHistory().isShowModified();
        karteViewerList = new ArrayList<>();
        stateMgr = new StateMgr();

        // 保存されていない編集中カルテチェック
        Autosave.checkForTemporaryFile((ChartImpl) getContext());

        enter();
    }

    @Override
    public void stop() {
        if (karteViewerList != null) {
            karteViewerList.stream().forEach(KarteViewer2::stop);
            karteViewerList.clear();
        }
    }

    @Override
    public void enter() {
        super.enter();
        stateMgr.enter();
    }

    /// 選択されている KarteViewer を返す.
    /// その KarteViewer が karteViewerList 中になければ null に設定して返す
    ///
    /// @return 選択されているKarteViwer
    public KarteViewer2 getSelectedKarteViewer() {
        if (karteViewerList.stream().noneMatch(kv -> kv == selectedKarteViewer)) {
            selectedKarteViewer = null;
        }
        return selectedKarteViewer;
    }

    /// マウスクリック(選択)された KarteViewer を selectedKarte に設定する.
    /// 他のカルテが選択されている場合はそれを解除する.
    /// StateMgrを HasKarte State にする.
    ///
    /// @param view 選択された KarteViewer
    public void setSelectedKarteViewer(KarteViewer2 view) {

        KarteViewer2 old = getSelectedKarteViewer();
        selectedKarteViewer = view;
        //
        // 他のカルテが選択されている場合はそれを解除する
        //
        if (selectedKarteViewer != old) {
            if (selectedKarteViewer != null) {
                karteViewerList.forEach(karte -> karte.setSelected(false));
                selectedKarteViewer.setSelected(true);
                stateMgr.processCleanEvent();

            } else {
                // null
                stateMgr.processEmptyEvent();
            }
        }
    }

    /// 新規カルテ作成の元になるカルテを返す.
    ///
    /// @return 作成の元になるカルテ
    public KarteViewer2 getBaseKarte() {
        KarteViewer2 selected = getSelectedKarteViewer();
        if (selected == null) {
            if (karteViewerList != null && !karteViewerList.isEmpty()) {
                selected = ascending ? karteViewerList.getLast() : karteViewerList.getFirst();
            }
        }
        return selected;
    }

    /// 文書履歴の抽出期間が変更された場合，
    /// karteList を clear, 選択されている karteViewer を解除, sateMgr  NoKarte 状態に設定する.
    public void historyPeriodChanged() {
        if (karteViewerList != null) { karteViewerList.clear(); }
        setSelectedKarteViewer(null);
        getContext().showDocument(0);
    }

    /// DocumentBridgeImpl から呼ばれる. KarteDocumentViewerWorker を呼び出して, カルテ表示を行う.
    ///
    /// @param selected 選択された文書情報 DocInfo 配列
    /// @param scroller PNSScrollPane
    public void showDocuments(DocInfoModel[] selected, final PNSScrollPane scroller) {
        getContext().showDocument(0);  // Chart のカルテ参照タブの選択

        scroller.setViewportView(scrollerPanel);
        if (selected == null || selected.length == 0) { return; }

        // 選択が変化していなければ帰る
        Set<String> selectedIds = new HashSet<>();
        for (DocInfoModel df : selected) {
            selectedIds.add(df.getDocId());
        }
        if (selectedHistoryMap.keySet().equals(selectedIds)) {
            logger.debug("selection unchanged");
            return;
        }

        // 選択された History の DocId をキーとした HashMap を作成
        selectedHistoryMap = new HashMap<>();
        for (DocInfoModel model : selected) {
            selectedHistoryMap.put(model.getDocId(), model);
        }

        // KarteTask でデータベースから DocumentModel を取ってきて EDT で addKarteViewer を呼ぶ
        if (currentTask != null && !currentTask.isDone()) {
            // 実行中のタスクがあればキャンセル
            currentTask.cancel(true);
        }
        currentTask = new KarteDocumentViewerWorker(this, karteViewerList, selectedHistoryMap, scrollerPanel);
        currentTask.execute();
    }

    /// カルテを修正する.
    public void modifyKarte() {
        logger.debug("modifyKarte() in KarteDocumentViewer starts");

        if (getBaseKarte() == null) {
            return;
        }

        ChartImpl chart = (ChartImpl) getContext();
        DocumentModel baseDocumentModel = getBaseKarte().getDocument();

        // すでに修正中の document があれば toFront するだけで帰る
        if (chart.toFrontDocumentIfPresent(baseDocumentModel)) {
            return;
        }
        // 削除フラグが設定されていたらenterしない
        if (IInfoModel.STATUS_DELETE.equals(baseDocumentModel.getDocInfo().getStatus())) {
            return;
        }

        DocumentModel editModel = chart.getKarteModelToEdit(baseDocumentModel);
        KarteEditor editor = chart.createEditor();
        editor.setDocument(editModel);
        editor.setEditable(true);
        editor.setModify(true);

        // このフラグはカルテを別ウインドウで編集するかどうか (使ってない)
        //if (Project.getPreferences().getBoolean(Project.KARTE_PLACE_MODE, true)) {
        EditorFrame editorFrame = new EditorFrame();
        editorFrame.setChart(getContext());
        editorFrame.setKarteEditor(editor);
        editorFrame.start();
        //} else {
        //    editor.setContext(chart);
        //    editor.initialize();
        //    editor.start();
        //    chart.addChartDocument(editor, TITLE_UPDATE);
        //}
    }

    /// masuda-sensei.
    @Override
    public void print() {

        // インスペクタに表示されているカルテをまとめて印刷する.
        // ブザイクなんだけど，あまり使わない機能なのでこれでヨシとする masuda
        // modified by pns
        // 背景色が緑だとインクがもったいないので白にする. 選択も解除しておく.
        karteViewerList.forEach((kv) -> {
            //kv.panel2.setBorder(BorderFactory.createEmptyBorder());
            KartePane kp = kv.getSOAPane();
            kp.getTextPane().setBackground(Color.WHITE);
            kp = kv.getPPane();
            kp.getTextPane().setBackground(Color.WHITE);
        });

        // 患者名を取得
        String name = getContext().getPatient().getFullName();
        String id = getContext().getPatient().getPatientId();
        // scrollerPanelを印刷する
        PrintKarteDocumentView.printComponent(scrollerPanel, name, id);

        // 背景色を戻しておく
        karteViewerList.forEach(kv -> {
            //kv.panel2.setBorder(MyBorderFactory.createClearBorder());
            if (kv.isSelected()) {
                setSelectedKarteViewer(kv);
            }
            KartePane kp = kv.getSOAPane();
            kp.getTextPane().setBackground(KartePane.UNEDITABLE_COLOR);
            kp = kv.getPPane();
            kp.getTextPane().setBackground(KartePane.UNEDITABLE_COLOR);
        });
    }

    /// 昇順表示にする.
    public void ascending() {
        ascending = true;
        getContext().getDocumentHistory().setAscending(ascending);
    }

    /// 降順表示にする.
    public void descending() {
        ascending = false;
        getContext().getDocumentHistory().setAscending(ascending);
    }

    /// 修正履歴の表示モードにする.
    public void showModified() {
        showModified = !showModified;
        getContext().getDocumentHistory().setShowModified(showModified);
    }

    /// karteList 内でダブルクリックされたカルテ（文書）を EditorFrame で開く.
    public void openKarte() {
        // ダブルクリックで modifyKarte することにした (isReadOnly対応)
        if (!getContext().isReadOnly()) {
            modifyKarte();
        }
    }

    /// 表示選択されているカルテを論理削除する.
    /// 患者を間違えた場合等に履歴に表示されないようにするため.
    public void delete() {

        // 対象のカルテを得る
        KarteViewer2 delete = getBaseKarte();
        if (delete == null) {
            return;
        }

        // Dialog を表示し理由を求める
        String message = "このドキュメントを削除しますか ?   ";
        final JCheckBox box1 = new JCheckBox("作成ミス");
        final JCheckBox box2 = new JCheckBox("診察キャンセル");
        final JCheckBox box3 = new JCheckBox("その他");
        box1.setSelected(true);

        ActionListener al = e -> {
            if (!box1.isSelected() && !box2.isSelected() && !box3.isSelected()) {
                box3.setSelected(true);
            }
        };

        box1.addActionListener(al);
        box2.addActionListener(al);
        box3.addActionListener(al);

        Object[] msg = new Object[5];
        msg[0] = message;
        msg[1] = box1;
        msg[2] = box2;
        msg[3] = box3;
        msg[4] = new JLabel(" ");
        String deleteText = "削除する";
        String cancelText = (String) UIManager.get("OptionPane.cancelButtonText");

        int option = PNSOptionPane.showOptionDialog(
                this.getUI(),
                msg,
                ClientContext.getFrameTitle("ドキュメント削除"),
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.WARNING_MESSAGE,
                null,
                new String[]{deleteText, cancelText},
                cancelText);

        // System.out.println(option);

        // キャンセルの場合はリターンする
        if (option != 0) {
            return;
        }

        //
        // 削除する status = 'D'
        //
        long deletePk = delete.getDocument().getId();
        DocumentDelegater ddl = new DocumentDelegater();
        DeleteTask task = new DeleteTask(getContext(), deletePk, ddl);
        task.execute();
    }

    /// 表示されているカルテを CLAIM 送信する.
    public void sendClaim() {
        logger.info("sendClaim() called");
        Frame parent = getContext().getFrame();
        if (JSheet.isAlreadyShown(parent)) {
            logger.info("sendClaim() modal dialog already shown");
            return;
        }

        String message;
        int messageType;

        if (!Project.getSendClaim()) {
            message = "CLAIM を送信しない設定になっています";
            messageType = JOptionPane.ERROR_MESSAGE;

        } else {
            // claim を送るのはカルテだけ
            // getBaseKarte() は選択カルテを返す
            String docType = getBaseKarte().getDocument().getDocInfo().getDocType();
            if (!IInfoModel.DOCTYPE_KARTE.equals(docType)) {
                return;
            }

            ChartImpl chart = (ChartImpl) getContext();
            DocumentModel model = chart.getKarteModelToEdit(getBaseKarte().getDocument());
            model.setKarte(getContext().getKarte());
            model.getDocInfo().setConfirmDate(LocalDateTime.now());
            model.setCreator(Project.getUserModel());       // 送信者

            OrcaDelegater delegater = new OrcaDelegater();
            OrcaDelegater.Result result = delegater.sendDocument(model);

            if (result.equals(OrcaDelegater.Result.NO_ERROR)) {
                message = "ORCA に送信しました";
                messageType = JOptionPane.INFORMATION_MESSAGE;
            } else {
                message = "ORCA に送信できませんでした";
                messageType = JOptionPane.ERROR_MESSAGE;
            }
        }

        JSheet.showMessageDialog(parent, message, "", messageType);
    }

    /// State Interface.
    private interface BrowserState {
        void enter();
    }

    /// カルテの削除タスククラス.
    private class DeleteTask extends ChartTask<Boolean> {

        private final DocumentDelegater ddl;
        private final long docPk;

        public DeleteTask(Chart ctx, long docPk, DocumentDelegater ddl) {
            super(ctx);
            this.docPk = docPk;
            this.ddl = ddl;
        }

        @Override
        protected Boolean doInBackground() {
            logger.debug("DeleteTask started");
            ddl.deleteDocument(docPk);
            return ddl.isNoError();
        }

        @Override
        protected void succeeded(Boolean result) {
            logger.debug("DeleteTask succeeded");
            if (result) {
                Chart chart = (KarteDocumentViewer.this).getContext();
                chart.getDocumentHistory().update();
            } else {
                warning(ClientContext.getString("ドキュメント削除"), ddl.getErrorMessage());
            }
        }
    }

    /// StateContext クラス.
    private final class StateMgr {

        private final BrowserState emptyState = new EmptyState();
        private final BrowserState cleanState = new CleanState();

        private BrowserState currentState;

        public StateMgr() {
            currentState = emptyState;
        }

        public void processEmptyEvent() {
            currentState = emptyState;
            this.enter();
        }

        public void processCleanEvent() {
            currentState = cleanState;
            this.enter();
        }

        public void enter() {
            currentState.enter();
        }
    }

    /// EmptyState.
    /// 表示するカルテがない状態を表す.
    private final class EmptyState implements BrowserState {

        public EmptyState() {
        }

        @Override
        public void enter() {
            boolean canEdit = !isReadOnly();
            getContext().enabledAction(GUIConst.ACTION_NEW_KARTE, canEdit);     // 新規カルテ
            getContext().enabledAction(GUIConst.ACTION_NEW_DOCUMENT, canEdit);  // 新規文書
            getContext().enabledAction(GUIConst.ACTION_MODIFY_KARTE, false);    // 修正
            getContext().enabledAction(GUIConst.ACTION_DELETE, false);          // 削除
            getContext().enabledAction(GUIConst.ACTION_PRINT, false);           // 印刷
            getContext().enabledAction(GUIConst.ACTION_ASCENDING, false);       // 昇順
            getContext().enabledAction(GUIConst.ACTION_DESCENDING, false);      // 降順
            getContext().enabledAction(GUIConst.ACTION_SHOW_MODIFIED, false);   // 修正履歴表示

            getContext().enabledAction(GUIConst.ACTION_FIND_FIRST, false);
            getContext().enabledAction(GUIConst.ACTION_FIND_NEXT, false);
            getContext().enabledAction(GUIConst.ACTION_FIND_PREVIOUS, false);
            getContext().enabledAction(GUIConst.ACTION_SEND_CLAIM, false);
        }
    }

    /// CleanState.
    /// カルテが表示されている状態を表す.
    private final class CleanState implements BrowserState {

        public CleanState() {
        }

        @Override
        public void enter() {
            //
            // 新規カルテが可能なケース 仮保存でないことを追加
            //
            boolean canEdit = !isReadOnly();
            boolean tmpKarte = false;
            KarteViewer2 base = getBaseKarte();
            if (base != null) {
                String state = base.getDocument().getDocInfo().getStatus();
                String confirmDate = base.getDocument().getDocInfo().getConfirmDateTrimTime();

                // もし今日のカルテが一時保存なら新規カルテは作らない
                if (state.equals(IInfoModel.STATUS_TMP) && todayDateAsString.equals(confirmDate)) {
                    tmpKarte = true;
                }
            }
            boolean newOk = canEdit && !tmpKarte;
            getContext().enabledAction(GUIConst.ACTION_NEW_KARTE, newOk);        // 新規カルテ
            getContext().enabledAction(GUIConst.ACTION_NEW_DOCUMENT, canEdit);   // 新規文書
            getContext().enabledAction(GUIConst.ACTION_MODIFY_KARTE, canEdit);   // 修正
            getContext().enabledAction(GUIConst.ACTION_DELETE, canEdit);         // 削除
            getContext().enabledAction(GUIConst.ACTION_PRINT, true);             // 印刷
            getContext().enabledAction(GUIConst.ACTION_ASCENDING, true);         // 昇順
            getContext().enabledAction(GUIConst.ACTION_DESCENDING, true);        // 降順
            getContext().enabledAction(GUIConst.ACTION_SHOW_MODIFIED, true);     // 修正履歴表示

            getContext().enabledAction(GUIConst.ACTION_FIND_FIRST, true);
            getContext().enabledAction(GUIConst.ACTION_FIND_NEXT, false);
            getContext().enabledAction(GUIConst.ACTION_FIND_PREVIOUS, false);
            getContext().enabledAction(GUIConst.ACTION_SEND_CLAIM, true);
        }
    }
}
