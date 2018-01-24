package open.dolphin.dto;

import java.io.Serializable;

/**
 * LaboSearchSpec.
 *
 * @author Minagawa,Kazushi
 */
public class LaboSearchSpec implements Serializable {
    private static final long serialVersionUID = 2201738793947138141L;

    private long karteId;
    private String fromDate;
    private String toDate;

    public void setKarteId(long patientId) {
        this.karteId = patientId;
    }

    public long getKarteId() {
        return karteId;
    }

    public void setFromDate(String fromDate) {
        this.fromDate = fromDate;
    }

    public String getFromDate() {
        return fromDate;
    }

    public void setToDate(String toDate) {
        this.toDate = toDate;
    }

    public String getToDate() {
        return toDate;
    }
}
