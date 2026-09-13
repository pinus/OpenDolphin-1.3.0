package open.dolphin.client;

import open.dolphin.delegater.DocumentDelegater;
import open.dolphin.delegater.PnsDelegater;
import open.dolphin.dto.DiagnosisSearchSpec;
import open.dolphin.infomodel.KarteBean;
import open.dolphin.infomodel.PatientVisitModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

/**
 * ちょっとカルテ内容をチェックして pvt に必要な情報をセットする.
 *
 * @author pns
 */
public class DocumentPeeker {
    public static final int LEAST_KARTE_SIZE = 15; // カルテに必要な最低文字数

    private final DocumentDelegater ddl;
    private final PnsDelegater pnsdl;
    private final DiagnosisSearchSpec spec;
    private KarteBean karte;
    private PatientVisitModel pvt;
    private final LocalDateTime today;

    private final Logger logger;

    public DocumentPeeker() {
        //使い回す Object
        today = LocalDateTime.now();
        ddl = new DocumentDelegater();
        pnsdl = new PnsDelegater();
        spec = new DiagnosisSearchSpec();

        logger = LoggerFactory.getLogger(DocumentPeeker.class);
    }

    public DocumentPeeker(PatientVisitModel pvt) {
        this();
        setPatientVisitModel(pvt);
    }

    public static boolean isKarteEmpty(String text) {
        //logger.info(text);
        return text != null && (text.length() < LEAST_KARTE_SIZE || text.contains("+++"));
    }

    public void setPatientVisitModel(PatientVisitModel pvt) {
        this.pvt = pvt;
        karte = ddl.getKarte(pvt.getPatient().getId(), today);
    }

    public boolean isKarteEmpty() {
        if (pvt == null) {
            logger.info("pvt null -- set pvt!");
            return false;
        }
        String text = pnsdl.peekKarte(pvt.getPatient().getId());
        return isKarteEmpty(text);
    }
}
