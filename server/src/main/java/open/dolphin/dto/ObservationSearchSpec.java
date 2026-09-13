package open.dolphin.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * ObservationSearchSpec.
 *
 * @author Minagawa, kazushi
 */
public class ObservationSearchSpec implements Serializable {

    private long karteId;
    private String observation;
    private String phenomenon;
    private LocalDateTime firstConfirmed;

    public LocalDateTime getFirstConfirmed() {
        return firstConfirmed;
    }

    public void setFirstConfirmed(LocalDateTime firstConfirmed) {
        this.firstConfirmed = firstConfirmed;
    }

    public long getKarteId() {
        return karteId;
    }

    public void setKarteId(long karteId) {
        this.karteId = karteId;
    }

    public String getObservation() {
        return observation;
    }

    public void setObservation(String observation) {
        this.observation = observation;
    }

    public String getPhenomenon() {
        return phenomenon;
    }

    public void setPhenomenon(String phenomenon) {
        this.phenomenon = phenomenon;
    }
}
