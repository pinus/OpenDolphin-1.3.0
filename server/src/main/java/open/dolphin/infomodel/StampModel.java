package open.dolphin.infomodel;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import javax.persistence.*;

/**
 * StampModel
 *
 * @author Minagawa,Kazushi
 */
@Entity
@Table(name="d_stamp")
public class StampModel extends InfoModel {
    private static final long serialVersionUID = 7588591951375345379L;

    @Id
    private String id;

    // UserPK
    @Column(nullable=false)
    private long userId;

    @Column(nullable=false)
    private String entity;

    @Column(nullable=false)
    @Lob
    private byte[] stampBytes;

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
    @Transient
    private IInfoModel stamp;

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getUserId() {
        return userId;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }

    public String getEntity() {
        return entity;
    }

    public void setStampBytes(byte[] stampBytes) {
        this.stampBytes = stampBytes;
    }

    public byte[] getStampBytes() {
        return stampBytes;
    }

    public IInfoModel getStamp() {
        return stamp;
    }

    public void setStamp(IInfoModel stamp) {
        this.stamp = stamp;
    }

    @Override
    public int hashCode() {
        return (id == null)? "".hashCode() : id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final StampModel other = (StampModel) obj;
        return id.equals(other.getId());
    }
}
