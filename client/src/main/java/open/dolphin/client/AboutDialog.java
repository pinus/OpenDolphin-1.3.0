package open.dolphin.client;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import open.dolphin.ui.MoreInfoPanel;


/**
 * About dialog
 * modified by pns
 *
 * @author  Kazushi Minagawa, Digital Globe, Inc.
 */
public class AboutDialog extends JDialog {
    private static final long serialVersionUID = 1L;

    /** Creates new AboutDialog */
    public AboutDialog(Frame f, String title, String imageFile) {

        super(f, title, true);

        //ラベル作成
        final JLabel imageLabel = new JLabel();
        final Icon icon1 = GUIConst.ICON_SPLASH_DOLPHIN;
        final Icon icon2 = GUIConst.ICON_SPLASH_USAGI;
        imageLabel.setIcon(icon1);
        imageLabel.addMouseListener(new MouseAdapter() {
            boolean flg = true;
            @Override
            public void mouseClicked(MouseEvent e) {
                if (flg) {
                    imageLabel.setIcon(icon2);
                    flg = false;
                } else {
                    imageLabel.setIcon(icon1);
                    flg = true;
                }
            }
        });

        // version 文字列作成
        StringBuilder buf = new StringBuilder();
        buf.append("<html>");
        buf.append(ClientContext.getString("productString"));
        buf.append("  Ver.");
        buf.append(ClientContext.getString("version"));
        buf.append("</html>");
        String version = buf.toString();

        // copyright 文字列作成
        String[] copyrightList = ClientContext.getStringArray("copyrightString");
        buf = new StringBuilder();
        buf.append("<html>");
        for (int i=0; i<=4; i++) {
            buf.append((i==0)? "": "<br>");
            buf.append(copyrightList[i]);
        }
        buf.append("</html>");
        String copyright = buf.toString();

        JPanel textPanel = new JPanel(new BorderLayout());
        textPanel.add(getTextLabel(version, new Font(Font.DIALOG, Font.BOLD, 16)), BorderLayout.NORTH);
        textPanel.add(getTextLabel(copyright, new Font(Font.DIALOG, Font.PLAIN, 12)), BorderLayout.CENTER);

        // 閉じるボタン作成
        JButton closeButton = new JButton("閉じる");
        this.getRootPane().setDefaultButton(closeButton);
        closeButton.setSelected(true);
        closeButton.setFocusPainted(false);
        closeButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                close();
            }
        });
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(closeButton);

        // messagePanel 作成
        JPanel messagePanel = new JPanel(new BorderLayout());
        messagePanel.add(imageLabel, BorderLayout.NORTH);
        messagePanel.add(textPanel, BorderLayout.CENTER);
        messagePanel.add(buttonPanel, BorderLayout.SOUTH);

        JPanel content = new MoreInfoPanel(messagePanel, getMoreInfoPane(), "More info...");

        content.setOpaque(true);

        this.setContentPane(content);
        this.pack();
        Point loc = GUIFactory.getCenterLoc(this.getWidth(), this.getHeight());
        this.setLocation(loc);
        this.setResizable(false);
        this.setVisible(true);
    }

    private void close() {
        this.setVisible(false);
        this.dispose();
    }

    private JScrollPane getMoreInfoPane() {
        JTextArea area =
            new JTextArea(
                "This product also contains copyrighted materials as follows: "+
                "OpenDolphin 1.3-2.2 Copyright (C) Digital Globe Inc.,  " +
                "OpenDolphin 1.4m-2.3m Copyright (C) Masuda Naika Clinic,  " +
                "Fugue Icons 2.4.2 Copyright (C) Yusuke Kamiyamane, " +
                "Aesthetica Icons 1.12 (http://dryicons.com), " +
                "Icons from Tango Desktop Project" +
                "", 5, 20);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        JScrollPane scroller =
                new JScrollPane(area,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        return scroller;
    }

    private JLabel getTextLabel(String text, Font font) {
        JLabel label = new JLabel();
        label.setText(text);
        label.setFont(font);
        label.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        return label;
    }
}
