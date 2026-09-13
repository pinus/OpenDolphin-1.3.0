package open.dolphin.dto;

import java.time.LocalDateTime;

/**
 * KarteBeanSpec.
 *
 * @author pns
 */
public class KarteBeanSpec {
    // PatientModel primary key
    private long patientPk;
    private LocalDateTime fromDate;

    public long getPatientPk() {
        return patientPk;
    }

    public void setPatientPk(long patientPk) {
        this.patientPk = patientPk;
    }

    public LocalDateTime getFromDate() {
        return fromDate;
    }

    public void setFromDate(LocalDateTime fromDate) {
        this.fromDate = fromDate;
    }
}
