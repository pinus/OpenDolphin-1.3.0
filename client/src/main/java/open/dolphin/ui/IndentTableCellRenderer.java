package open.dolphin.ui;

import open.dolphin.helper.GUIFactory;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * インデントを付けるレンダラ.
 * 頭にスペースをつける偽インデント.
 *
 * @author pns
 */
public class IndentTableCellRenderer extends DefaultTableCellRenderer {
    // pixels to indent
    public static final int NARROW = 5;
    public static final int WIDE = 10;
    public static final Font NORMAL_FONT = GUIFactory.getFont(12);
    public static final Font SMALL_FONT = GUIFactory.getFont(9);
    final private int indent;
    final private Font font;

    public IndentTableCellRenderer() {
        this(WIDE);
    }

    public IndentTableCellRenderer(int indent) {
        this(indent, NORMAL_FONT);
    }

    public IndentTableCellRenderer(int indent, Font font) {
        super();
        this.indent = indent;
        this.font = font;
    }

    /**
     * にせインデント.
     *
     * @param text   インデントを付けるテキスト
     * @param indent インデント量
     * @return インデントを付けたテキスト
     */
    public static String addIndent(String text, int indent) {
        if (indent >= 10) {
            return "　" + text;
        } else {
            return " " + text;
        }
    }

    @Override
    public Component getTableCellRendererComponent(JTable table,
                                                   Object value,
                                                   boolean isSelected,
                                                   boolean isFocused,
                                                   int row, int col) {

        if (isSelected) {
            Color fore;
            Color back;
            if (table.isFocusOwner()) {
                fore = table.getSelectionForeground();
                back = table.getSelectionBackground();
            } else {
                fore = table.getForeground();
                back = (Color) table.getClientProperty("JTable.backgroundOffFocus");
            }
            setForeground(fore);
            setBackground(back);

        } else {
            setForeground(table.getForeground());
            setBackground(table.getBackground());
        }

        if (value == null) {
            this.setText("");
        } else {
            this.setText(addIndent(value.toString(), indent));
        }
        this.setFont(font);

        return this;
    }

    /**
     * Show horizontal grid (Retina 対応)
     *
     * @param g Graphics
     */
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        g.setColor(Color.WHITE);
        g.drawLine(0, getHeight(), getWidth(), getHeight());
    }
}
