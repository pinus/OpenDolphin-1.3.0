package open.dolphin.helper;

import open.dolphin.ui.PNSBorderFactory;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * GridBagLayout の JPanel を作る.
 *
 * @author Kazushi Minagawa Digital Globe, Inc.
 */
public class GridBagBuilder {

    private static final int CMP_HGAP = 7;
    private static final int CMP_VGAP = 7;
    private static final int TITLE_SPACE_TOP = 0;
    private static final int TITLE_SPACE_LEFT = 0;
    private static final int TITLE_SPACE_BOTTOM = 10;
    private static final int TITLE_SPACE_RIGHT = 0;

    private JPanel container;
    private GridBagConstraints c;
    private JPanel product;
    private int cmpSpaceH = CMP_HGAP;
    private int cmpSpaceV = CMP_VGAP;
    private int titleSpaceTop = TITLE_SPACE_TOP;
    private int titleSpaceLeft = TITLE_SPACE_LEFT;
    private int titleSpaceBottom = TITLE_SPACE_BOTTOM;
    private int titleSpaceRight = TITLE_SPACE_RIGHT;

    /**
     * GridBagLayout の JPanel を作る.
     */
    public GridBagBuilder() {
        container = new JPanel(new GridBagLayout());
        c = new GridBagConstraints();
        product = container;
    }

    /**
     * GridBagLayout の JPanel を BorderLayout の CENTER に入れた
     * TitledBorder を付けた JPanel を返す.
     *
     * @param title
     */
    public GridBagBuilder(String title) {
        this();
        setTitle(title);
    }

    public JPanel getProduct() {
        return product;
    }

    /**
     * タイトルボーダを設定する.
     */
    private void setTitle(String title) {

        if (title != null) {
            product = new JPanel(new BorderLayout());
            product.setBorder(PNSBorderFactory.createTitledBorder(title));

            container.setBorder(BorderFactory.createEmptyBorder(
                    getTitleSpaceTop(),
                    getTitleSpaceLeft(),
                    getTitleSpaceBottom(),
                    getTitleSpaceRight()));

            product.add(container, BorderLayout.CENTER);
        }
    }

    /**
     * 座標(x, y) anchor の位置に長さ 1 のコンポーネントを追加する.
     *
     * @param c
     * @param x
     * @param y
     * @param anchor
     */
    public void add(Component c, int x, int y, int anchor) {
        add(c, x, y, 1, 1, anchor);
    }

    /**
     * 座標(x, y) anchor の位置にスパン(width, height)のコンポーネントを追加する.
     *
     * @param cmp
     * @param x
     * @param y
     * @param width
     * @param height
     * @param anchor
     */
    public void add(Component cmp, int x, int y, int width, int height, int anchor) {

        int top = (y == 0) ? 0 : getCmpSpaceV();
        int left = (x == 0) ? 0 : getCmpSpaceH();

        c.gridx = x;
        c.gridy = y;
        c.gridwidth = width;
        c.gridheight = height;
        c.fill = GridBagConstraints.NONE;    // 大きくしない !!!
        c.anchor = anchor;

        // X,Y 方向とも２番目以降の部品は水平方向に 7,
        // 垂直方向に 5 ピクセルの間隔をあける
        if (top != 0 || left != 0) {
            c.insets = new Insets(top, left, 0, 0);  // top left bottom right
        }

        ((GridBagLayout) container.getLayout()).setConstraints(cmp, c);
        container.add(cmp);
    }

    /**
     * 座標(x, y)の位置にスパン１で重み(wx, wy)のコンポーネントを追加する.
     *
     * @param cmp
     * @param x
     * @param y
     * @param fill
     * @param wx
     * @param wy
     */
    public void add(Component cmp, int x, int y, int fill, double wx, double wy) {
        add(cmp, x, y, 1, 1, fill, wx, wy);
    }

