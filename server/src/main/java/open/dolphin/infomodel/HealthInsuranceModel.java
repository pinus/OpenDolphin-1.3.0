package open.dolphin.infomodel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.hibernate.annotations.ColumnTransformer;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/// HealthInsuranceModel.
///
/// @author Minagawa, kazushi
@Entity
@Table(name = "d_health_insurance")
public class HealthInsuranceModel extends InfoModel {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = InfoModel.GENERATOR_NAME)
    private long id;

    // @Lob
    // @Column(nullable = false)
    // masuda-sensei
    // oid(Large Object) 列を lo_get / lo_from_bytea で bytea として読み書きする。
    // JDBC の Blob 読み出し(lo_open/loread の往復)を避けるため。列の型は oid のまま
    @JsonIgnore
    @JdbcTypeCode(SqlTypes.VARBINARY)
    @ColumnTransformer(read = "lo_get(beanBytes)", write = "lo_from_bytea(0, ?)")
    @Column(nullable = false, columnDefinition = "oid")
    private byte[] beanBytes;

    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private PatientModel patient;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public byte[] getBeanBytes() {
        return beanBytes;
    }

    public void setBeanBytes(byte[] beanBytes) {
        this.beanBytes = beanBytes;
    }

    public PatientModel getPatient() {
        return patient;
    }

    public void setPatient(PatientModel patient) {
        this.patient = patient;
    }

    @Override
    public int hashCode() {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + Long.hashCode(id);
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
        final HealthInsuranceModel other = (HealthInsuranceModel) obj;
        return (id == other.getId());
    }
}
