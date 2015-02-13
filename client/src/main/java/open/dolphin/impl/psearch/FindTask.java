package open.dolphin.impl.psearch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.SwingUtilities;
import open.dolphin.client.ClientContext;
import open.dolphin.delegater.PatientDelegater;
import open.dolphin.dto.PatientSearchSpec;
import open.dolphin.helper.Task;
import open.dolphin.infomodel.PatientModel;
import open.dolphin.table.ObjectReflectTableModel;
import org.apache.log4j.Logger;

/**
 * 検索タスクの実務をするクラス
 * @author pns
 */
class FindTask extends Task<Collection> {

    private PatientSearchPanel view;
    private ObjectReflectTableModel tableModel;
    private PatientSearchSpec spec;
    private List<PatientModel> result;

    private Logger logger = ClientContext.getBootLogger();

    /**
     * メモ検索 or 全文検索のコンストラクタ
     * @param view
     * @param message
     * @param note
     * @param searchText
     * @param option
     */
    public FindTask(PatientSearchPanel view, Object message, String note, PatientSearchSpec spec) {
        super(SwingUtilities.getWindowAncestor(view), message, note);
        this.view = view;
        this.spec = spec;
        tableModel = (ObjectReflectTableModel) view.getTable().getModel();
    }

    @Override
    protected Collection doInBackground() throws Exception {
        logger.debug("FindTask doInBackground");

        // auto narrowing search
        // table のリストが空の場合は全患者から，リストがあればその中で検索する
        // そのために，spec に PatientModel の id をセットしておく

        List<Long> ids = new ArrayList<Long>();

        if (view.getNarrowingSearchCb().isSelected()) {
            List<PatientModel> ptOnTable = tableModel.getObjectList();
            for (PatientModel pm : ptOnTable) {
                ids.add(pm.getId());
            }
        }
        spec.setNarrowingIds(ids);

        PatientDelegater pdl = new PatientDelegater();
        List<PatientModel> pm = pdl.getPatients(spec);

        // カルテ検索で薬の名前を検索すると患者名と判断されてしまうので，
        // 検索結果が 0 だったら，full text search に切り替えることにする
        if (pm.isEmpty() && (
                spec.getCode() == PatientSearchSpec.KANA_SEARCH
                || spec.getCode() == PatientSearchSpec.ROMAN_SEARCH
                || spec.getCode() == PatientSearchSpec.NAME_SEARCH)) {

            spec.setCode(PatientSearchSpec.FULL_TEXT_SEARCH);
            spec.setSearchText(spec.getName());
            pm = pdl.getPatients(spec);
        }

        result = new ArrayList<PatientModel>();
        result.addAll(pm);

        return result;
    }

    @Override
    protected void cancelled() {
        unfinished();
    }

    @Override
    protected void interrupted(InterruptedException ex) {
        unfinished();
    }

    @Override
    protected void failed(Throwable cause) {
        unfinished();
    }

    @Override
    protected void succeeded(Collection result) {
        setResult();
        logger.debug("FindTask succeeded");
    }

    private void unfinished() {
        // 途中経過の書き込み
        if (tableModel.getObjectCount() == 0) setResult();
    }

    /**
     * table に result をセットする
     */
    protected void setResult() {

        tableModel.setObjectList((ArrayList) result);

        // 件数表示
        int cnt = result != null ? result.size() : 0;
        String cntStr = String.valueOf(cnt);
        view.getCntLbl().setText(cntStr + " 件");
    }
}
