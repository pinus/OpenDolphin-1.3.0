package open.dolphin.orca.orcaapi.bean;

/**
 * Selection_Expression_Information. 選択式コメントリスト(繰り返し　２００)
 *
 * @author pns
 */
public class SelectionExpressionInformation {
    /**
     * コメントコード (例:  )
     */
    private String Comment_Code;

    /**
     * コメントコード名称 (例:  )
     */
    private String Comment_Name;

    /**
     * 項番 (例:  )
     */
    private String Item_Number;

    /**
     * 枝番 (例:  )
     */
    private String Item_Number_Branch;

    /**
     * 区分 (例:  )
     */
    private String Category;

    /**
     * 条件区分 (例:  )
     */
    private String Condition_Category;

    /**
     * 非算定理由コメント (例:  )
     */
    private String Not_Use_Comment;

    /**
     * 処理区分 (例:  )
     */
    private String Process_Category;

    /**
     * 診療行為名称等 (例:  )
     */
    private String Selection_Grep_Name;


    /**
     * Comment_Code
     *
     * @return Comment_Code
     */
    public String getComment_Code() {
        return Comment_Code;
    }

    /**
     * Comment_Code
     *
     * @param Comment_Code to set
     */
    public void setComment_Code(String Comment_Code) {
        this.Comment_Code = Comment_Code;
    }

    /**
     * Comment_Name
     *
     * @return Comment_Name
     */
    public String getComment_Name() {
        return Comment_Name;
    }

    /**
     * Comment_Name
     *
     * @param Comment_Name to set
     */
    public void setComment_Name(String Comment_Name) {
        this.Comment_Name = Comment_Name;
    }

    /**
     * Item_Number
     *
     * @return Item_Number
     */
    public String getItem_Number() {
        return Item_Number;
    }

    /**
     * Item_Number
     *
     * @param Item_Number to set
     */
    public void setItem_Number(String Item_Number) {
        this.Item_Number = Item_Number;
    }

    /**
     * Item_Number_Branch
     *
     * @return Item_Number_Branch
     */
    public String getItem_Number_Branch() {
        return Item_Number_Branch;
    }

    /**
     * Item_Number_Branch
     *
     * @param Item_Number_Branch to set
     */
    public void setItem_Number_Branch(String Item_Number_Branch) {
        this.Item_Number_Branch = Item_Number_Branch;
    }

    /**
     * Category
     *
     * @return Category
     */
    public String getCategory() {
        return Category;
    }

    /**
     * Category
     *
     * @param Category to set
     */
    public void setCategory(String Category) {
        this.Category = Category;
    }

    /**
     * Condition_Category
     *
     * @return Condition_Category
     */
    public String getCondition_Category() {
        return Condition_Category;
    }

    /**
     * Condition_Category
     *
     * @param Condition_Category to set
     */
    public void setCondition_Category(String Condition_Category) {
        this.Condition_Category = Condition_Category;
    }

    /**
     * Not_Use_Comment
     *
     * @return Not_Use_Comment
     */
    public String getNot_Use_Comment() {
        return Not_Use_Comment;
    }

    /**
     * Not_Use_Comment
     *
     * @param Not_Use_Comment to set
     */
    public void setNot_Use_Comment(String Not_Use_Comment) {
        this.Not_Use_Comment = Not_Use_Comment;
    }

    /**
     * Process_Category
     *
     * @return Process_Category
     */
    public String getProcess_Category() {
        return Process_Category;
    }

    /**
     * Process_Category
     *
     * @param Process_Category to set
     */
    public void setProcess_Category(String Process_Category) {
        this.Process_Category = Process_Category;
    }

    /**
     * Selection_Grep_Name
     *
     * @return Selection_Grep_Name
     */
    public String getSelection_Grep_Name() {
        return Selection_Grep_Name;
    }

    /**
     * Selection_Grep_Name
     *
     * @param Selection_Grep_Name to set
     */
    public void setSelection_Grep_Name(String Selection_Grep_Name) {
        this.Selection_Grep_Name = Selection_Grep_Name;
    }
}
