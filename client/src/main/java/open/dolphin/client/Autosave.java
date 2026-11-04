package open.dolphin.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.smile.databind.SmileMapper;
import open.dolphin.infomodel.DocumentModel;
import open.dolphin.infomodel.SchemaModel;
import open.dolphin.ui.sheet.JSheet;
import open.dolphin.util.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/// 編集中の KarteEditor を定期的に一時保存する.
///
/// @author pns
public class Autosave implements Runnable {
    private final static int INTERVAL = 2;
    private final static int INITIAL_DELAY = 2;
    private final static String SUFFIX = "open.dolphin.autosave";
    private final static String TMP_DIR = System.getProperty("java.io.tmpdir");
    private final KarteEditor editor;
    private File tmpFile;
    private boolean shouldSave = true;
    // ObjectMapper 関連
    private static final ObjectMapper objectMapper = new SmileMapper();
    static { JsonUtils.initialize(objectMapper); }
    private static final ObjectReader objectReader = objectMapper.readerFor(SaveModel.class);
    private static final ObjectWriter objectWriter = objectMapper.writerFor(SaveModel.class);
    // 自動セーブのタイマー
    private ScheduledExecutorService executor;
    // Logger
    private final Logger logger = LoggerFactory.getLogger(Autosave.class);

    public Autosave(KarteEditor e) {
        editor = e;
    }

    /// pid と doc id から temporary file を作る.
    ///
    /// @param patientId PatientId
    /// @param docId     DocID
    /// @return File
    private static File getTemporaryFile(String patientId, String docId) {
        String path = String.format("%s%s-%s.%s", TMP_DIR, patientId, docId, SUFFIX);
        return new File(path);
    }

    /// 保存された Temporary file を読み込んで SaveModel を生成する.
    ///
    /// @return list of SaveModel
    private static List<SaveModel> load() {
        File[] autoSavedFiles = new File(TMP_DIR).listFiles(fn -> fn.getName().endsWith(SUFFIX));
        if (autoSavedFiles == null) { return new ArrayList<>(); }
        return Stream.of(autoSavedFiles).<SaveModel>map(f -> {
                    try {
                        return objectReader.readValue(f);
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                }).toList();
    }

    /// 保存されていない編集中カルテをチェックして編集するかどうか決める.
    /// KarteDocumentViewer から static に呼ばれる
    ///
    /// @param chart ChartImpl
    public static void checkForTemporaryFile(final ChartImpl chart) {
        String chartPid = chart.getKarte().getPatient().getPatientId();
        List<SaveModel> targetModels = load().stream()
                .filter(m -> chartPid.equals(m.getPatientId())).toList();
        if (targetModels.isEmpty()) { return; }

        SwingUtilities.invokeLater(() -> {
            String message = "保存されていない編集中カルテがみつかりました. \nエディタで開きますか?";
            String[] selection = {"開く", "破棄"};
            int opt = JSheet.showOptionDialog(chart.getFrame(), message, "保存されていないカルテ",
                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, selection, selection[0]);

            switch (opt) {
                case JOptionPane.YES_OPTION ->
                    targetModels.forEach(saveModel -> {
                        DocumentModel dm = saveModel.getDocument();
                        // ImageIcon を作り直す
                        for (SchemaModel schema : dm.getSchema()) {
                            schema.setIcon(new ImageIcon(schema.getJpegByte()));
                        }
                        KarteEditor editor = chart.createEditor();
                        editor.setDocument(dm);
                        editor.setEditable(true);
                        editor.setModify(true);

                        EditorFrame editorFrame = new EditorFrame();
                        editorFrame.setChart(chart);
                        editorFrame.setKarteEditor(editor);
                        editorFrame.start();
                        // EDT の最後に setDirty を載せる
                        SwingUtilities.invokeLater(() -> editor.setDirty(true));
                    });

                case JOptionPane.NO_OPTION, JOptionPane.CLOSED_OPTION ->
                    targetModels.stream()
                        .map(s -> getTemporaryFile(s.getPatientId(), s.getDocument().getDocInfo().getDocId()))
                        .forEach(File::delete);
            }
        });
    }

    /// 編集中カルテの記録を開始する.
    public void start() {
        String patientId = editor.getContext().getPatient().getPatientId();
        String docId = editor.getDocument().getDocInfo().getDocId();

        tmpFile = getTemporaryFile(patientId, docId);
        logger.info(String.valueOf(tmpFile));

        executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleWithFixedDelay(this, INITIAL_DELAY, INTERVAL, TimeUnit.SECONDS);
    }

    /// 編集中カルテの記録を終了し, TemporaryFile を消去する.
    public void stop() {
        if (! (executor.isShutdown() || executor.isTerminated())) {
            executor.shutdownNow();
            try {
                executor.awaitTermination(1, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace(System.err);
            }
            tmpFile.delete();

        } else {
            logger.warn("stop() is called, but is already shutdown");
        }
    }

    /// KarteEditor から Dirty 情報を受け取る.
    ///
    /// @param dirty new dirty flag
    public void setDirty(boolean dirty) {
        shouldSave = dirty;
    }

    /// 編集中の KarteEditor を SaveModel にしてファイル保存する.
    @Override
    public void run() {
        if (shouldSave) {
            long l = System.nanoTime();
            try {
                SaveModel saveModel = new SaveModel();
                saveModel.setPatientId(editor.getContext().getKarte().getPatient().getPatientId());
                saveModel.setDocument(editor.getDocument());
                objectWriter.writeValue(tmpFile, saveModel);

            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                shouldSave = false;
                logger.debug("autosave took {} ms", (System.nanoTime() - l)/1000000);
            }
        }
    }

    private static class SaveModel {
        private String patientId;
        private DocumentModel documentModel;

        public SaveModel() {}
        public void setPatientId(String patientId) { this.patientId = patientId; }
        public String getPatientId() { return patientId; }
        public void setDocument(DocumentModel documentModel) { this.documentModel = documentModel; }
        public DocumentModel getDocument() { return documentModel; }
    }
}
