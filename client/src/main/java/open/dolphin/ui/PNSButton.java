package open.dolphin.ui;

import open.dolphin.client.Dolphin;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Default ボタンが青地に白になる JButton.
 * com.apple.laf.AquaButtonUI が extend できなくなったので作った.
 */
public class PNSButton extends JButton {
    private Window parent;
    private WindowAdapter windowAdapter;
    private boolean parentIsActive = true; // JSheet always returns inactive

    public PNSButton() {
        this(null, null);
    }

    public PNSButton(Icon icon) {
        this(null, icon);
    }

    public PNSButton(String text) {
        this(text, null);
    }

    public PNSButton(Action a) { super(a); }

    public PNSButton(String text, Icon icon) {
        super(text, icon);
    }

    public void addNotify() {
        if (Dolphin.forMac) {
            parent = SwingUtilities.getWindowAncestor(this);
            windowAdapter = new WindowAdapter() {
                @Override
                public void windowActivated(WindowEvent e) {
                    parentIsActive = true;
                    repaint();
                }
                @Override
                public void windowDeactivated(WindowEvent e) {
                    parentIsActive = false;
                    repaint();
                }
            };
            parent.addWindowListener(windowAdapter);

        }
        super.addNotify();
    }

    public void removeNotify() {
        if (Dolphin.forMac) {
            parent.removeWindowListener(windowAdapter);
        }
        super.removeNotify();
    }

    public void paint(Graphics g) {
        if (Dolphin.forMac) {
            if (model.isEnabled()) {
                setForeground(parentIsActive && isDefaultButton() && !model.isPressed()? Color.WHITE : Color.BLACK);
            }
        }
        super.paint(g);
    }
}