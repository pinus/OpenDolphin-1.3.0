package open.dolphin.client;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JPanel;

/**
 * SeparatorPanel
 *
 * Based on Java Swing Hacks.
 */
public class SeparatorPanel extends JPanel {

    private static final long serialVersionUID = 335333326611592227L;
    private static final Color DEFAULT_LEFT_COLOR = Color.WHITE;
    private static final Color DEFAULT_RIGHT_COLOR = Color.LIGHT_GRAY;
    private Color leftColor = DEFAULT_LEFT_COLOR;
    private Color rightColor = DEFAULT_RIGHT_COLOR;

    public SeparatorPanel() {
        setOpaque(false);
        this.setPreferredSize(new Dimension(3, 12));
    }

    public SeparatorPanel(Color leftColor, Color rightColor) {
        this();
        this.leftColor = leftColor;
        this.rightColor = rightColor;
    }

    @Override
    public void paintComponent(Graphics graphis) {
        Graphics g = graphis.create();
        g.setColor(leftColor);
        g.drawLine(0, 0, 0, getHeight());
        g.setColor(rightColor);
        g.drawLine(1, 0, 1, getHeight());
        g.dispose();
    }
}
