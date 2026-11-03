package open.dolphin.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.smile.databind.SmileMapper;
import open.dolphin.delegater.DocumentDelegater;
import open.dolphin.infomodel.*;
import open.dolphin.project.Project;
import open.dolphin.util.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/// DocumentModel Cache.
/// キャッシュはファイルに保存する.
///
/// @author pns
public class DocumentModelCache {
    private static final long TIMEOUT_SECONDS = 30;
    private static final long SAVE_DELAY_MILLIS = 1000;

    // DocumentModel(KarteBean)#id (DocInfoModel#docPk) をキーとした HashMap
    private static final Map<Long, DocumentModel> cache = new ConcurrentHashMap<>();
    private static final Map<Long, KartePanel> kartePanelCache = new ConcurrentHashMap<>();
    // Cache ファイル関連
    private static final String SUFFIX = "open.dolphin.documentcache";
    private static final String TMP_DIR = System.getProperty("java.io.tmpdir");
    private static final File documentModelCacheFile = new File(TMP_DIR, LocalDate.now() + "." + SUFFIX);
    // ObjectMapper 関連
    private static final ObjectMapper objectMapper = new SmileMapper();
    static { JsonUtils.initialize(objectMapper); }
    private static final ObjectReader objectReader = objectMapper.readerFor(DocumentModel[].class);
    private static final ObjectWriter objectWriter = objectMapper.writerFor(DocumentModel[].class);

    private static final Logger logger = LoggerFactory.getLogger(DocumentModelCache.class);

