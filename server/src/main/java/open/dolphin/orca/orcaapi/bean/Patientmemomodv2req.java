package open.dolphin.orca.orcaapi.bean;

/**
 * Patientmemomodv2req.
 *
 * @author pns
 */
public class Patientmemomodv2req {
    /**
     * リクエスト番号 (例: 01)
     */
    private String Request_Number;

    /**
     * 患者番号 (例: 1)
     */
    private String Patient_ID;

    /**
     * 登録日 (例: 2025-08-25)
     */
    private String Perform_Date;

    /**
     * Request_Number
     *
     * @return Request_Number
     */
    public String getRequest_Number() {
        return Request_Number;
    }

    /**
     * Request_Number
     *
     * @param Request_Number to set
     */
    public void setRequest_Number(String Request_Number) {
        this.Request_Number = Request_Number;
    }

    /**
     * Patient_ID
     *
     * @return Patient_ID
     */
    public String getPatient_ID() {
        return Patient_ID;
    }

    /**
     * Patient_ID
     *
     * @param Patient_ID to set
     */
    public void setPatient_ID(String Patient_ID) {
        this.Patient_ID = Patient_ID;
    }

    /**
     * Perform_Date
     *
     * @return Perform_Date
     */
    public String getPerform_Date() {
        return Perform_Date;
    }

    /**
     * Perform_Date
     *
     * @param Perform_Date to set
     */
    public void setPerform_Date(String Perform_Date) {
        this.Perform_Date = Perform_Date;
    }

    /**
     * Department_Code
     *
     * @return Department_Code
     */
    public String getDepartment_Code() {
        return Department_Code;
    }

    /**
     * Department_Code
     *
     * @param Department_Code to set
     */
    public void setDepartment_Code(String Department_Code) {
        this.Department_Code = Department_Code;
    }

    /**
     * Memo_Class
     *
     * @return Memo_Class
     */
    public String getMemo_Class() {
        return Memo_Class;
    }

    /**
     * Memo_Class
     *
     * @param Memo_Class to set
     */
    public void setMemo_Class(String Memo_Class) {
        this.Memo_Class = Memo_Class;
    }

    /**
     * Patient_Memo
     *
     * @return Patient_Memo
     */
    public String getPatient_Memo() {
        return Patient_Memo;
    }

    /**
     * Patient_Memo
     *
     * @param Patient_Memo to set
     */
    public void setPatient_Memo(String Patient_Memo) {
        this.Patient_Memo = Patient_Memo;
    }

    /**
     * 診療科コード (例: 01)
     */
    private String Department_Code;

    /**
     * メモ区分 (例: 1)
     */
    private String Memo_Class;

    /**
     * メモ内容 (例: テストメモ)
     */
    private String Patient_Memo;
}