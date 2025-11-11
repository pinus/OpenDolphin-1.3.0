package open.dolphin.orca.orcaapi.bean;

/**
 * medicalgetreq.
 *
 * @author pns
 */
public class Medicalgetreq {
    /**
     * 入外区分(I：入院) (例: I)
     */
    private String InOut;

    /**
     * 患者番号 (例: 12)
     */
    private String Patient_ID;

    /**
     * 診療日 (例: 2014-01-06)
     */
    private String Perform_Date;

    /**
     * 月数 (例: 12)
     */
    private String For_Months;

    /**
     * 対象更新日 (例: ) 追加 (2024-09-25)
     */
    private String Base_StartDate;

    /**
     * 対象更新時間 (例: ) 追加 (2024-09-25) (00:00:00 から 23:59:59の範囲内)
     */
    private String Base_StartTime;

    /**
     * 診療情報 (例:  )
     */
    private MedicalInformation3 Medical_Information;

    /**
     * 入外区分(I：入院) (例: I)
     *
     * @return the InOut
     */
    public String getInOut() {
        return InOut;
    }

    /**
     * 入外区分(I：入院) (例: I)
     *
     * @param InOut the InOut to set
     */
    public void setInOut(String InOut) {
        this.InOut = InOut;
    }

    /**
     * 患者番号 (例: 12)
     *
     * @return the Patient_ID
     */
    public String getPatient_ID() {
        return Patient_ID;
    }

    /**
     * 患者番号 (例: 12)
     *
     * @param Patient_ID the Patient_ID to set
     */
    public void setPatient_ID(String Patient_ID) {
        this.Patient_ID = Patient_ID;
    }

    /**
     * 診療日 (例: 2014-01-06)
     *
     * @return the Perform_Date
     */
    public String getPerform_Date() {
        return Perform_Date;
    }

    /**
     * 診療日 (例: 2014-01-06)
     *
     * @param Perform_Date the Perform_Date to set
     */
    public void setPerform_Date(String Perform_Date) {
        this.Perform_Date = Perform_Date;
    }

    /**
     * 月数 (例: 12)
     *
     * @return the For_Months
     */
    public String getFor_Months() {
        return For_Months;
    }

    /**
     * 月数 (例: 12)
     *
     * @param For_Months the For_Months to set
     */
    public void setFor_Months(String For_Months) {
        this.For_Months = For_Months;
    }

    /**
     * Base_StartDate
     *
     * @return Base_StartDate
     */
    public String getBase_StartDate() {
        return Base_StartDate;
    }

    /**
     * Base_StartDate
     *
     * @param Base_StartDate to set
     */
    public void setBase_StartDate(String Base_StartDate) {
        this.Base_StartDate = Base_StartDate;
    }

    /**
     * Base_StartTime
     *
     * @return Base_StartTime
     */
    public String getBase_StartTime() {
        return Base_StartTime;
    }

    /**
     * Base_StartTime
     *
     * @param Base_StartTime to set
     */
    public void setBase_StartTime(String Base_StartTime) {
        this.Base_StartTime = Base_StartTime;
    }

    /**
     * 診療情報 (例:  )
     *
     * @return the Medical_Information
     */
    public MedicalInformation3 getMedical_Information() {
        return Medical_Information;
    }

    /**
     * 診療情報 (例:  )
     *
     * @param Medical_Information the Medical_Information to set
     */
    public void setMedical_Information(MedicalInformation3 Medical_Information) {
        this.Medical_Information = Medical_Information;
    }
}