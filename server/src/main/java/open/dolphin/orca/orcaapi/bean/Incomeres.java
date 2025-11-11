package open.dolphin.orca.orcaapi.bean;

/**
 * Incomeres.
 * @author pns
 */
public class Incomeres {
    /**
     * 実施日 (例: 2022-12-26)
     */
    private String Information_Date;

    /**
     * 実施時間 (例: 10:00:00)
     */
    private String Information_Time;

    /**
     * 結果コード(ゼロ以外エラー) (例: 00)
     */
    private String Api_Result;

    /**
     * エラーメッセージ (例: 処理終了)
     */
    private String Api_Result_Message;

    /**
     *   (例: PatientInfo)
     */
    private String Reskey;

    /**
     * 基準日 (例: 2022-12-26)
     */
    private String Base_Date;

    /**
     * 入金方法情報(繰り返し100) (例:  )
     */
    private IncomeInformation[] income_Information;

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
     * income_Information
     *
     * @return income_Information
     */
    public IncomeInformation[] getIncome_Information() {
        return income_Information;
    }

    /**
     * income_Information
     *
     * @param income_Information to set
     */
    public void setIncome_Information(IncomeInformation[] income_Information) {
        this.income_Information = income_Information;
    }
}