    private static final ScheduledExecutorService saver = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r);
        t.setDaemon(true);
        return t;
    });
    private static ScheduledFuture<?> saveTask;

    static {
        deleteOldFiles();
        if (documentModelCacheFile.exists()) {
            if (Project.getPreferences().getBoolean(Project.PREFETCH_CACHING, false)) {
                load();
            }
        } else {
            save();
        }
    }

    /// 今日の日付より古いキャッシュファイルを削除する.
    private static void deleteOldFiles() {
        File[] files = new File(TMP_DIR).listFiles((dir, name) -> name.endsWith(SUFFIX));
        if (files == null) { return; }
        for (File file : files) {
            if (!file.getName().equals(documentModelCacheFile.getName())) {
                if (file.delete()) {
                    logger.info("deleted old cache file {}", file);
                } else {
                    logger.warn("failed to delete old cache file {}", file);
                }
            }
        }
    }

    /// ファイルから cache に読み込む.
    private static void load() {
        if (!documentModelCacheFile.exists()) { return; }
        try {
            long lap = System.nanoTime();
            DocumentModel[] models = objectReader.readValue(documentModelCacheFile);
            if (models == null) { return; }
            for (DocumentModel model : models) {
                if (model != null && model.getDocInfo() != null) {
                    // ImageIcon は作り直す必要あり
                    for (SchemaModel schema : model.getSchema()) {
                        schema.setIcon(new ImageIcon(schema.getJpegByte()));
                    }
                    cache.put(model.getId(), model);
                    // ついでに KartePanel をレンダリングしてしまう
                    kartePanelCacheAdd(model.getId(), createKartePanel(model));
                }
            }
            logger.info("loaded {} models from {} in {} ms", cache.size(), documentModelCacheFile, (System.nanoTime() - lap)/1000000);

        } catch (IOException | RuntimeException e) {
            logger.warn("failed to load {}: {}", documentModelCacheFile, e.toString());
        }
    }

    /// cache の内容をファイルに保存する.
    private static synchronized void save() {
        try {
            long lap = System.nanoTime();
            objectWriter.writeValue(documentModelCacheFile, cache.values().toArray(new DocumentModel[0]));
            logger.debug("saved {} models to {} in {} ms", cache.size(), documentModelCacheFile, (System.nanoTime() - lap)/1000000);

        } catch (IOException | RuntimeException e) {
            logger.warn("failed to save {}: {}", documentModelCacheFile, e.toString());
        }
    }

    /// add が連続することがあるので, add が途切れて一定時間経ってからまとめて保存する.
    private static synchronized void scheduleSave() {
        if (saveTask != null) { saveTask.cancel(false); }
        saveTask = saver.schedule(DocumentModelCache::save, SAVE_DELAY_MILLIS, TimeUnit.MILLISECONDS);
    }

    /// DocumentModel をキャッシュに入れて, cache の内容をファイルに保存する.
    public static void add(DocumentModel model) {
        if (Project.getPreferences().getBoolean(Project.PREFETCH_CACHING, false)) {
            cache.put(model.getId(), model);
            logger.debug("add model {}", model.getId());
            scheduleSave();
        }
    }

    /// DocumentModel をキャッシュに入れて, cache の内容をファイルに保存する.
    public static void addAll(List<DocumentModel> models) {
        models.forEach(DocumentModelCache::add);
    }

    /// docId に対応する DocumentModel をキャッシュから取得する. なければ null を返す.
    public static DocumentModel get(long docPk) {
        return cache.get(docPk);
    }

    /// docId に対応する DocumentModel がキャッシュに存在するかを返す.
    public static boolean contains(long docPk) {
        return cache.containsKey(docPk);
    }

    /// KartePanel をキャッシュに入れる
    public static void kartePanelCacheAdd(long docPk, KartePanel kartePanel) {
        if (Project.getPreferences().getBoolean(Project.PREFETCH_CACHING, false)) {
            kartePanelCache.put(docPk, kartePanel);
        }
    }

    /// KartePanel をキャッシュから取り出す
    public static KartePanel kartePanelCacheGet(DocumentModel model) {
        return kartePanelCache.get(model.getId());
    }

    /// KartePanel をレンダリングする
    public static KartePanel createKartePanel(DocumentModel model) {
        KartePanel kartePanel = KartePanelFactory.createViewerPanel();
        KarteRenderer_2 renderer = new KarteRenderer_2(kartePanel.getSoaKartePane(), kartePanel.getPKartePane());
        renderer.render(model);
        return kartePanel;
    }

    /// KartePanel cache をクリアする
    public static void clearKartePanelCache() {
        logger.info("KartePanel cache cleared");
        kartePanelCache.clear();
    }

    /// キャッシュを全て消去し, 保存ファイルも消去.
    public static void clear() {
        kartePanelCache.clear();
        cache.clear();
        boolean deleted = documentModelCacheFile.delete();
        logger.info("Cache cleared and cache file deleted: {}", deleted);
    }

    /// 受付時に DocumentModel を全部取ってきてキャッシュしてしまう.
    /// ORCA で受付してから OpenDolphin に表示されるまでの時間が多少遅くなっても実用上問題ない.
    public static void add(PatientVisitModel pvt) {
        long patientPk = pvt.getPatient().getId();
        logger.info("add patientPk {}", patientPk);

        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            long lap = System.nanoTime();

            // KarteBean を取りに行く
            LocalDateTime from = LocalDate.now().atStartOfDay().minusMonths(360);
            DocumentDelegater ddl = new DocumentDelegater();
            KarteBean karte = ddl.getKarte(patientPk, from);
            karte.setPatient(pvt.getPatient());

            // DocumentModel 取得のための pk list を作り, 同時に DocInfoModel の map を作る
            Map<Long, DocInfoModel> docInfoMap = new HashMap<>();
            karte.getDocInfoEntry().forEach(docInfo -> docInfoMap.put(docInfo.getDocPk(), docInfo));
            List<DocumentModel> models = ddl.getDocuments(new ArrayList<>(docInfoMap.keySet()));

            // 取得した DocumentModel に DocInfoModel の実体をセットして cache に追加する
            models.forEach(model -> {
                model.setDocInfo(docInfoMap.get(model.getId()));
                add(model);

                // ついでに KartePanel をレンダリングしてしまう
                kartePanelCacheAdd(model.getId(), createKartePanel(model));
            });

            logger.debug("lap = {}", (System.nanoTime() - lap)/1000000);
        });

        try {
            future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        } catch (TimeoutException e) {
            future.cancel(true);
            logger.warn("timed out ({} sec)", TIMEOUT_SECONDS);
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            future.cancel(true);
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e.getCause() != null ? e.getCause() : e);
        }
    }
}
