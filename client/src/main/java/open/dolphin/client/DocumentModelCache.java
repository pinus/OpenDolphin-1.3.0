package open.dolphin.client;

import open.dolphin.delegater.DocumentDelegater;
import open.dolphin.infomodel.DocInfoModel;
import open.dolphin.infomodel.DocumentModel;
import open.dolphin.infomodel.KarteBean;
import open.dolphin.util.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
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

    private static final Map<String, DocumentModel> cache = new ConcurrentHashMap<>();

    private static final String SUFFIX = "open.dolphin.document.cache";
    private static final String TMP_DIR = System.getProperty("java.io.tmpdir");
    private static final File documentModelCacheFile = new File(TMP_DIR, LocalDate.now() + "." + SUFFIX);
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
            load();
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
        try {
            String json = Files.readString(documentModelCacheFile.toPath(), StandardCharsets.UTF_8);
            if (json.isBlank()) { return; }
            DocumentModel[] models = JsonUtils.fromJson(json, DocumentModel[].class);
            if (models == null) { return; }
            for (DocumentModel model : models) {
                if (model != null && model.getDocInfo() != null) {
                    cache.put(model.getDocInfo().getDocId(), model);
                }
            }
            logger.info("loaded {} models from {}", cache.size(), documentModelCacheFile);
        } catch (IOException | RuntimeException e) {
            logger.warn("failed to load {}: {}", documentModelCacheFile, e.toString());
        }
    }

    /// cache の内容をファイルに保存する.
    private static synchronized void save() {
        try {
            String json = JsonUtils.toJson(cache.values().toArray(new DocumentModel[0]));
            if (json == null) { return; }
            Files.writeString(documentModelCacheFile.toPath(), json, StandardCharsets.UTF_8);
            logger.debug("saved {} models to {}", cache.size(), documentModelCacheFile);
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
        cache.put(model.getDocInfo().getDocId(), model);
        logger.debug("add model {}", model.getDocInfo().getDocId());
        scheduleSave();
    }

    public static void addAll(List<DocumentModel> models) {
        models.forEach(DocumentModelCache::add);
    }

    /// docId に対応する DocumentModel をキャッシュから取得する. なければ null を返す.
    public static DocumentModel get(String docId) {
        return cache.get(docId);
    }

    /// docId に対応する DocumentModel がキャッシュに存在するかを返す.
    public static boolean contains(String docId) {
        return cache.containsKey(docId);
    }

    public static void clear() {
        cache.clear();
    }

    /// 受付時に DocumentModel を全部取ってきてキャッシュしてしまう.
    /// ORCA で受付してから OpenDolphin に表示されるまでの時間が多少遅くなっても実用上問題ない.
    ///
    /// @param patientPk the primary key (identifier) of the patient whose documents
    ///                  are to be added to the cache
    /// @throws RuntimeException if an error occurs during execution, including timeout,
    ///                          interruption, or other execution failures
    public static void add(long patientPk) {
        logger.info("add patientPk {}", patientPk);
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            long lap = System.currentTimeMillis();

            // KarteBean を取りに行く
            LocalDateTime from = LocalDate.now().atStartOfDay().minusMonths(360);
            DocumentDelegater ddl = new DocumentDelegater();
            KarteBean karte = ddl.getKarte(patientPk, from);
            // DocumentModel 取得のための pk list を作り, 同時に DocInfoModel の map を作る
            List<Long> docIds = new ArrayList<>();
            Map<String, DocInfoModel> docInfoMap = new HashMap<>();
            karte.getDocInfoEntry().forEach(docInfo -> {
                docInfoMap.put(docInfo.getDocId(), docInfo);
                docIds.add(docInfo.getDocPk());
            });

            List<DocumentModel> models = ddl.getDocuments(docIds);

            // 取得した DocumentModel に DocInfoModel の実体をセットして cache に追加する
            models.forEach(model -> {
                model.setDocInfo(docInfoMap.get(model.getDocInfo().getDocId()));
                add(model);
            });

            logger.debug("lap = {}", (System.currentTimeMillis() - lap));
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
