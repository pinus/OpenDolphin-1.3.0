package open.dolphin.orca.orcaapi.bean;

/**
 * Medicationgetres.
 *
 * @author pns
 */
public class Medicationgetres {
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
     * リクエストコード (例: 01)
     */
    private String Request_Code;

    /**
     * 基準日 (例: 2024-11-26)
     */
    private String Base_Date;

    /**
     * 診療行為コード情報 (例: )
     */
    private MedicationInformation Medication_Information;

    /**
     * Selection_Expression_Information
     *
     * @return Selection_Expression_Information
     */
    public SelectionExpressionInformation[] getSelection_Expression_Information() {
        return Selection_Expression_Information;
    }

    /**
     * Selection_Expression_Information
     *
     * @param Selection_Expression_Information to set
     */
    public void setSelection_Expression_Information(SelectionExpressionInformation[] Selection_Expression_Information) {
        this.Selection_Expression_Information = Selection_Expression_Information;
    }

    /**
     * Medication_Information
     *
     * @return Medication_Information
     */
    public MedicationInformation getMedication_Information() {
        return Medication_Information;
    }

    /**
     * Medication_Information
     *
     * @param Medication_Information to set
     */
    public void setMedication_Information(MedicationInformation Medication_Information) {
        this.Medication_Information = Medication_Information;
    }

    /**
     * Base_Date
     *
     * @return Base_Date
     */
    public String getBase_Date() {
        return Base_Date;
    }

    /**
     * Base_Date
     *
     * @param Base_Date to set
     */
    public void setBase_Date(String Base_Date) {
        this.Base_Date = Base_Date;
    }

    /**
     * Request_Code
     *
     * @return Request_Code
     */
    public String getRequest_Code() {
        return Request_Code;
    }

    /**
     * Request_Code
     *
     * @param Request_Code to set
     */
    public void setRequest_Code(String Request_Code) {
        this.Request_Code = Request_Code;
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
     * 選択式コメントリスト(繰り返し　２００) (例:  )
     */
    private SelectionExpressionInformation[] Selection_Expression_Information;

}