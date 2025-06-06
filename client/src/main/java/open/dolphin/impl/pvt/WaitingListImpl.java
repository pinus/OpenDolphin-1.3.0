package open.dolphin.impl.pvt;

import open.dolphin.client.*;
import open.dolphin.delegater.DolphinClientContext;
import open.dolphin.delegater.PvtDelegater;
import open.dolphin.dto.PvtStateSpec;
import open.dolphin.event.BadgeEvent;
import open.dolphin.event.ProxyAction;
import open.dolphin.helper.PNSTriple;
import open.dolphin.helper.ScriptExecutor;
import open.dolphin.helper.WindowHolder;
import open.dolphin.impl.orcon.OrcaController;
import open.dolphin.infomodel.IInfoModel;
import open.dolphin.infomodel.KarteState;
import open.dolphin.infomodel.PatientVisitModel;
import open.dolphin.project.Project;
import open.dolphin.stampbox.StampBoxPlugin;
import open.dolphin.ui.IndentTableCellRenderer;
import open.dolphin.ui.ObjectReflectTableModel;
import open.dolphin.ui.PNSBadgeTabbedPane;
import open.dolphin.ui.PNSOptionPane;
import open.dolphin.ui.sheet.JSheet;
import open.dolphin.util.Gengo;
import open.dolphin.util.ModelUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.*;
import java.util.concurrent.*;
import java.util.prefs.Preferences;

/**
 * 受付リスト.
 *
 * @author Kazushi Minagawa, Digital Globe, Inc.,
 * @author pns
 */
public class WaitingListImpl extends AbstractMainComponent {
    private static final String NAME = "受付リスト";

    // 来院情報テーブルの年齢カラム
    public static final int AGE_COLUMN = 5;
    public static final int BIRTHDAY_COLUMN = 6;
    // アイコン
    protected static final ImageIcon DONE_ICON = GUIConst.ICON_CHECK_LIGHTBLUE_16;
    protected static final ImageIcon OPEN_ICON = GUIConst.ICON_BOOK_OPEN_16;
    protected static final ImageIcon OPEN_READ_ONLY_ICON = GUIConst.ICON_BOOK_OPEN_16;
    protected static final ImageIcon UNFINISHED_ICON = GUIConst.ICON_CHECK_RED_16;
    protected static final ImageIcon TEMPORARY_ICON = GUIConst.ICON_ONEDIT_16;
    protected static final ImageIcon OPEN_USED_NONE = GUIConst.ICON_USER_WHITE_16;
    protected static final ImageIcon OPEN_USED_SAVE = GUIConst.ICON_USER_BLUE_16;
    protected static final ImageIcon OPEN_USED_UNFINISHED = GUIConst.ICON_USER_RED_16;
    protected static final Color SHOSHIN_COLOR = new Color(180, 220, 240); //青っぽい色
    protected static final Color KARTE_EMPTY_COLOR = new Color(250, 200, 160); //茶色っぽい色
    protected static final Color DIAGNOSIS_EMPTY_COLOR = new Color(243, 255, 15); //黄色
    // JTableレンダラ用のカラー
    private static final Color MALE_COLOR = new Color(230, 243, 243);
    private static final Color FEMALE_COLOR = new Color(254, 221, 242);
    private static final Color CANCEL_PVT_COLOR = new Color(128, 128, 128);
    // WaitingList の表示パネル
    private WaitingListPanel view;
    // PVT Table
    private JTable pvtTable;
    private ObjectReflectTableModel<PatientVisitModel> pvtTableModel;
    // Preference
    private Preferences preferences;
    // 性別レンダラフラグ
    private boolean sexRenderer;
    // 生年月日の元号表示
    private boolean ageDisplay;
    // フォントサイズ
    private int fontSize;
    // 選択されている患者情報
    private PatientVisitModel[] selectedPvt; // 複数行選択対応
    private int[] selectedIndex; // 複数行選択対応
    // Pvt チェックの runnable
    private final PvtChecker pvtChecker;
    // PvtChecker の臨時起動等, runnable を乗せるための ExecutorService
    private final ExecutorService executor;
    // 時計を更新するための ExecutorService
    private final ScheduledExecutorService schedule;
    // Logger
    private final Logger logger = LoggerFactory.getLogger(WaitingListImpl.class);

    /**
     * Creates new WaitingList.
     */
    public WaitingListImpl() {
        setName(NAME);
        // 使い回すオブジェクト
        schedule = Executors.newSingleThreadScheduledExecutor();
        executor = Executors.newSingleThreadExecutor();
        pvtChecker = new PvtChecker();
    }

    /**
     * プログラムを開始する.
     */
    @Override
    public void start() {
        setup();
        initComponents();
        connect();

        // 初回のチェック
        executor.submit(pvtChecker);

        // １分ごとに setCheckedTime() を呼んで待ち時間を更新して時計として使う
        Runnable r = () -> setCheckedTime(LocalDateTime.now());
        schedule.scheduleAtFixedRate(r, 0, 1, TimeUnit.MINUTES);
    }

