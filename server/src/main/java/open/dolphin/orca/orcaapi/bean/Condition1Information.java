package open.dolphin.orca.orcaapi.bean;

/**
 * Condition1_Information. 患者状態コメント情報１(繰り返し100)
 * @author pns
 */
public class Condition1Information {
    /**
     * 患者状態コメント区分１ (例:  )
     */
    private String Condition1;

    /**
     * 状態内容１ (例:  )
     */
    private String Condition1_Name;

    /**
     * 比喩単語１ (例:  )
     */
    private String Condition1_Word;

    /**
     * アイコンファイル名１ (例:  )
     */
    private String Condition1_Icon;

    /**
     * Condition1
     *
     * @return Condition1
     */
    public String getCondition1() {
        return Condition1;
    }

    /**
     * Condition1
     *
     * @param Condition1 to set
     */
    public void setCondition1(String Condition1) {
        this.Condition1 = Condition1;
    }

    /**
     * Condition1_Name
     *
     * @return Condition1_Name
     */
    public String getCondition1_Name() {
        return Condition1_Name;
    }

    /**
     * Condition1_Name
     *
     * @param Condition1_Name to set
     */
    public void setCondition1_Name(String Condition1_Name) {
        this.Condition1_Name = Condition1_Name;
    }

    /**
     * Condition1_Word
     *
     * @return Condition1_Word
     */
    public String getCondition1_Word() {
        return Condition1_Word;
    }

    /**
     * Condition1_Word
     *
     * @param Condition1_Word to set
     */
    public void setCondition1_Word(String Condition1_Word) {
        this.Condition1_Word = Condition1_Word;
    }

    /**
     * Condition1_Icon
     *
     * @return Condition1_Icon
     */
    public String getCondition1_Icon() {
        return Condition1_Icon;
    }

    /**
     * Condition1_Icon
     *
     * @param Condition1_Icon to set
     */
    public void setCondition1_Icon(String Condition1_Icon) {
        this.Condition1_Icon = Condition1_Icon;
    }
}
