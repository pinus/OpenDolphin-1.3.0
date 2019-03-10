package open.dolphin.delegater;

import java.util.*;

import open.dolphin.infomodel.*;
import open.dolphin.service.PnsService;
import open.dolphin.util.ModelUtils;

/**
 * いろいろやってみる delegater.
 *
 * @author pns
 */
public class PnsDelegater extends BusinessDelegater<PnsService> {

    /**
     * 今日のカルテの記載を返す.（カルテ未記入をチェックするため）
     *
     * @param patientId PatientModel の pk
     * @return 今日のカルテの記載
     */
    public String peekKarte(Long patientId) {
        List<ModuleModel> list = getService().peekKarte(patientId);
        // 今日のカルテがない場合
        if (list == null) {
            return null;
        }

        String text = null;

        // soa の記入を取り出す
        for (ModuleModel bean : list) {
            String role = bean.getModuleInfo().getStampRole();
            if (role.equals(IInfoModel.ROLE_SOA_SPEC)) {
                String xml = ((ProgressCourse) bean.getModel()).getFreeText();
                text = ModelUtils.extractText(xml);
                break;
            }
        }
        return text;
    }

    /**
     * HibernateSearch 用のインデックスを作る.
     */
    public void makeInitialIndex() {
        getService().makeInitialIndex();
    }
}