    /**
     * Preferences を取得.
     */
    private void setup() {
        preferences = Preferences.userNodeForPackage(this.getClass());
        sexRenderer = preferences.getBoolean("sexRenderer", false);
        ageDisplay = preferences.getBoolean("ageDisplay", true);
        fontSize = preferences.getInt("fontSize", 12);
    }

    /**
     * GUI コンポーネントを初期化しレアイアウトする.
     */
    private void initComponents() {

        view = new WaitingListPanel();
        setUI(view);

        // Table の設定
        pvtTable = view.getTable();
        // 来院テーブル用のパラメータ
        List<PNSTriple<String, Class<?>, String>> reflectList = Arrays.asList(
            new PNSTriple<>(" 受付", Integer.class, "getNumber"),
            new PNSTriple<>("　患者 ID", String.class, "getPatientId"),
            new PNSTriple<>("　来院時間", String.class, "getPvtDateTrimDate"),
            new PNSTriple<>("　氏　　名", String.class, "getPatientName"),
            new PNSTriple<>(" 性別", String.class, "getPatientGenderDesc"),
            new PNSTriple<>("　年齢", String.class, "getPatientAge"),
            new PNSTriple<>("　生年月日", String.class, "getPatientBirthday"),
            new PNSTriple<>("　ドクター", String.class, "getAssignedDoctorName"),
            new PNSTriple<>(" メモ", String.class, "getMemo"),
            new PNSTriple<>(" 予約", String.class, "getAppointment"),
            new PNSTriple<>("状態", Integer.class, "getState")
        );

        pvtTableModel = new ObjectReflectTableModel<>(reflectList) {
            @Override
            public Object getValueAt(int row, int col) {
                Object obj = super.getValueAt(row, col);
                return (col == BIRTHDAY_COLUMN && ageDisplay) ? Gengo.toGengo((String) obj) : obj;
            }
        };
        pvtTable.setModel(pvtTableModel);
        view.setFontSize(fontSize);

        // 来院情報テーブルの属性を設定する
        pvtTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        pvtTable.setRowSelectionAllowed(true);

        // sorter を設定
        TableRowSorter<ObjectReflectTableModel> sorter = new TableRowSorter<>(pvtTableModel) {
            // ASCENDING -> DESCENDING -> 初期状態 と切り替える
            @Override
            public void toggleSortOrder(int column) {
                if (column >= 0 && column < getModelWrapper().getColumnCount() && isSortable(column)) {
                    List<RowSorter.SortKey> keys = new ArrayList<>(getSortKeys());
                    if (!keys.isEmpty()) {
                        RowSorter.SortKey sortKey = keys.get(0);
                        if (sortKey.getColumn() == column && sortKey.getSortOrder() == SortOrder.DESCENDING) {
                            setSortKeys(null);
                            return;
                        }
                    }
                }
                super.toggleSortOrder(column);
            }
        };
        pvtTable.setRowSorter(sorter);

        // 年齢コラム 32.10 の型式をソートできるようにする
        sorter.setComparator(AGE_COLUMN, Comparator.comparing(x -> {
            String[] age = ((String) x).split("\\.");
            int y = Integer.parseInt(age[0]); // 歳
            int m = Integer.parseInt(age[1]); // ヶ月
            return 100 * y + m;
        }));

        // 生年月日コラム
        sorter.setComparator(BIRTHDAY_COLUMN,
                Comparator.comparing(x -> ageDisplay ? Gengo.toSeireki((String) x) : (String) x));

        // レンダラを生成する
        MaleFemaleRenderer sRenderer = new MaleFemaleRenderer();
        MaleFemaleRenderer centerRenderer = new MaleFemaleRenderer(JLabel.CENTER);
        MaleFemaleRenderer rightRenderer = new MaleFemaleRenderer(JLabel.RIGHT);
        KarteStateRenderer stateRenderer = new KarteStateRenderer();

        pvtTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer); // 0 受付
        pvtTable.getColumnModel().getColumn(1).setCellRenderer(sRenderer); // 1 患者 ID
        pvtTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer); // 2 来院時間
        pvtTable.getColumnModel().getColumn(3).setCellRenderer(sRenderer); // 3 氏名
        pvtTable.getColumnModel().getColumn(4).setCellRenderer(centerRenderer); // 4 性別
        pvtTable.getColumnModel().getColumn(5).setCellRenderer(rightRenderer); // 5 年齢
        pvtTable.getColumnModel().getColumn(6).setCellRenderer(sRenderer); // 6 生年月日
        pvtTable.getColumnModel().getColumn(7).setCellRenderer(sRenderer); // 7 ドクター
        pvtTable.getColumnModel().getColumn(8).setCellRenderer(sRenderer); // 8 メモ
        pvtTable.getColumnModel().getColumn(9).setCellRenderer(sRenderer); // 9 予約
        pvtTable.getColumnModel().getColumn(10).setCellRenderer(stateRenderer);

        // 日付ラベルに値を設定する
        setOperationDate(LocalDateTime.now());
        // 来院数を表示する
        updatePvtCount();
    }

    /**
     * コンポーネントにイベントハンドラーを登録し相互に接続する.
     */
    private void connect() {
        // 靴のアイコンをクリックした時来院情報をフルチェックする
        view.getKutuBtn().addActionListener(e -> executor.submit(pvtChecker));

        // コンテキストリスナを登録する
        pvtTable.addMouseListener(new ContextListener());

        // ListSelectionListener を組み込む
        pvtTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int[] rows = pvtTable.getSelectedRows();
                if (rows == null) {
                    setSelectedPvt(null);
                } else {
                    PatientVisitModel[] patients = new PatientVisitModel[rows.length];
                    for (int i = 0; i < rows.length; i++) {
                        rows[i] = pvtTable.convertRowIndexToModel(rows[i]);
                        patients[i] = pvtTableModel.getObject(rows[i]);
                    }
                    setSelectedPvt(patients);
                }
            }
        });

        // SPACE でカルテオープン
        pvtTable.getInputMap().put(KeyStroke.getKeyStroke("SPACE"), "openKarte");
        pvtTable.getActionMap().put("openKarte", new ProxyAction(this::openKarte));

        // command-F で search field にフォーカスする裏コマンド
        pvtTable.getInputMap().put(KeyStroke.getKeyStroke("meta F"), "showWaitingList");
        pvtTable.getActionMap().put("showWaitingList", new ProxyAction(((Dolphin)getContext())::showPatientSearch));

        // ENTER の行送りをやめる
        pvtTable.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "doNothing");
        pvtTable.getInputMap().put(KeyStroke.getKeyStroke("shift ENTER"), "doNothing");
        pvtTable.getActionMap().put("doNothing", new ProxyAction(() -> {}));

        // Tab キー
        pvtTable.getInputMap().put(KeyStroke.getKeyStroke("TAB"), "focusPrevious");
        pvtTable.getInputMap().put(KeyStroke.getKeyStroke("shift TAB"), "focusPrevious");
        pvtTable.getActionMap().put("focusPrevious", new ProxyAction(KeyboardFocusManager.getCurrentKeyboardFocusManager()::focusPreviousComponent));

        // pvt 受信待ち endpoint
        PvtEndpoint endpoint = new PvtEndpoint();
        DolphinClientContext.getContext().setEndpoint(endpoint);
        endpoint.addPvtListener(this::hostPvtReceiver);
    }

    /**
     * メインウインドウのタブで受付リストに切り替わった時コールされる.
     */
    @Override
    public void enter() {
        controlMenu();
        // オルコン操作で隠されたウインドウ再表示
        if (!getContext().getPlugin(OrcaController.class).isEnabled()) {
            StampBoxPlugin stampBox = getContext().getPlugin(StampBoxPlugin.class);
            if (stampBox != null) {
                stampBox.getFrame().setState(Frame.NORMAL);
            }
            WindowHolder.allCharts().forEach(c -> c.getFrame().setState(Frame.NORMAL));
        }
    }

    /**
     * プログラムを終了する.
     */
    @Override
    public void stop() {
    }

    /**
     * 性別レンダラかどうかを返す.
     *
     * @return 性別レンダラの時 true
     */
    public boolean isSexRenderer() {
        return sexRenderer;
    }

    /**
     * レンダラをトグルで切り替える.
     */
    public void switchRenderer() {
        sexRenderer = !sexRenderer;
        preferences.putBoolean("sexRenderer", sexRenderer);
        if (pvtTable != null) {
            pvtTableModel.fireTableDataChanged();
        }
    }

    /**
     * 来院情報を取得する日を設定する.
     *
     * @param date 取得する日
     */
    private void setOperationDate(LocalDateTime date) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd (EE)"); // 2006-11-20 (水)
        view.getDateLbl().setText(dtf.format(date));
    }

    /**
     * 来院情報をチェックした時刻を設定する.
     *
     * @param time チェックした時刻
     */
    private void setCheckedTime(LocalDateTime time) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm");
        view.getCheckedTimeLbl().setText(dtf.format(time));
    }

    /**
     * 来院数, 待人数, 待時間表示.
     */
    private void updatePvtCount() {
        List<PatientVisitModel> pvtList = pvtTableModel.getObjectList();
        int pvtCount = pvtList.size();
        int waitingCount = 0;
        String waitingTime = "00:00";

        boolean found = false; // 待ち患者がいるかどうか
        int continuousCount = 0;

        for (PatientVisitModel pvt : pvtList) {
            int state = pvt.getState();
            if (state == KarteState.CLOSE_NONE || state == KarteState.OPEN_NONE) {
                // 診察未終了レコードをカウント, 最初に見つかった未終了レコードの時間から待ち時間を計算
                waitingCount++;
                // 診療未終了レコードの連続回数をカウント
                continuousCount++;

                if (!found) {
                    long pvtTime = ModelUtils.getDateTimeAsObject(pvt.getPvtDate()).getTime();
                    long nowTime = new Date().getTime();
                    if (pvtTime < nowTime) { // サーバ・クライアント時間がミリ秒単位でずれて反転することがある
                        waitingTime = DurationFormatUtils.formatPeriod(pvtTime, nowTime, "HH:mm");
                    }
                    found = true;
                }
            } else {
                // 診療未終了レコードが２件以下しかなければ待ち時間に入れない（待合室にいなかった人と判断）
                if (continuousCount > 0 && continuousCount <= 2) {
                    //System.out.println("continuous count: " + continuousCount);
                    found = false;
                    continuousCount = 0;
                }
            }
        }
        view.getCountLbl().setText(String.format("来院数%d人, 待ち%d人, 待ち時間 %s", pvtCount, waitingCount, waitingTime));

        // PNSBadgeTabbedPane に待ち人数を伝える
        PNSBadgeTabbedPane pane = ((Dolphin) getContext()).getTabbedPane();
        BadgeEvent e = new BadgeEvent(this);
        e.setBadgeNumber(waitingCount);
        e.setTabIndex(pane.indexOfTab(getName()));
        pane.setBadge(e);

        // Dock のアイコンにバッジを出す
        if (Dolphin.forMac) {
            Taskbar.getTaskbar().setIconBadge(waitingCount == 0 ? null : String.valueOf(waitingCount));
        }
    }

    /**
     * テーブル及び靴アイコンの enable/disable 制御を行う.
     *
     * @param busy pvt 検索中は true
     */
    public void setBusy(boolean busy) {
        view.getKutuBtn().setEnabled(!busy);

        if (busy) {
            getContext().getFrame().getGlassPane().setVisible(true);
            selectedIndex = pvtTable.getSelectedRows();
        } else {
            getContext().getFrame().getGlassPane().setVisible(false);
            if (selectedIndex != null) {
                for (int index : selectedIndex) {
                    pvtTable.getSelectionModel().addSelectionInterval(index, index);
                }
            }
        }
    }

    /**
     * 選択されている来院情報を設定返す.
     *
     * @return 選択されている来院情報
     */
    public PatientVisitModel[] getSelectedPvt() {
        return selectedPvt;
    }

    /**
     * 選択された来院情報を設定する.
     *
     * @param selectedPvt selected PatientVisitModel
     */
    public void setSelectedPvt(PatientVisitModel[] selectedPvt) {
        this.selectedPvt = selectedPvt;
        controlMenu();
    }

    /**
     * カルテオープンメニューを制御する.
     */
    private void controlMenu() {
        getContext().enableAction(GUIConst.ACTION_OPEN_KARTE, canOpen());
    }

    /**
     * Popupメニューから, 現在選択されている全ての患者のカルテを開く.
     */
    public void openKarte() {
        PatientVisitModel[] pvtModel = getSelectedPvt();
        if (pvtModel != null) {
            setBusy(true);
            Arrays.asList(pvtModel).forEach(this::openKarte);
            setBusy(false);
        }
    }

    /**
     * Read Only でカルテを開く.
     *
     * @param pvtModel PatientVisitModel
     * @param state State
     */
    @Override
    public void openReadOnlyKarte(final PatientVisitModel pvtModel, final int state) {
        // 元々 ReadOnly のユーザーならそのまま開いて OK
        if (Project.isReadOnly()) {
            getContext().openKarte(pvtModel);
            return;
        }

        // ダイアログで確認する
        String ptName = pvtModel.getPatientName();
        String[] options = {"閲覧のみ", "強制的に編集", "キャンセル"};

        JOptionPane pane = new PNSOptionPane(
                "<html>" +
                        "<h3>" + ptName + " 様のカルテは他の端末で編集中です</h3>" +
                        "<p><nobr>強制的に編集した場合、後から保存したカルテが最新カルテになります<nobr></p></html>",
                JOptionPane.WARNING_MESSAGE);
        pane.setOptions(options);
        pane.setInitialValue(options[0]);

        // JOptionPane から component を再帰検索して forceEditBtn を取り出す
        JButton tmpForce = null;

        Component[] components = pane.getComponents();
        List<Component> cc = java.util.Arrays.asList(components);

        while (!cc.isEmpty()) {
            List<Component> stack = new ArrayList<>();
            for (Component c : cc) {
                if (c instanceof JButton button) {
                    String name = button.getText();
                    if (options[1].equals(name)) {
                        tmpForce = button;
                    }

                } else if (c instanceof JComponent) {
                    components = ((JComponent) c).getComponents();
                    stack.addAll(java.util.Arrays.asList(components));
                }
            }
            cc = stack;
        }
        final JButton forceEditBtn = tmpForce;

        JSheet dialog = JSheet.createDialog(pane, getContext().getFrame());
        dialog.addSheetListener(se -> {
            int result = se.getOption();
            if (result == 0) { // 閲覧
                pvtModel.setState(KarteState.READ_ONLY);
                getContext().openKarte(pvtModel);
            } else if (result == 1) { // 強制編集
                pvtModel.setState(KarteState.toClosedState(state));
                getContext().openKarte(pvtModel);
            }
            // それ以外はキャンセル
        });

        // 強制編集ボタンにショートカット登録
        ActionMap am = dialog.getRootPane().getActionMap();
        InputMap im = dialog.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        im.put(KeyStroke.getKeyStroke("E"), "force-edit");
        im.put(KeyStroke.getKeyStroke("SPACE"), "force-edit");
        am.put("force-edit", new ProxyAction(forceEditBtn::doClick));

        dialog.setVisible(true);
    }

    /**
     * 現在の selectedPvt が canOpen かどうか判定.
     */
    private boolean canOpen() {
        PatientVisitModel[] pvt = getSelectedPvt();
        return Objects.nonNull(pvt) && pvt.length > 0;
    }

    /**
     * チャートステートの状態をデータベースに書き込む.
     * ChartImpl の windowOpened, windowClosed で呼ばれる.
     * state, byomeiCount, byomeiCountToday が変化している可能性がある.
     *
     * @param updated PatientVisitModel
     */
    public void updateState(final PatientVisitModel updated) {

        final int row = getRowForPvt(updated);
        if (row < 0) {
            logger.info("Something weird! Updated pvt is not found in the present pvtTable");
            return;
        }
        final int state = updated.getState();

        Runnable r;
        if (state == KarteState.READ_ONLY) {
            // ReadOnly の時, state を読み直す
            r = () -> {
                PvtDelegater pdl = new PvtDelegater();
                updated.setState(pdl.getPvtState(updated.getId()));
                pvtTableModel.fireTableRowsUpdated(row, row);
            };
        } else {
            // データベースへの書き込み
            r = () -> {
                PvtDelegater pdl = new PvtDelegater();
                int serverState = pdl.getPvtState(updated.getId());
                // サーバが NONE 以外かつクライアントが NONE の時はサーバの state を優先する
                // i.e. NONE のカルテを強制変更でひらいて, 変更しないで終了した場合, 他の端末で SAVE になったのを NONE に戻してしまうのを防ぐ
                if (!KarteState.isNone(serverState) && KarteState.isNone(state)) {
                    updated.setState(serverState);
                }
                pdl.updatePvt(updated);
                pvtTableModel.fireTableRowsUpdated(row, row);
            };
        }
        executor.submit(r);
    }

    /**
     * 終了時にカルテが OPEN になったまま残るのを防ぐためにすべて CLOSED に変換.
     *
     * @return stoppingTask
     */
    @Override
    public Callable<Boolean> getStoppingTask() {
        logger.info("WaitingListImpl stoppingTask starts");

        return () -> {
            // 開いているカルテを調べる
            WindowHolder.allCharts().stream()
                    .map(ChartImpl::getPatientVisit)
                    .filter(pvt -> pvt.getPvtDate() != null) // 今日の受診と関係あるカルテのみ選択
                    .forEach(pvt -> {
                        // 今日の受診がある場合は pvt status を変更する（open -> close)
                        int oldState = pvt.getState();
                        boolean isEmpty = new DocumentPeeker(pvt).isKarteEmpty();
                        int newState = KarteState.toClosedState(oldState, isEmpty);

                        // サーバに書き込む
                        PvtDelegater pdl = new PvtDelegater();
                        // NONE 以外から NONE への状態変更はありえないので無視
                        // NONE のカルテを強制変更でひらいて, 変更しないで終了した場合, 他の端末で SAVE になったのを NONE に戻してしまうのを防ぐ
                        int serverState = pdl.getPvtState(pvt.getId());
                        if (KarteState.isNone(serverState) || !KarteState.isNone(newState)) {
                            pvt.setState(newState);
                            pdl.updatePvt(pvt);
                        }
                    });
            return true;
        };
    }

    /**
     * 与えられた pvt に対応する TableModel の行を返す.
     * 要素が別オブジェクトになっている場合があるため, レコードIDで探す.
     *
     * @param updated PatientVisitModel
     * @return row
     */
    private int getRowForPvt(PatientVisitModel updated) {
        List<PatientVisitModel> pvtList = pvtTableModel.getObjectList();
        int updatedRow = -1;

        for (int i = 0; i < pvtList.size(); i++) {
            PatientVisitModel pvt = pvtList.get(i);
            if (updated.getId() == pvt.getId()) {
                updatedRow = i;
                break;
            }
        }
        return updatedRow;
    }

    /**
     * Server からの pvt message を受け取って pvtTableModel を更新する.
     *
     * @param hostPvt host pvt
     */
    public void hostPvtReceiver(PatientVisitModel hostPvt) {
        // 送られてきた pvt と同じものを local で探す
        // pvtDate & patientId で判定する → Patient ID が同じでも, pvtDate が違えば違う受付と判断する
        PatientVisitModel localPvt = null;
        int row = -1;
        int totalRowCount = pvtTableModel.getObjectCount();

        // hostPvt と同じ localPvt を探す
        for (int i = 0; i < totalRowCount; i++) {
            PatientVisitModel p = pvtTableModel.getObject(i);
            if (p.getPvtDate().equals(hostPvt.getPvtDate()) && p.getPatientId().equals(hostPvt.getPatientId())) {
                localPvt = p;
                row = i;
                break;
            }
        }

        if (localPvt != null) {
            // localPvt が見つかった場合
            //logger.info("pvt state local = " + localPvt.getState() + ", server = " + hostPvt.getState());

            // キャンセルかどうか
            if (hostPvt.getState() == KarteState.CANCEL_PVT) {
                // キャンセルの場合, 削除して番号を付け直す
                pvtTableModel.deleteRow(row);
                for (int i=row; i<pvtTableModel.getObjectCount(); i++) {
                    pvtTableModel.getObject(i).setNumber(i+1);
                }

            } else {
                // pvt を置き換える
                hostPvt.setNumber(localPvt.getNumber());
                pvtTableModel.getObjectList().set(row, hostPvt);
                // changeRow を fire, ただしカルテが開いていたら fire しない
                //if (!isKarteOpened(localPvt)) {
                    pvtTableModel.fireTableRowsUpdated(row, row);
                //}
            }

        } else if (hostPvt.getState() != KarteState.CANCEL_PVT){
            // localPvt がなければ, それは追加である
            row = pvtTableModel.getObjectCount();
            // 番号付加
            hostPvt.setNumber(row + 1);
            pvtTableModel.addRow(hostPvt);
            //logger.info("pvt added at row " + row);
            // 通知を表示
            ScriptExecutor.displayNotification(hostPvt.getPatientAgeBirthday(), "受付 " + (row + 1), hostPvt.getPatientName());
            // 追加行までスクロール
            int viewRow = pvtTable.convertRowIndexToView(row);
            pvtTable.scrollRectToVisible(pvtTable.getCellRect(viewRow, 0, false));
        }
        // pvt count 更新
        updatePvtCount();
    }

    /**
     * 患者来院情報をフルチェックするタスク.
     */
    private class PvtChecker implements Runnable {

        @Override
        public void run() {
            PatientVisitModel pvt;
            PvtDelegater delegater = new PvtDelegater();

            SwingUtilities.invokeLater(() -> setBusy(true));

            // PVTDelegater で使う date を作成する
            // [0] = today, date[1] = AppodateFrom, date[2] = AppodateTo
            // [0] 今日, [1] 2ヶ月前(AppodateFrom), [2]その2ヶ月後(AppodateTo) それは今日
            final LocalDateTime date = LocalDateTime.now();
            String[] dateToSearch = new String[3];
            dateToSearch[0] = dateToSearch[2] = date.format(DateTimeFormatter.ISO_DATE);
            dateToSearch[1] = date.plusMonths(-2).format((DateTimeFormatter.ISO_DATE));

            // フルチェックする
            final List<PatientVisitModel> result = delegater.getPvt(dateToSearch, 0);

            // 結果を全部更新する
            pvtTableModel.clear();
            for (int i = 0; i < result.size(); i++) {
                pvt = result.get(i);
                // 受付番号セット
                pvt.setNumber(i + 1);
                // 受付リスト追加
                pvtTableModel.addRow(pvt);
            }

            // pvtState をアップデート pvtStateList.size は table の ObjectCount と同じはず
            List<PvtStateSpec> pvtStateList = delegater.getPvtState();

            if (pvtStateList != null && (pvtStateList.size() == pvtTableModel.getObjectCount())) {
                logger.info("pvt state update");

                PatientVisitModel myPvt;
                List<Integer> changedRows = new ArrayList<>();

                for (int i = 0; i < pvtStateList.size(); i++) {
                    PvtStateSpec serverPvt = pvtStateList.get(i);
                    myPvt = pvtTableModel.getObject(i);

                    // サーバと違いがあればアップデート
                    // 自分でカルテを開いている場合はアップデートしない
                    if (!isKarteOpened(myPvt)) {
                        boolean lineChanged = false;
                        if (myPvt.getState() != serverPvt.getState()) {
                            //logger.info("PVT state changed row=" + i + " state=" + serverPvt.getState());
                            myPvt.setState(serverPvt.getState());
                            lineChanged = true;
                        }
                        if (myPvt.getByomeiCount() != serverPvt.getByomeiCount()) {
                            //logger.info("PVT byomeiCount changed row=" + i + " byomeiCount=" + serverPvt.getByomeiCount());
                            myPvt.setByomeiCount(serverPvt.getByomeiCount());
                            lineChanged = true;
                        }
                        if (myPvt.getByomeiCountToday() != serverPvt.getByomeiCountToday()) {
                            //logger.info("PVT byomeiCountToday changed row=" + i + " byomeiCountToday=" + serverPvt.getByomeiCountToday());
                            myPvt.setByomeiCountToday(serverPvt.getByomeiCountToday());
                            lineChanged = true;
                        }
                        if (lineChanged) {
                            changedRows.add(i);
                        }
                    }
                }
            } else {
                logger.info("object count and pvtState count discrepant??");
            }

            pvtTableModel.fireTableDataChanged();

            SwingUtilities.invokeLater(() -> {
                setCheckedTime(date);
                updatePvtCount();
                setBusy(false);
            });

            //logger.info("check time " + (new Date().getTime()-startTime) + " msec");
        }
    }

    /**
     * Retina 対応 Grid を描くレンダラのベース.
     */
    private abstract class TableCellRendererBase extends DefaultTableCellRenderer {

        protected boolean horizontalGrid = false;
        protected boolean verticalGrid = false;
        protected boolean marking = false;
        protected Color markingColor = Color.WHITE;

        public TableCellRendererBase() {
        }

        /**
         * Show grids and markings.
         *
         * @param graphics Graphics
         */
        @Override
        public void paint(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics;
            super.paint(graphics);
            if (horizontalGrid) {
                g.setColor(Color.WHITE);
                g.drawLine(0, getHeight(), getWidth(), getHeight());
            }
            if (verticalGrid) {
                g.setColor(Color.WHITE);
                g.drawLine(0, 0, 0, getHeight());

            }
            if (marking) {
                g.setColor(markingColor);
                g.fillRect(0, 0, 6, getHeight());
            }
        }

        /**
         * Set background color.
         *
         * @param table JTable
         * @param value value
         * @param isSelected isSelected
         * @param isFocused isFocused
         * @param row row
         * @param col col
         */
        public void setBackground(JTable table,
                                  Object value,
                                  boolean isSelected,
                                  boolean isFocused,
                                  int row, int col) {

            Color bg = table.getBackground();

            if (isSexRenderer()) {
                PatientVisitModel pvt = pvtTableModel.getObject(table.convertRowIndexToModel(row));
                if (pvt != null) {
                    String gender = pvt.getPatient().getGender();
                    if (gender.equals(IInfoModel.MALE)) {
                        bg = MALE_COLOR;
                    } else if (gender.equals(IInfoModel.FEMALE)) {
                        bg = FEMALE_COLOR;
                    }
                }
            }
            setBackground(bg);
        }
    }

    /**
     * KarteStateRenderer.
     * カルテ（チャート）の状態をレンダリングするクラス.
     */
    private class KarteStateRenderer extends TableCellRendererBase {

        public KarteStateRenderer() {
            horizontalGrid = true;
            verticalGrid = true;
            initComponent();
        }

        private void initComponent() {
            setHorizontalAlignment(JLabel.CENTER);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table,
                                                       Object value,
                                                       boolean isSelected,
                                                       boolean isFocused,
                                                       int row, int col) {

            PatientVisitModel pvt = pvtTableModel.getObject(table.convertRowIndexToModel(row));

            // 背景色の設定
            setBackground(table, value, isSelected, isFocused, row, col);

            // 文字色
            Color fore = pvt != null && pvt.getState() == KarteState.CANCEL_PVT ? CANCEL_PVT_COLOR : table.getForeground();
            this.setForeground(fore);

            // state の value チェック, 本日のカルテがあるかどうか（今日のカルテはないけど, 以前のカルテを編集しただけの場合）
            if (value instanceof Integer state) {
                switch (state) {
                    // 診察終了
                    case KarteState.CLOSE_SAVE -> this.setIcon(DONE_ICON);
                    // カルテ記載がまだ
                    case KarteState.CLOSE_UNFINISHED -> this.setIcon(UNFINISHED_ICON);
                    // 仮保存
                    case KarteState.CLOSE_TEMP -> this.setIcon(TEMPORARY_ICON);
                    // READ_ONLY で開いている
                    case KarteState.READ_ONLY -> this.setIcon(OPEN_ICON);
                    // カルテが開いている
                    case KarteState.OPEN_NONE, KarteState.OPEN_TEMP -> {
                        if (isKarteOpened(pvt)) {
                            this.setIcon(OPEN_ICON);
                        } else {
                            this.setIcon(OPEN_USED_NONE);
                        }
                    }
                    case KarteState.OPEN_SAVE -> {
                        if (isKarteOpened(pvt)) {
                            this.setIcon(OPEN_ICON);
                        } else {
                            this.setIcon(OPEN_USED_SAVE);
                        }
                    }
                    case KarteState.OPEN_UNFINISHED -> {
                        if (isKarteOpened(pvt)) {
                            this.setIcon(OPEN_ICON);
                        } else {
                            this.setIcon(OPEN_USED_UNFINISHED);
                        }
                    }
                    // その他の場合はアイコン無し CLOSE_NONE はここ
                    default -> this.setIcon(null);
                }

                // マーキングする
                marking = false;
                if (pvt != null && pvt.getState() != KarteState.CANCEL_PVT) {
                    if (!pvt.hasByomei()) {
                        marking = true;
                        markingColor = DIAGNOSIS_EMPTY_COLOR;
                    } else if (pvt.isShoshin()) {
                        marking = true;
                        markingColor = SHOSHIN_COLOR;
                    }
                }

                this.setText("");

            } else {
                setIcon(null);
                this.setText(value == null ? "" : value.toString());
            }
            return this;
        }
    }

    /**
     * MaleFemaleRenderer.
     * 男女で色を変える renderer.
     */
    private class MaleFemaleRenderer extends TableCellRendererBase {

        public MaleFemaleRenderer() {
            this(JLabel.LEFT);
        }

        public MaleFemaleRenderer(int alignment) {
            horizontalGrid = true;
            initComponent(alignment);
        }

        private void initComponent(int alignment) {
            setHorizontalAlignment(alignment);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table,
                                                       Object value,
                                                       boolean isSelected,
                                                       boolean isFocused,
                                                       int row, int col) {

            PatientVisitModel pvt = pvtTableModel.getObject(table.convertRowIndexToModel(row));

            if (isSelected) {
                Color fore;
                Color back;
                if (table.isFocusOwner()) {
                    fore = pvt != null && pvt.getState() == KarteState.CANCEL_PVT ?
                            CANCEL_PVT_COLOR : table.getSelectionForeground();
                    back = table.getSelectionBackground();

                } else {
                    fore = pvt != null && pvt.getState() == KarteState.CANCEL_PVT ?
                            CANCEL_PVT_COLOR : table.getForeground();
                    back = (Color) table.getClientProperty("JTable.backgroundOffFocus");
                }
                setForeground(fore);
                setBackground(back);

            } else {
                setBackground(table, value, isSelected, isFocused, row, col);
                Color fore = pvt != null && pvt.getState() == KarteState.CANCEL_PVT ? CANCEL_PVT_COLOR : table.getForeground();
                this.setForeground(fore);
            }

            if (value instanceof String) {
                switch (col) {
                    case 3 -> { // 名前
                        if (fontSize > 12) {
                            value = value + "　(" + pvt.getPatient().getKanaName() + ")";
                        }
                        this.setText(IndentTableCellRenderer.addIndent((String) value, IndentTableCellRenderer.WIDE, this.getForeground()));
                        this.setFont(view.getNormalFont());
                    }
                    case 1, 6 -> { // ID, 生年月日
                        this.setText(IndentTableCellRenderer.addIndent((String) value, IndentTableCellRenderer.WIDE, this.getForeground()));
                        this.setFont(view.getNormalFont());
                    }
                    case 5 -> { // 年齢
                        String[] age = ((String) value).split("\\.");
                        if (age[0].equals("0")) {
                            setText(age[1] + " ヶ月");
                        } else {
                            setText(age[0] + " 歳");
                        }
                        this.setFont(view.getNormalFont());
                    }
                    case 7, 8 -> { // ドクター, メモ
                        this.setText(IndentTableCellRenderer.addIndent((String) value, IndentTableCellRenderer.WIDE, this.getForeground()));
                        this.setFont(view.getSmallFont());
                    }
                    default -> {
                        this.setText((String) value);
                        this.setFont(view.getNormalFont());
                    }
                }
            } else {
                setIcon(null);
                this.setText(value == null ? "" : value.toString());
            }
            return this;
        }
    }

    /**
     * 受付リストのコンテキストメニュークラス.
     */
    private class ContextListener extends AbstractMainComponent.ContextListener<PatientVisitModel> {
        private final JPopupMenu contextMenu;

        public ContextListener() {
            contextMenu = getContextMenu();
        }

        @Override
        public void openKarte(PatientVisitModel pvt) {
            WaitingListImpl.this.openKarte(pvt);
        }

        @Override
        public void maybeShowPopup(MouseEvent e) {

            if (e.isPopupTrigger()) {
                contextMenu.removeAll();

                if (canOpen()) {
                    String pop1 = "カルテを開く";
                    JMenuItem openKarte = new JMenuItem(new ProxyAction(pop1, WaitingListImpl.this::openKarte));
                    openKarte.setIconTextGap(8);
                    contextMenu.add(openKarte);
                    contextMenu.addSeparator();
                }

                String pop3 = "偶数奇数レンダラを使用する";
                String pop4 = "性別レンダラを使用する";
                JRadioButtonMenuItem oddEven = new JRadioButtonMenuItem(new ProxyAction(pop3, WaitingListImpl.this::switchRenderer));
                JRadioButtonMenuItem sex = new JRadioButtonMenuItem(new ProxyAction(pop4, WaitingListImpl.this::switchRenderer));
                ButtonGroup renderButtonGroup = new ButtonGroup();
                renderButtonGroup.add(oddEven);
                renderButtonGroup.add(sex);
                contextMenu.add(oddEven);
                contextMenu.add(sex);
                if (sexRenderer) {
                    sex.setSelected(true);
                } else {
                    oddEven.setSelected(true);
                }

                String pop5 = "生年月日の元号表示";
                JCheckBoxMenuItem gengo = new JCheckBoxMenuItem(pop5);
                contextMenu.add(gengo);
                gengo.setSelected(ageDisplay);
                gengo.addActionListener(ae -> {
                    ageDisplay = gengo.isSelected();
                    preferences.putBoolean("ageDisplay", ageDisplay);
                });

                String pop6 = "フォントサイズ";
                JMenu fontMenu = new JMenu(pop6);
                contextMenu.add(fontMenu);
                int[] sizes = { 12, 18, 24 };
                JRadioButtonMenuItem[] sizeItems = new JRadioButtonMenuItem[sizes.length];
                ButtonGroup fontButtonGroup = new ButtonGroup();
                for (int i=0; i<sizes.length; i++) {
                    final int size = sizes[i];
                    sizeItems[i] = new JRadioButtonMenuItem(String.valueOf(size));
                    sizeItems[i].setIconTextGap(12);
                    sizeItems[i].addActionListener(ae -> {
                        fontSize = size;
                        view.setFontSize(size);
                        preferences.putInt("fontSize", size);
                    });
                    if (fontSize == size) { sizeItems[i].setSelected(true); }
                    fontButtonGroup.add(sizeItems[i]);
                    fontMenu.add(sizeItems[i]);
                }

                oddEven.setIconTextGap(12);
                sex.setIconTextGap(12);
                gengo.setIconTextGap(12);
                fontMenu.setIconTextGap(12);
                contextMenu.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }
}
