package open.dolphin.stampbox;

import open.dolphin.infomodel.ModuleInfoBean;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

/**
 * スタンプツリーのモデルクラス.
 *
 * @author Kazushi Minagawa, Digital Globe, Inc.
 */
public class StampTreeModel extends DefaultTreeModel {
    private static final long serialVersionUID = -2227174337081687786L;

    /**
     * デフォルトコンストラクタ.
     *
     * @param node
     */
    public StampTreeModel(TreeNode node) {
        super(node);
    }

    /**
     * ノード名の変更をインターセプトして処理する.
     *
     * @param path
     * @param newValue
     */
    @Override
    public void valueForPathChanged(TreePath path, Object newValue) {

        // 変更ノードを取得する
        StampTreeNode node = (StampTreeNode) path.getLastPathComponent();

        // Debug
        //String oldString = node.toString ();
        String newString = (String) newValue;
        //System.out.println (oldString + " -> " + newString);

        /**
         * 葉ノードの場合は StampInfo の name を変更する.
         * そうでない場合は新しい文字列を userObject に設定する.
         */
        if (node.isLeaf()) {
            ModuleInfoBean info = (ModuleInfoBean) node.getUserObject();
            info.setStampName(newString);

        } else {
            node.setUserObject(newString);
        }

        // リスナへ通知する
        nodeChanged(node);
    }
}
