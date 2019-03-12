package open.dolphin.infomodel;

import javax.persistence.*;
import java.util.Date;

/**
 * PhenomenonModel.
 *
 * @author Minagawa, Kazushi
 */
@Entity
@Table(name = "d_phenomenon")
public class PhenomenonModel extends InfoModel {
    private static final long serialVersionUID = -5238918766376204524L;

    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    // 患者ID、UserId、施設ID等
    @Column(nullable = false)
    private long partyId;

    @Column(nullable = false)
    private String phenomenon;

    @Column(name="c_value", nullable = false)
    private String value;

    private String valueDesc;

    private String valueSys;

    @Temporal(value = TemporalType.DATE)
    private Date startDate;

    @Temporal(value = TemporalType.DATE)
    private Date endDate;

    @Temporal(value = TemporalType.DATE)
    private Date recorded;


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getPartyId() {
        return partyId;
    }

    public void setPartyId(long partyId) {
        this.partyId = partyId;
    }

    public String getPhenomenon() {
        return phenomenon;
    }

    public void setPhenomenon(String phenomenon) {
        this.phenomenon = phenomenon;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getValueDesc() {
        return valueDesc;
    }

    public void setValueDesc(String valueDesc) {
        this.valueDesc = valueDesc;
    }

    public String getValueSys() {
        return valueSys;
    }

    public void setValueSys(String valueSys) {
        this.valueSys = valueSys;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Date getRecorded() {
        return recorded;
    }

    public void setRecorded(Date recorded) {
        this.recorded = recorded;
    }

    @Override
    public int hashCode() {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + (int) (id ^ (id >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final PhenomenonModel other = (PhenomenonModel) obj;
        return (id == other.getId());
    }
}
