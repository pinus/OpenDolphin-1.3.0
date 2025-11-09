package open.dolphin.orca.orcaapi.bean;

/**
 * Patientmemomodv2res.
 *
 * @author pns
 */
public class Patientmemomodv2res {
    /**
     * 実施日 (例: 2021-11-12)
     */
    private String Information_Date;

    /**
     * 実施時間 (例: 13:36:47)
     */
    private String Information_Time;

    /**
     * 結果コード (例: 000)
     */
    private String Api_Result;

    /**
     * 処理メッセージ (例:  )
     */
    private String Api_Result_Message;

    /**
     *   (例: PatientInfo)
     */
    private String Reskey;

    /**
     * 患者情報 (例: )
     */
    private PatientInformation Patient_Information;

    /**
     * メモ情報 (例: )
     */
    private PatientMemoInformation Patient_Memo_Information;

    /**
     * Information_Date
     *
     * @return Information_Date
     */
    public String getInformation_Date() {
        return Information_Date;
    }

    /**
     * Information_Date
     *
     * @param Information_Date to set
     */
    public void setInformation_Date(String Information_Date) {
        this.Information_Date = Information_Date;
    }

    /**
     * Information_Time
     *
     * @return Information_Time
     */
    public String getInformation_Time() {
        return Information_Time;
    }

    /**
     * Information_Time
     *
     * @param Information_Time to set
     */
    public void setInformation_Time(String Information_Time) {
        this.Information_Time = Information_Time;
    }

    /**
     * Api_Result
     *
     * @return Api_Result
     */
    public String getApi_Result() {
        return Api_Result;
    }

    /**
     * Api_Result
     *
     * @param Api_Result to set
     */
    public void setApi_Result(String Api_Result) {
        this.Api_Result = Api_Result;
    }

    /**
     * Api_Result_Message
     *
     * @return Api_Result_Message
     */
    public String getApi_Result_Message() {
        return Api_Result_Message;
    }

    /**
     * Api_Result_Message
     *
     * @param Api_Result_Message to set
     */
    public void setApi_Result_Message(String Api_Result_Message) {
        this.Api_Result_Message = Api_Result_Message;
    }

    /**
     * Reskey
     *
     * @return Reskey
     */
    public String getReskey() {
        return Reskey;
    }

    /**
     * Reskey
     *
     * @param Reskey to set
     */
    public void setReskey(String Reskey) {
        this.Reskey = Reskey;
    }

    /**
     * Patient_Information
     *
     * @return Patient_Information
     */
    public PatientInformation getPatient_Information() {
        return Patient_Information;
    }

    /**
     * Patient_Information
     *
     * @param Patient_Information to set
     */
    public void setPatient_Information(PatientInformation Patient_Information) {
        this.Patient_Information = Patient_Information;
    }

    /**
     * Patient_Memo_Information
     *
     * @return Patient_Memo_Information
     */
    public PatientMemoInformation getPatient_Memo_Information() {
        return Patient_Memo_Information;
    }

    /**
     * Patient_Memo_Information
     *
     * @param Patient_Memo_Information to set
     */
    public void setPatient_Memo_Information(PatientMemoInformation Patient_Memo_Information) {
        this.Patient_Memo_Information = Patient_Memo_Information;
    }
}
