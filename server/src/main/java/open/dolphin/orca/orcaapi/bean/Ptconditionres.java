package open.dolphin.orca.orcaapi.bean;

/**
 * Ptconditionres.
 * @author pns
 */
public class Ptconditionres {
    /**
     * 実施日 (例: 2024-12-23)
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
     * 基準日 (例: 2024-12-23)
     */
    private String Base_Date;

    /**
     * 患者状態コメント情報１(繰り返し100) (例:  )
     */
    private Condition1Information[] Condition1_Information;

    /**
     * 患者状態コメント情報２(繰り返し100) (例:  )
     */
    private Condition2Information[] Condition2_Information;

    /**
     * 患者状態コメント情報３(繰り返し100) (例:  )
     */
    private Condition3Information[] Condition3_Information;

}