    /**
     * 座標(x, y)の位置にスパン(width, height)で重み(wx, wy)のコンポーネントを追加する.
     *
     * @param cmp
     * @param x
     * @param y
     * @param width
     * @param height
     * @param fill
     * @param wx
     * @param wy
     */
    public void add(Component cmp, int x, int y, int width, int height, int fill, double wx, double wy) {

        int top = (y == 0) ? 0 : getCmpSpaceV();
        int left = (x == 0) ? 0 : getCmpSpaceH();

        //GridBagConstraints c = new GridBagConstraints();
        c.gridx = x;
        c.gridy = y;
        c.gridwidth = width;
        c.gridheight = height;
        c.fill = fill;
        c.weightx = wx;
        c.weighty = wy;

        if (top != 0 || left != 0) {
            c.insets = new Insets(top, left, 0, 0);  // top left bottom right
        }

        ((GridBagLayout) container.getLayout()).setConstraints(cmp, c);
        container.add(cmp);
    }

    public void addGlue(int x, int y, int fill) {
        add(new JLabel(""), x, y, 1, 1, fill, 1.0, 1.0);
    }

    public void addHGlue(int x, int y) {
        add(new JLabel(""), x, y, 1, 1, GridBagConstraints.HORIZONTAL, 1.0, 1.0);
    }

    public void addVGlue(int x, int y) {
        add(new JLabel(""), x, y, 1, 1, GridBagConstraints.VERTICAL, 1.0, 1.0);
    }

    public int getCmpSpaceH() {
        return cmpSpaceH;
    }

    public void setCmpSpaceH(int cmpSpaceH) {
        this.cmpSpaceH = cmpSpaceH;
    }

    public int getCmpSpaceV() {
        return cmpSpaceV;
    }

    public void setCmpSpaceV(int cmpSpaceV) {
        this.cmpSpaceV = cmpSpaceV;
    }

    public int getTitleSpaceTop() {
        return titleSpaceTop;
    }

    public void setTitleSpaceTop(int titleSpaceTop) {
        this.titleSpaceTop = titleSpaceTop;
    }

    public int getTitleSpaceLeft() {
        return titleSpaceLeft;
    }

    public void setTitleSpaceLeft(int titleSpaceLeft) {
        this.titleSpaceLeft = titleSpaceLeft;
    }

    public int getTitleSpaceBottom() {
        return titleSpaceBottom;
    }

    public void setTitleSpaceBottom(int titleSpaceBottom) {
        this.titleSpaceBottom = titleSpaceBottom;
    }

    public int getTitleSpaceRight() {
        return titleSpaceRight;
    }

    public void setTitleSpaceRight(int titleSpaceRight) {
        this.titleSpaceRight = titleSpaceRight;
    }

    public void addTextItem(int row, int col, String title, int length, boolean kanji) {
        JLabel l = new JLabel(title, SwingConstants.RIGHT);

        JTextField tf = new JTextField(length);
        tf.setMargin(new Insets(1, 2, 1, 2));

        add(l, col, row, 1, 1, SwingConstants.EAST);
        add(tf, col, row, 1, 1, SwingConstants.WEST);
    }


    /**
     * @param components
     */
    public void layout(List<GridBagComponent> components) {

        components.forEach(gbc -> {

            int x = gbc.getCol();
            int y = gbc.getRow();

            int top = (y == 0) ? 0 : getCmpSpaceV();
            int left = (x == 0) ? 0 : getCmpSpaceH();

            c.gridx = x;
            c.gridy = y;
            c.gridwidth = gbc.getColSpan();
            c.gridheight = gbc.getRowSpan();
            c.anchor = gbc.getAnchor();
            c.fill = gbc.getFill();
            c.weightx = gbc.getColWeight();
            c.weighty = gbc.getRowWeight();

            if (top != 0 || left != 0) {
                c.insets = new Insets(top, left, 0, 0);  // top left bottom right
            }

            ((GridBagLayout) container.getLayout()).setConstraints(gbc.getComponent(), c);
            container.add(gbc.getComponent());
        });
    }
}
