package open.dolphin.ui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.image.BufferedImage;
import javax.swing.JLabel;
import javax.swing.TransferHandler;

/**
 * DragImage の utility 付きの TransferHandler.
 * @author pns
 */
public class PNSTransferHandler extends TransferHandler {
    private static final long serialVersionUID = 1L;

    /**
     * JLabel から DragImage を作る.
     * @param label
     */
    protected void setDragImage(JLabel label) {
        setDragImage(label, false);
    }

    /**
     * JLabel から DragImage を作る.
     * @param label
     * @param clip true の場合，文字幅に合わせて clipping する
     */
    protected void setDragImage(JLabel label, boolean clip) {
        int width = label.getWidth();
        int height = label.getHeight();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics g = image.getGraphics();

        // background
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);

        label.paint(g);

        if (clip) {
            // 文字列の長さに応じて幅を調節する
            int stringWidth = g.getFontMetrics().stringWidth(label.getText());
            if (stringWidth + 8 < width) { // 8ドット余裕
                width = stringWidth + 8;
                image = image.getSubimage(0, 0, width, height);
            }
        }

        // グレーの枠を付ける
        if (label.getBorder() == null) {
            g.setColor(Color.gray);
            g.drawRect(0, 0, width-1, height-1);
        }

        setDragImage(image);
    }

    /**
     * DragImageOffset を設定してから setDragImage する.
     * @param image
     */
    @Override
    public void setDragImage(Image image) {
        setDragImageOffset(new Point(-image.getWidth(null)/2, -image.getHeight(null)/2));
        super.setDragImage(image);
    }
}
