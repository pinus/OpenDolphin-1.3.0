package open.dolphin.orca.orcaapi.bean;

/**
 * Medicationgetreq.
 *
 * @author pns
 */
public class Medicationgetreq {
    /**
     * リクエスト番号 (例: 01)
     */
    private String Request_Number;

    /**
     * リクエストコード (例: 114030710)
     */
    private String Request_Code;

    /**
     * リクエストコード (例: 基準日)
     */
    private String Base_Date;

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
}
