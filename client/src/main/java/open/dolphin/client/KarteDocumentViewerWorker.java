package open.dolphin.client;

import open.dolphin.delegater.DocumentDelegater;
import open.dolphin.infomodel.DocInfoModel;
import open.dolphin.infomodel.DocumentModel;
import open.dolphin.infomodel.IInfoModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/// 文書をデータベースから取得するタスククラス.
/// TIME-FOR-SWING 0, FRACTION=300 での 290件のカルテ表示時間 2350
/// TIME-FOR-SWING 200 msec での 290件のカルテ表示時間.
/// FRACTION を大きくすると速くなる. 小さくするとロード中の操作がしやすくなる.
/// FRACTION 10: 8578, 20: 4863, 30: 3394, 40: 2903, 50: 2640, 60: 2636
/// FRACTION 40 での表示時間
public class KarteDocumentViewerWorker extends SwingWorker<Integer, List<Component>> {
    // FRACTION 数ごとに取得、描画の処理をする
    private static final int FRACTION = 40;

    private final KarteDocumentViewer context;
    private final Chart chart;
    private final List<KarteViewer2> karteViewerList;
    // 選択された History の DocId をキーとした Map
    private final Map<String, DocInfoModel> selectedHistoryMap;
    // karteViewer を並べるパネル
    private final JPanel scrollerPanel;
    private final JProgressBar progressBar;
    private final Logger logger = LoggerFactory.getLogger(KarteDocumentViewerWorker.class);
    private long lap;

    public KarteDocumentViewerWorker(KarteDocumentViewer context, List<KarteViewer2> karteViewerList, Map<String, DocInfoModel> selectedHistoryMap, JPanel scrollerPanel) {
        this.context = context;
        this.chart = context.getContext();
        this.scrollerPanel = scrollerPanel;
        this.karteViewerList = karteViewerList;
        this.selectedHistoryMap = selectedHistoryMap;
        progressBar = chart.getStatusPanel().getProgressBar();
    }

    @Override
    protected Integer doInBackground() {
        lap = System.currentTimeMillis();

        // 選択リストにあって 現在の karteList にないものは追加する
        List<DocInfoModel> added = new ArrayList<>(); // 追加されたもの
        Set<String> karteListIdSet = karteViewerList.stream().map(viewer -> viewer.getDocument().getDocInfo().getDocId()).collect(Collectors.toSet());

        for (DocInfoModel selectedDocInfo : selectedHistoryMap.values()) {
            if (!karteListIdSet.contains(selectedDocInfo.getDocId())) {
                //logger.debug("added: " + selectedDocInfo.getDocId());
                added.add(selectedDocInfo);
            }
        }

        if (!added.isEmpty()) {
            progressBar.setMaximum(added.size());
            // 描画時のパタ付きを防ぐためにソートしておく
            added.sort(chart.getDocumentHistory().isAscending() ? Comparator.naturalOrder() : Comparator.reverseOrder());

            DocumentDelegater ddl = new DocumentDelegater();
            int count = 0;
            boolean hasNext = true;
            while (hasNext) {
                List<Long> pkList = new ArrayList<>(FRACTION);
                for (int i = 0; i < FRACTION; i++) {
                    pkList.add(added.get(count++).getDocPk());
                    if (count >= added.size()) {
                        hasNext = false;
                        break;
                    }
                }
                logger.debug("count = {}", count);
                // 途中で選択が変わったり，ウインドウが閉じられている可能性がある
                if (!isCancelled() && chart.getFrame().isVisible()) {
                    addKarteViewer(ddl.getDocuments(pkList));
                    progressBar.setValue(count);

                } else {
                    logger.debug("selection has changed");
                    break;
                }
            }
        } else {
            // removed 処理のために空の addKarteViewer 呼ぶ必要有り
            addKarteViewer(new ArrayList<>(1));
        }
        return added.size();
    }

    @Override
    protected void process(List<List<Component>> chunks) {
        logger.debug("process published chunks: {}", chunks.size());
        if (!isCancelled()) {
            updateScrollerPanel(chunks);
        }
    }

    @Override
    protected void done() {
        progressBar.setValue(0);
        // リスト先頭のカルテを選択状態に
        if (!karteViewerList.isEmpty()) {
            context.setSelectedKarteViewer(karteViewerList.getFirst());
        }
        logger.debug("lap time: {}", System.currentTimeMillis() - lap);
    }

