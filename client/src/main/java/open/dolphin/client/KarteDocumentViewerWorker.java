package open.dolphin.client;

import open.dolphin.delegater.DocumentDelegater;
import open.dolphin.infomodel.DocInfoModel;
import open.dolphin.infomodel.DocumentModel;
import open.dolphin.project.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ContainerListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/// 文書をデータベースから取得するタスククラス.
/// FRACTION を大きくすると速くなる. 小さくするとロード中の操作がしやすくなる.
/// 290件のカルテ表示時間
/// FRACTION 10: 2168, 20: 1756, 30: 1480, 40: 1408, 50: 1430, 60: 1434
public class KarteDocumentViewerWorker extends SwingWorker<Integer, List<Component>> implements PropertyChangeListener {
    private KarteDocumentViewer context;
    private Chart chart;
    private List<KarteViewer2> karteViewerList;
    // 選択された History の DocPk (DocumentModel#id) をキーとした Map
    private Map<Long, DocInfoModel> selectedHistoryMap;
    // karteViewer を並べるパネル
    private JPanel scrollerPanel;
    private JProgressBar progressBar;

    private final Logger logger = LoggerFactory.getLogger(KarteDocumentViewerWorker.class);
    private long lap;

    public KarteDocumentViewerWorker(KarteDocumentViewer context, List<KarteViewer2> karteViewerList, Map<Long, DocInfoModel> selectedHistoryMap, JPanel scrollerPanel) {
        this.context = context;
        this.chart = context.getContext();
        this.scrollerPanel = scrollerPanel;
        this.karteViewerList = karteViewerList;
        this.selectedHistoryMap = selectedHistoryMap;

        progressBar = chart.getStatusPanel().getProgressBar();
        progressBar.setMinimum(0);
        progressBar.setMaximum(100);
        addPropertyChangeListener(this);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if ("progress".equals(evt.getPropertyName())) {
            progressBar.setValue((Integer) evt.getNewValue()); // EDT 上で実行される
        }
    }

