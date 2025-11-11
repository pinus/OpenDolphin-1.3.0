package open.dolphin.orca.orcaapi.bean;

/**
 * Medicalinf_Information. 診療内容情報(繰り返し100)
 * @author pns
 */
public class MedicalinfInformation {
    /**
     * 診療内容区分 (例:  )
     */
    private String Medical_Information;

    /**
     * 診療内容 (例:  )
     */
    private String Medical_Information_Name;

    /**
     * 診療内容表示用 (例:  )
     */
    private String Medical_Information_Name2;

    /**
     * Medical_Information
     *
     * @return Medical_Information
     */
    public String getMedical_Information() {
        return Medical_Information;
    }

    /**
     * Medical_Information
     *
     * @param Medical_Information to set
     */
    public void setMedical_Information(String Medical_Information) {
        this.Medical_Information = Medical_Information;
    }

    /**
     * Medical_Information_Name
     *
     * @return Medical_Information_Name
     */
    public String getMedical_Information_Name() {
        return Medical_Information_Name;
    }

    /**
     * Medical_Information_Name
     *
     * @param Medical_Information_Name to set
     */
    public void setMedical_Information_Name(String Medical_Information_Name) {
        this.Medical_Information_Name = Medical_Information_Name;
    }

    /**
     * Medical_Information_Name2
     *
     * @return Medical_Information_Name2
     */
    public String getMedical_Information_Name2() {
        return Medical_Information_Name2;
    }

    /**
     * Medical_Information_Name2
     *
     * @param Medical_Information_Name2 to set
     */
    public void setMedical_Information_Name2(String Medical_Information_Name2) {
        this.Medical_Information_Name2 = Medical_Information_Name2;
    }
}
