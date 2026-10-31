package open.dolphin.client;

import open.dolphin.infomodel.DocInfoModel;
import open.dolphin.infomodel.DocumentModel;
import open.dolphin.infomodel.IInfoModel;
import open.dolphin.util.DateUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.event.MouseListener;
import java.awt.print.PageFormat;
import java.time.LocalDateTime;
import java.util.Objects;

/// KarteViewer2. 2号カルテを View する ChartDocument.
/// KartePanel はタイムスタンプ, SoaTextPane, PTextPane を含んだ JPanel. これを KarteDocumentViewer で見る.
/// KartePane は SoaTextPane または PTextPane を含んだ KarteComposite. いろいろな機能を持つ.
///
/// @author Kazushi Minagawa, Digital Globe, Inc.
/// @author pns
public class KarteViewer2 extends AbstractChartDocument implements Comparable<KarteViewer2> {
    // 仮保存中のドキュメントを表す文字
    private static final String UNDER_TMP_SAVE = " - 仮保存中";
    // 2号カルテ
    private KartePanel kartePanel;
    // 選択されているかどうかのフラグ
    private boolean selected;
    private final Logger logger = LoggerFactory.getLogger(KarteViewer2.class);

    public KarteViewer2() { }

    /// SOA Pane を返す.
    ///
    /// @return soaPane
    public KartePane getSoaKartePane() { return kartePanel.getSoaKartePane(); }

    /// P Pane を返す.
    ///
    /// @return pPane
    public KartePane getPKartePane() {
        return kartePanel.getPKartePane();
    }

    /// KartePanel を返す.
    ///
    /// @return KartePanel
    public KartePanel getKartePanel() {
        return kartePanel;
    }

    /// プログラムを開始する.
    @Override
    public void start() {
        DocumentModel document = getDocument();
        if (document == null) { return; }

        // Creates GUI
        kartePanel = KartePanelFactory.createViewerPanel();
        KarteRenderer_2 renderer = new KarteRenderer_2(getSoaKartePane(), getPKartePane());
        renderer.render(document);

        kartePanel.setParent(this);
        // Schema 画像にファイル名を付けるのために必要
        kartePanel.setDocId(getDocument().getDocInfo().getDocId());

        StringBuilder timeStamp = new StringBuilder();

        // time stamp
        LocalDateTime confirmDate = document.getDocInfo().getFirstConfirmDate();
        String firstConfirmDate = confirmDate.format(DateUtils.KARTE_DATE_FORMATTER);
        timeStamp.append(firstConfirmDate);

        // 修正日表示
        boolean showModified = getContext().getDocumentHistory().isShowModified(); // 修正履歴表示モードかどうか
        String parent = document.getDocInfo().getParentId(); // 親があるかどうか

        if (showModified && Objects.nonNull(parent)) {
            timeStamp.append(" [");
            timeStamp.append(MODIFY_MARK); // update マーク
            timeStamp.append(firstConfirmDate);
            timeStamp.append("]");
        }

        if (document.getDocInfo().getStatus().equals(IInfoModel.STATUS_TMP)) {
            timeStamp.append(UNDER_TMP_SAVE); // - 仮保存中
        }

        // timeStamp にカルテ作成者を入れる
        String drName = document.getCreator().getCommonName();
        timeStamp.append(" 記載医師: ");
        timeStamp.append(drName);

        kartePanel.setTimeStamp(timeStamp.toString());

        // モデル表示後にリスナ等を設定する init(editable, mediator)
        ChartMediator mediator = getContext().getChartMediator();
        getSoaKartePane().init(false, mediator);
        getPKartePane().init(false, mediator);

        setUI(kartePanel);
        enter();
    }

    @Override
    public void stop() {
        kartePanel.stop();
    }

    public void addMouseListener(MouseListener ml) {
        kartePanel.addMouseListener(ml);
    }

    public String getDocType() {
        return Objects.nonNull(getDocument())
                ? getDocument().getDocInfo().getDocType()
                : null;
    }

    /// KartePanel を 1枚，ダイアログを出してプリントする.
    /// Junzo SATO
    @Override
    public void print() {
        PageFormat pageFormat = getContext().getContext().getPageFormat();
        String name = getContext().getPatient().getFullName();
        kartePanel.printPanel(pageFormat, 1, true, name, kartePanel.getPreferredSize().height + 30);
    }

    /// コンテナからコールされる enter() メソッドでメニューを制御する.
    @Override
    public void enter() {
        super.enter();

        // ReadOnly 属性
        boolean canEdit = !getContext().isReadOnly();

        // 仮保存かどうか
        boolean tmp = getDocument().getDocInfo().getStatus().equals(IInfoModel.STATUS_TMP);

        // 新規カルテ作成が可能な条件
        boolean newOk = canEdit && (!tmp);

        ChartMediator mediator = getContext().getChartMediator();
        mediator.getAction(GUIConst.ACTION_NEW_KARTE).setEnabled(newOk);        // 新規カルテ
        mediator.getAction(GUIConst.ACTION_PRINT).setEnabled(true);             // 印刷
        mediator.getAction(GUIConst.ACTION_MODIFY_KARTE).setEnabled(canEdit);   // 修正
    }

    /// 選択されているかどうかを返す.
    ///
    /// @return 選択されている時 true
    public boolean isSelected() {
        return selected;
    }

    /// 選択状態を設定する.
    /// 選択状態により Viewer のタイムスタンプ部分のフォントを変える (BOLD にする).
    ///
    /// @param selected 選択された時 true
    public void setSelected(boolean selected) {
        this.selected = selected;
        kartePanel.setSelected(selected);
    }

    @Override
    public int hashCode() {
        return getDocument().getDocInfo().getDocId().hashCode() + 72;
    }

    @Override
    public boolean equals(Object other) {
        if (other != null && other.getClass() == this.getClass()) {
            DocInfoModel otheInfo = ((KarteViewer2) other).getDocument().getDocInfo();
            return getDocument().getDocInfo().equals(otheInfo);
        }
        return false;
    }

    /// 順序を DocInfoModel の順序で決める.
    /// firstConfirmDate で並べて，同日の場合は confirmDate の順番に並べる.
    ///
    /// @param other 相手
    /// @return compareTo 値
    @Override
    public int compareTo(@NotNull KarteViewer2 other) {
        if (other.getClass() == this.getClass()) {
            DocInfoModel otheInfo = other.getDocument().getDocInfo();
            return getDocument().getDocInfo().compareTo(otheInfo);
        }
        return -1;
    }
}
