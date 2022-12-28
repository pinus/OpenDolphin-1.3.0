package open.dolphin.infomodel;

import jakarta.persistence.*;

/**
 * RadiologyMethodValue.
 *
 * @author Minagawa, kazushi
 */
@Entity
@Table(name = "d_radiology_method")
public class RadiologyMethodValue extends InfoModel {
    private static final long serialVersionUID = -710424383733112788L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    private String hierarchyCode1;

    private String hierarchyCode2;

    private String methodName;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getHierarchyCode1() {
        return hierarchyCode1;
    }

    public void setHierarchyCode1(String hierarchyCode1) {
        this.hierarchyCode1 = hierarchyCode1;
    }

    public String getHierarchyCode2() {
        return hierarchyCode2;
    }

    public void setHierarchyCode2(String hierarchyCode2) {
        this.hierarchyCode2 = hierarchyCode2;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    @Override
    public String toString() {
        return methodName;
    }

    @Override
    public int hashCode() {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + id;
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
        final RadiologyMethodValue other = (RadiologyMethodValue) obj;
        return (id == other.getId());
    }
}
