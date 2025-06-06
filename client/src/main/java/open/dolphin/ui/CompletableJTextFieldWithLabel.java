package open.dolphin.ui;

import open.dolphin.client.Dolphin;
import open.dolphin.client.GUIConst;
import open.dolphin.helper.ImageHelper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;

/**
 * CompletableJTextFieldWithLabel.
 * 未入力の Text Field に，半透明のアイコンと Label を表示する.
 * 入力が始まるとアイコンと Label は消える.
 *
 * @author pns
 */
public class CompletableJTextFieldWithLabel extends CompletableJTextField {
    private static final int ICON_TEXT_GAP = 5;

    private BufferedImage clearButton;
    private String label;
    private BufferedImage icon;
    private Font font;

    public CompletableJTextFieldWithLabel(int col) {
        super(col);
        init();
    }

    private void init() {
        putClientProperty("Quaqua.TextField.style", "search");
        clearButton = ImageHelper.imageToBufferedImage(GUIConst.ICON_CROSS_16);
        font = new Font(getFont().getFontName(), Font.PLAIN, 12);

        // Field の右端に X を出して，そこをクリックしたらテキストをクリアする.
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (getWidth() - e.getX() <= 20) {
                    setText("");
                    // リターンキー入力されたことにする
                    fireActionPerformed();
                }
            }
        });

        // 右端の X のところでカーソルの形を変える
        addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                if (getText() != null && !getText().isEmpty() && getWidth() - e.getX() <= 20) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                } else {
                    setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
                }
            }
        });
    }

    /**
     * 未入力の Text Field に表示する文字列を設定.
     *
     * @param s 表示する文字列
     */
    public void setLabel(String s) {
        label = s;
        repaint();
    }

    /**
     * 未入力の Text Field に表示するアイコンを設定.
     *
     * @param icon ImageIcon
     */
    public void setIcon(ImageIcon icon) {
        this.icon = ImageHelper.imageToBufferedImage(icon);
    }

    /**
     * アイコンと Label 文字列を表示する.
     *
     * @param graphics Graphics
     */
    @Override
    public void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics;
        if (Dolphin.forMac) {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f));

        int verticalCentering = (getHeight() - icon.getHeight()) / 2;

        if (getText() == null || getText().isEmpty()) {
            // 虫眼鏡とラベル
            FontMetrics fm = g.getFontMetrics();
            int iconWidth = icon.getWidth();
            int labelWidth = iconWidth + fm.stringWidth(label) + ICON_TEXT_GAP;

            int x = (getWidth() - labelWidth) / 2;

            g.setFont(font);

            g.drawImage(icon, null, x - ICON_TEXT_GAP, verticalCentering);
            g.drawString(label, x + iconWidth, verticalCentering + fm.getAscent()); // height でセンタリングして ascent 分下げる

        } else {
            // 右端のクリアボタン（X マーク）
            g.drawImage(clearButton, null, getWidth() - 22, verticalCentering);
        }
    }

    public static void main(String[] arg) {
        open.dolphin.client.ClientContext.setClientContextStub(new open.dolphin.client.ClientContextStub());
        JFrame f = new JFrame();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        CompletableJTextFieldWithLabel field = new CompletableJTextFieldWithLabel(15);
        field.setLabel("病名検索");
        field.setIcon(GUIConst.ICON_SEARCH_16);
        f.add(field);
        f.pack();
        f.setVisible(true);
    }
}