    private void addKarteViewer(List<DocumentModel> models) {
        long lap = System.currentTimeMillis();
        updateKarteViewerList(models);

        // karteViewerList の内容を scrollerPanel に反映する
        List<Component> currentViewerUIs = new ArrayList<>();
        for (KarteViewer2 viewer : karteViewerList) {
            currentViewerUIs.add(viewer.getUI());
        }
        publish(currentViewerUIs);

        logger.debug("viewer: {}", lap);
    }

    /// KarteViewerList を更新する
    private synchronized void updateKarteViewerList(List<DocumentModel> models) {
        // 選択解除された KarteViewer2 のリストを作る
        List<KarteViewer2> removed = new ArrayList<>(); // 選択が解除されているもの

        // karteViewerList にあって選択リストにないものは除去リストに入れる
        for (KarteViewer2 viewer : karteViewerList) {
            String id = viewer.getDocument().getDocInfo().getDocId();
            if (!selectedHistoryMap.containsKey(id)) {
                removed.add(viewer);
                viewer.stop();
            }
        }
        // 削除リストを karteViewerList から取り除く
        karteViewerList.removeAll(removed);
        // 非選択項目削除済みの karteList の DocId セットを作る
        Set<String> karteListDocIdSet = karteViewerList.stream().map(v -> v.getDocument().getDocInfo().getDocId()).collect(Collectors.toSet());

        // 追加された DocumentModel から　karteViewer をつくって karteList に加える
        for (DocumentModel dm : models) {
            DocInfoModel docInfo = dm.getDocInfo();
            String docId = docInfo.getDocId();

            // karteList にないものがあれば, KarteViewer を作って加える
            if (!karteListDocIdSet.contains(docId)) {

                // 対応する DocInfoModel を探す
                DocInfoModel foundInfo = selectedHistoryMap.values().stream()
                        .filter(d -> d.getDocId().equals(docId)).findFirst().orElse(null);
                if (foundInfo != null) {
                    // 実体のある docInfo をセットする
                    dm.setDocInfo(foundInfo);
                } else {
                    // 選択が変化して，せっかく取ってきた DocumentModel が無駄になった
                    logger.debug("selection changed, document fetched in vain");
                    break;
                }

                // KarteViewer を生成する
                final KarteViewer2 karteViewer = new KarteViewer2();
                karteViewer.setContext(chart);
                karteViewer.setDocument(dm);
                karteViewer.setAvoidEnter(true);

                // このコールでモデルのレンダリングが開始される
                karteViewer.start();
                // ダブルクリックされたカルテを別画面で表示する MouseListener を生成して KarteViewer の Pane にアタッチする
                if (docInfo.getDocType().equals(IInfoModel.DOCTYPE_KARTE)) {
                    final MouseListener ml = new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            int cnt = e.getClickCount();
                            if (cnt == 2) {
                                // 選択した Karte を EditorFrame で開く
                                context.setSelectedKarteViewer(karteViewer);
                                context.openKarte();
                            } else if (cnt == 1) {
                                context.setSelectedKarteViewer(karteViewer);
                            }
                        }
                    };
                    karteViewer.addMouseListener(ml);
                }
                // ボーダーをあらかじめ付けておく
                karteViewer.getKartePanel().setBackground(karteViewer.getSOAPane().getTextPane().getBackground());
                karteViewer.setSelected(false);
                karteViewerList.add(karteViewer);
            }
        }
        // 時間軸でソート
        karteViewerList.sort(chart.getDocumentHistory().isAscending() ? Comparator.naturalOrder() : Comparator.reverseOrder());
    }

    /// karteViewerList の内容を scrollerPanel に反映する
    private void updateScrollerPanel(List<List<Component>> chunks) {
        for (List<Component> currentViewerUIs : chunks) {
            Set<Component> currentViewersSet = new HashSet<>(currentViewerUIs);

            //remove
            for (Component comp : scrollerPanel.getComponents()) {
                if (!currentViewersSet.contains(comp)) {
                    scrollerPanel.remove(comp);
                }
            }

            // add
            for (int i = 0; i < currentViewerUIs.size(); i++) {
                Component comp = currentViewerUIs.get(i);
                if (comp.getParent() == scrollerPanel) {
                    scrollerPanel.setComponentZOrder(comp, i);
                } else {
                    scrollerPanel.add(comp, i);
                }
            }
        }

        // repaint
        scrollerPanel.revalidate();
        scrollerPanel.repaint();
    }
}
