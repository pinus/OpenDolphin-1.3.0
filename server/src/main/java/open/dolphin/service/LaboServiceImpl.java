package open.dolphin.service;

import java.util.List;
import javax.ejb.Stateless;
import open.dolphin.dto.LaboSearchSpec;
import open.dolphin.infomodel.KarteBean;
import open.dolphin.infomodel.LaboItemValue;
import open.dolphin.infomodel.LaboModuleValue;
import open.dolphin.infomodel.LaboSpecimenValue;
import open.dolphin.infomodel.PatientModel;

/**
 *
 * @author pns
 */
@Stateless
public class  LaboServiceImpl extends DolphinService implements LaboService {
    private static final long serialVersionUID = 3956888524428014377L;

    /**
     * LaboModuleを保存する。
     * @param laboModuleValue LaboModuleValue
     */
    @Override
    public PatientModel putLaboModule(LaboModuleValue laboModuleValue) {

        // MMLファイルをパースした結果が登録される
        // 施設IDはコンテキストから取得する
        String facilityId = getCallersFacilityId();

        // 施設IDと LaboModule の患者IDで 患者を取得する
        PatientModel exist = em.createQuery("select p from PatientModel p where p.facilityId = :fid and p.patientId = :pid", PatientModel.class)
            .setParameter("fid", facilityId)
            .setParameter("pid", laboModuleValue.getPatientId()).getSingleResult();

        // 患者のカルテを取得する
        KarteBean karte = em.createQuery("select k from KarteBean k where k.patient.id = :pk", KarteBean.class)
            .setParameter("pk", exist.getId()).getSingleResult();

        // laboModuleとカルテの関係を設定する
        laboModuleValue.setKarte(karte);

        // 永続化する
        em.persist(laboModuleValue);

        // IDをリターンする
        return exist;
    }

    /**
     * 患者の検体検査モジュールを取得する。
     * @param spec LaboSearchSpec 検索仕様
     * @return laboModule の List
     */
    @Override
    public List<LaboModuleValue> getLaboModuleList(LaboSearchSpec spec) {

        long karteId = spec.getKarteId();

        // 即時フェッチではない
        List<LaboModuleValue> modules = em.createQuery("select l from LaboModuleValue l where l.karte.id = :karteId and l.sampleTime between :sampleFrom and :sampleTo", LaboModuleValue.class)
            .setParameter("karteId", karteId)
            .setParameter("sampleFrom", spec.getFromDate())
            .setParameter("sampleTo", spec.getToDate()).getResultList();

        for (LaboModuleValue module : modules) {
            List<LaboSpecimenValue> specimens = em.createQuery("select l from LaboSpecimenValue l where l.laboModule.id = :moduleId", LaboSpecimenValue.class)
                .setParameter("moduleId", module.getId()).getResultList();

            module.setLaboSpecimens(specimens);

            for (LaboSpecimenValue specimen : specimens) {
                List<LaboItemValue> items = em.createQuery("select l from LaboItemValue l where l.laboSpecimen.id = :specimenId", LaboItemValue.class)
                    .setParameter("specimenId", specimen.getId()).getResultList();

                specimen.setLaboItems(items);
            }
        }
        return modules;
    }
}






