    @Override
    protected Integer doInBackground() {
        lap = System.nanoTime();
        int fraction = Project.getPreferences().getInt(Project.MAX_FRAGMENT, 40);

        // 選択リストにあって 現在の karteList にないものは追加する
        List<Long> added = new ArrayList<>(); // 追加されたもの
        Set<Long> karteListPkSet = karteViewerList.stream().map(viewer -> viewer.getDocument().getDocInfo().getDocPk()).collect(Collectors.toSet());

        for (DocInfoModel selectedDocInfo : selectedHistoryMap.values()) {
            if (!karteListPkSet.contains(selectedDocInfo.getDocPk())) {
                logger.debug("added: docPk={}", selectedDocInfo.getDocPk());
                added.add(selectedDocInfo.getDocPk());
            }
        }

        if (!added.isEmpty()) {
            // 描画時のパタ付きを防ぐためにソートしておく
            added.sort(chart.getDocumentHistory().isAscending() ? Comparator.naturalOrder() : Comparator.reverseOrder());

            DocumentDelegater ddl = new DocumentDelegater();
            int count = 0;
            boolean hasNext = true;
            while (hasNext) {
                List<Long> pkList = new ArrayList<>(fraction);
                List<DocumentModel> toAdd = new ArrayList<>();

                for (int i = 0; i < fraction; i++) {
                    long test = added.get(count++);
                    // DocumentModel の cache をチェック
                    DocumentModel dm = DocumentModelCache.get(test);
                    if (dm == null) {
                        // DocumentModel がキャッシュに無ければ取りに行く分のリストに追加
                        pkList.add(test);
                    } else {
                        logger.debug("DocumentModel cache hit");
                        toAdd.add(dm);
                    }
                    if (count >= added.size()) {
                        hasNext = false;
                        break;
                    }
                }
                logger.debug("total count = {}, fraction = {}, pkList count = {}", count, fraction, pkList.size());

                // 途中で選択が変わったり，ウインドウが閉じられている可能性がある
                if (!isCancelled() && chart.getFrame().isVisible()) {
                    if (!pkList.isEmpty()) {
                        logger.debug("fetching {} documents", pkList.size());
                        // この時点では DocumentModel.getDocInfo().getDocPk() == 0
                        List<DocumentModel> fetchedModels = ddl.getDocuments(pkList);
                        toAdd.addAll(fetchedModels);
                        // DocumentModel キャッシュに保存
                        if (Project.getPreferences().getBoolean(Project.PREFETCH_CACHING, false)) {
                            DocumentModelCache.addAll(fetchedModels);
                        }
                    }
                    addKarteViewer(toAdd);
                    setProgress(count*100 / added.size());

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
        SwingUtilities.invokeLater(() -> logger.debug("lap time: {}", (System.nanoTime() - lap)/1000000));

        // リソース開放 - SwingWorker は最終的に done() で終了する
        removePropertyChangeListener(this);
        context = null;
        chart = null;
        scrollerPanel = null;
        karteViewerList = null;
        selectedHistoryMap = null;
        progressBar = null;
    }

    private void addKarteViewer(List<DocumentModel> models) {
        updateKarteViewerList(models);

        // karteViewerList の内容を scrollerPanel に反映する
        List<Component> currentViewerUIs = new ArrayList<>();
        for (KarteViewer2 viewer : karteViewerList) {
            currentViewerUIs.add(viewer.getUI());
        }
        publish(currentViewerUIs);
    }

    /// KarteViewerList を更新する
    private synchronized void updateKarteViewerList(List<DocumentModel> models) {
        // 選択解除された KarteViewer2 のリストを作る
        List<KarteViewer2> removed = new ArrayList<>(); // 選択が解除されているもの

        // karteViewerList にあって選択リストにないものは除去リストに入れる
        for (KarteViewer2 viewer : karteViewerList) {
            viewer.stop(); // 全てリソース開放. 新たに作るとき KarteViewer.start() で KartePanel がキャッシュから読まれる.
            long pk = viewer.getDocument().getDocInfo().getDocPk();
            if (!selectedHistoryMap.containsKey(pk)) {
                removed.add(viewer);
            }
        }
        // 削除リストを karteViewerList から取り除く
        karteViewerList.removeAll(removed);

        // 残った処理対象の DocumentModel#id (docPk) セットを作る
        Set<Long> karteListDocPkSet = karteViewerList.stream().map(v -> v.getDocument().getId()).collect(Collectors.toSet());

        // 追加された DocumentModel から　karteViewer をつくって karteList に加える
        for (DocumentModel dm : models) {
            // karteList にないものがあれば, KarteViewer を作って加える
            if (!karteListDocPkSet.contains(dm.getId())) {

                // 対応する DocInfoModel を探す
                DocInfoModel foundInfo = selectedHistoryMap.values().stream()
                        .filter(d -> d.getDocPk() == dm.getId()).findFirst().orElse(null);
                if (foundInfo != null) {
                    // 実体のある docInfo をセットする
                    logger.debug("found docInfo: docPk={}", foundInfo.getDocPk());
                    dm.setDocInfo(foundInfo);
                } else {
                    // 選択が変化して，せっかく取ってきた DocumentModel が無駄になった
                    logger.debug("selection changed, document fetched in vain");
                    break;
                }

                // KarteViewer を生成する
                final KarteViewer2 karteViewer = new KarteViewer2();
                karteViewer.setKarteDocumentViewer(context);
                karteViewer.setDocument(dm);

                // このコールでモデルのレンダリングが開始される
                karteViewer.start();
                // ボーダーをあらかじめ付けておく
                karteViewer.getKartePanel().setBackground(karteViewer.getSoaKartePane().getTextPane().getBackground());
                karteViewer.setSelected(false);

                karteViewerList.add(karteViewer);
            }
        }
        // 時間軸でソート
        karteViewerList.sort(chart.getDocumentHistory().isAscending() ? Comparator.naturalOrder() : Comparator.reverseOrder());
    }

    /// karteViewerList の内容を scrollerPanel に反映する
    private void updateScrollerPanel(List<List<Component>> chunks) {
        // 1. 全 chunk をマージ → 目標コンポーネント 1 回だけ構築
        List<Component> target = new ArrayList<>();
        Set<Component> targetSet = new HashSet<>();
        for (List<Component> chunk : chunks) {
            for (Component c : chunk) {
                if (targetSet.add(c)) {
                    target.add(c);
                }
            }
        }

        if (target.isEmpty()) {
            return;
        }

        // 2. 現在コンポーネントを 1 回だけスナップショット
        Component[] current = scrollerPanel.getComponents();
        Set<Component> currentSet = new HashSet<>(Arrays.asList(current));

         // 3. 差分がゼロなら z-order だけ
        if (currentSet.equals(targetSet)) {
            for (int i = 0; i < target.size(); i++) {
                scrollerPanel.setComponentZOrder(target.get(i), i);
            }
            scrollerPanel.revalidate();
            scrollerPanel.repaint();
            return;
        }

        // 4. バッチ処理中だけ ContainerListener を一時的に外す
        ContainerListener[] listeners = scrollerPanel.getContainerListeners();
        for (ContainerListener l : listeners) {
            scrollerPanel.removeContainerListener(l);
        }
        try {
            // 4a. 目標にないものを削除
            for (Component c : current) {
                if (!targetSet.contains(c)) {
                    scrollerPanel.remove(c);
                }
            }
            // 4b. 未登録のものを末尾追加（index 付き add は使わない）
            for (Component c : target) {
                if (c.getParent() != scrollerPanel) {
                    scrollerPanel.add(c);
                }
            }
            // 4c. z-order をまとめて設定
            for (int i = 0; i < target.size(); i++) {
                scrollerPanel.setComponentZOrder(target.get(i), i);
            }
        } finally {
            for (ContainerListener l : listeners) {
                scrollerPanel.addContainerListener(l);
            }
        }

        // repaint
        scrollerPanel.revalidate();
        scrollerPanel.repaint();
    }
}
