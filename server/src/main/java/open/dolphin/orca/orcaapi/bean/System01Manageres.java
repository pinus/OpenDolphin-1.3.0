package open.dolphin.orca.orcaapi.bean;

/**
 * System01Manageres.
 * contains responses to system01_managereq (either departmentres/physicianres/system1001res)
 *
 * @author pns
 */
public class System01Manageres {
    /**
     * 診療科コード一覧.
     */
    private Departmentres departmentres;

    /**
     * ドクター・職員コード一覧.
     */
    private Physicianres physicianres;

    /**
     * 医療機関基本情報.
     */
    private System1001res system1001res;

    /**
     * 入金方法情報.
     */
    private Incomeres incomeres;

    /**
     * 診療内容情報.
     */
    private Medicalinfres medicalinfres;

    /**
     * 患者状態コメント情報.
     */
    private Ptconditionres ptconditionres;


    /**
     * departmentres
     *
     * @return departmentres
     */
    public Departmentres getDepartmentres() {
        return departmentres;
    }

    /**
     * departmentres
     *
     * @param departmentres to set
     */
    public void setDepartmentres(Departmentres departmentres) {
        this.departmentres = departmentres;
    }

    /**
     * physicianres
     *
     * @return physicianres
     */
    public Physicianres getPhysicianres() {
        return physicianres;
    }

    /**
     * physicianres
     *
     * @param physicianres to set
     */
    public void setPhysicianres(Physicianres physicianres) {
        this.physicianres = physicianres;
    }

    /**
     * system1001res
     *
     * @return system1001res
     */
    public System1001res getSystem1001res() {
        return system1001res;
    }

    /**
     * system1001res
     *
     * @param system1001res to set
     */
    public void setSystem1001res(System1001res system1001res) {
        this.system1001res = system1001res;
    }

    /**
     * incomeres
     *
     * @return incomeres
     */
    public Incomeres getIncomeres() {
        return incomeres;
    }

    /**
     * incomeres
     *
     * @param incomeres to set
     */
    public void setIncomeres(Incomeres incomeres) {
        this.incomeres = incomeres;
    }

    /**
     * medicalinfres
     *
     * @return medicalinfres
     */
    public Medicalinfres getMedicalinfres() {
        return medicalinfres;
    }

    /**
     * medicalinfres
     *
     * @param medicalinfres to set
     */
    public void setMedicalinfres(Medicalinfres medicalinfres) {
        this.medicalinfres = medicalinfres;
    }

    /**
     * ptconditionres
     *
     * @return ptconditionres
     */
    public Ptconditionres getPtconditionres() {
        return ptconditionres;
    }

    /**
     * ptconditionres
     *
     * @param ptconditionres to set
     */
    public void setPtconditionres(Ptconditionres ptconditionres) {
        this.ptconditionres = ptconditionres;
    }
}
