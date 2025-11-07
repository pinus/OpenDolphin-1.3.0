package open.dolphin.orca.orcaapi.bean;

/**
 * Medication_Information. 服用情報(繰り返し5)
 * Medication_Information. 診療行為コード情報
 *
 * @author pns
 */
public class MedicationInformation {
    /**
     * 服用時点(0:服用しない、1:服用する) (例:  )
     */
    private String Medication_Point;

    /**
     * 診療行為コード (例: 850100106)
     */
    private String Medication_Code;

    /**
     * 名称 (例: 往診又は訪問診療年月日（在医総管）)
     */
    private String Medication_Name;

    /**
     * カナ名称 (例:  )
     */
    private String Medication_Name_inKana;

    /**
     * 単位コード (例:  )
     */
    private String Unit_Code;

    /**
     * 単位名称 (例:  )
     */
    private String Unit_Name;

    /**
     * 有効開始日 (例: 2024-06-01)
     */
    private String StartDate;

    /**
     * 有効終了日 (例: 99999999)
     */
    private String EndDate;


    /**
     * 服用時点(0:服用しない、1:服用する) (例:  )
     *
     * @return the Medication_Point
     */
    public String getMedication_Point() {
        return Medication_Point;
    }

    /**
     * 服用時点(0:服用しない、1:服用する) (例:  )
     *
     * @param Medication_Point the Medication_Point to set
     */
    public void setMedication_Point(String Medication_Point) {
        this.Medication_Point = Medication_Point;
    }

    /**
     * Medication_Code
     *
     * @return Medication_Code
     */
    public String getMedication_Code() {
        return Medication_Code;
    }

    /**
     * Medication_Code
     *
     * @param Medication_Code to set
     */
    public void setMedication_Code(String Medication_Code) {
        this.Medication_Code = Medication_Code;
    }

    /**
     * Medication_Name
     *
     * @return Medication_Name
     */
    public String getMedication_Name() {
        return Medication_Name;
    }

    /**
     * Medication_Name
     *
     * @param Medication_Name to set
     */
    public void setMedication_Name(String Medication_Name) {
        this.Medication_Name = Medication_Name;
    }

    /**
     * Medication_Name_inKana
     *
     * @return Medication_Name_inKana
     */
    public String getMedication_Name_inKana() {
        return Medication_Name_inKana;
    }

    /**
     * Medication_Name_inKana
     *
     * @param Medication_Name_inKana to set
     */
    public void setMedication_Name_inKana(String Medication_Name_inKana) {
        this.Medication_Name_inKana = Medication_Name_inKana;
    }

    /**
     * Unit_Code
     *
     * @return Unit_Code
     */
    public String getUnit_Code() {
        return Unit_Code;
    }

    /**
     * Unit_Code
     *
     * @param Unit_Code to set
     */
    public void setUnit_Code(String Unit_Code) {
        this.Unit_Code = Unit_Code;
    }

    /**
     * Unit_Name
     *
     * @return Unit_Name
     */
    public String getUnit_Name() {
        return Unit_Name;
    }

    /**
     * Unit_Name
     *
     * @param Unit_Name to set
     */
    public void setUnit_Name(String Unit_Name) {
        this.Unit_Name = Unit_Name;
    }

    /**
     * StartDate
     *
     * @return StartDate
     */
    public String getStartDate() {
        return StartDate;
    }

    /**
     * StartDate
     *
     * @param StartDate to set
     */
    public void setStartDate(String StartDate) {
        this.StartDate = StartDate;
    }

    /**
     * EndDate
     *
     * @return EndDate
     */
    public String getEndDate() {
        return EndDate;
    }

    /**
     * EndDate
     *
     * @param EndDate to set
     */
    public void setEndDate(String EndDate) {
        this.EndDate = EndDate;
    }
}
