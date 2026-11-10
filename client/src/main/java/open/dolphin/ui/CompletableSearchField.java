package open.dolphin.ui;

import open.dolphin.client.GUIConst;
import open.dolphin.laf.MySearchFieldUI;

/**
 * CompletableSearchField.
 * 未入力の Text Field に，半透明の虫眼鏡アイコンと Label を表示する.
 * 入力が始まるとアイコンと Label は消える.
 *
 * @author pns
 */
public class CompletableSearchField extends CompletableJTextFieldWithLabel {
    public CompletableSearchField(int col) {
        super(col);
        setUI(new MySearchFieldUI());
        setIcon(GUIConst.ICON_SEARCH_16);
    }
}
