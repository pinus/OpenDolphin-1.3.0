package open.dolphin.ui;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.TexturePaint;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;
import javax.swing.border.AbstractBorder;

/**
 * 角丸のタイトルボーダー
 * @author pns
 */
public class MyRoundedTitleBorder extends AbstractBorder {

    private ImageIcon image;
    private Insets insets;

    public MyRoundedTitleBorder(ImageIcon image, Insets insets) {
        this.image = image;
        this.insets = insets;
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        Graphics2D g2d = (Graphics2D) g.create();

        BufferedImage buf = MyBorderFactory.imageToBufferedImage(image);
        TexturePaint paint = new TexturePaint(buf, new Rectangle2D.Double(0, 0, buf.getWidth(), buf.getHeight()));
        g2d.setPaint(paint);
        g2d.fillRoundRect(x, y, width-1, height-1, 10, 10);

        g2d.setColor(Color.LIGHT_GRAY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.drawRoundRect(x, y, width-1, height-1, 10, 10);
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
        g2d.drawRoundRect(x+1, y+1, width-1, height-1, 10, 10);
   }

    @Override
    public Insets getBorderInsets(Component c){
        return insets;
    }

    @Override
    public boolean isBorderOpaque(){
        return false;
    }
}
