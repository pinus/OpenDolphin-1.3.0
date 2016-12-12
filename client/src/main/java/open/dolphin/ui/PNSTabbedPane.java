package open.dolphin.ui;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultSingleSelectionModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.JToggleButton;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.tree.DefaultMutableTreeNode;
import open.dolphin.client.ClientContext;
import open.dolphin.client.ClientContextStub;

/**
 * JTabbedPane 的な何か
 * @author pns
 */
public class PNSTabbedPane extends JPanel implements ChangeListener {
    private static final long serialVersionUID = 1L;
    /** タブ切り替えボタン格納パネル */
    private ButtonPanel buttonPanel;
    /** 切り替えボタンを入れておくリスト */
    private ArrayList<TabButton> buttonList;
    /** 切り替えボタン格納パネルのレイアウト */
    private RightJustifiedFlowLayout buttonLayout;
    /** コンテント表示パネル */
    private JPanel contentPanel;
    /** コンテントを切り替えるためのカードレイアウト */
    private CardLayout card;
    /** タブ切り替えボタンのボタングループ */
    private ButtonGroup buttonGroup;
    /** セレクションモデル */
    private DefaultSingleSelectionModel selectionModel;
    /** タブの総数 */
    private int tabCount = 0;
    /** １行の最低タブ数 */
    private static final int MIN_TAB_PER_LINE = 3;
    /** タブの場所　上か下か */
    private int tabPlacement = JTabbedPane.TOP;
    /** ChangeListener */
    private ChangeListener listener = null;
    /** 親の Window */
    private Window parent = null;

    public PNSTabbedPane() {
        initComponents();
    }

    /**
     * 組み込まれるときに addNotify が呼ばれるのを利用して parent を登録する
     */
     @Override
    public void addNotify() {
        super.addNotify();

        if (parent == null) {
            parent = SwingUtilities.windowForComponent(this);
        }
    }

    private void initComponents() {
        // selection model 作成
        selectionModel = new DefaultSingleSelectionModel();
        selectionModel.addChangeListener(this);

        // ボタン格納パネル作成
        buttonPanel = new ButtonPanel();
        buttonLayout = new RightJustifiedFlowLayout();
        buttonPanel.setLayout(buttonLayout);

        // 内容表示パネル作成
        contentPanel = new JPanel();
        card = new CardLayout(0,0);
        contentPanel.setLayout(card);

        // ボタングループの設定
        buttonGroup = new ButtonGroup();
        buttonList = new ArrayList<>();

        // 全体のレイアウト
        setLayout(new BorderLayout(0,0));
        add(buttonPanel, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);
    }

    /**
     * タブのボタンを格納した JPanel を返す
     * @return
     */
    public ButtonPanel getButtonPanel() {
        return buttonPanel;
    }

    /**
     * タブにコンポネントを加える
     * @param name
     * @param c
     */
    public void addTab(String name, Component c) {
        TabButton button = new TabButton(name, tabCount);

        buttonPanel.add(button);
        contentPanel.add(c, String.valueOf(tabCount));
        buttonGroup.add(button);
        buttonList.add(button);

        if (tabCount == 0) selectionModel.setSelectedIndex(0);
        tabCount++;
    }

    /**
     * index の card に component をセットする
     * @param index
     * @param c
     */
    public void setComponentAt(int index, Component c) {
        contentPanel.remove(index);
        contentPanel.add(c, String.valueOf(index), index);

        // もし selected だったら，その component を表示し直す
        if (index == selectionModel.getSelectedIndex()) {
            card.show(contentPanel, String.valueOf(index));
        }
    }

    /**
     * index の component を返す
     * @param index
     * @return
     */
    public Component getComponentAt(int index) {
        return contentPanel.getComponent(index);
    }

    public String getTitleAt(int index) {
        TabButton button = (TabButton) buttonPanel.getComponent(index);
        return button.getName();
    }

    /**
     * ボタンパネルを上につけるか，下につけるか
     * @param tabPlacement
     */
    public void setTabPlacement(int tabPlacement) {
        if (this.tabPlacement != tabPlacement) {
            this.tabPlacement = tabPlacement;
            removeAll();
            if (tabPlacement == JTabbedPane.BOTTOM) {
                add(contentPanel, BorderLayout.CENTER);
                add(buttonPanel, BorderLayout.SOUTH);
            } else {
                add(buttonPanel, BorderLayout.NORTH);
                add(contentPanel, BorderLayout.CENTER);
            }
            revalidate();
            repaint();
        }
    }

    /**
     * index のボタンに ToolTipText をつける
     * @param index
     * @param text
     */
    public void setToolTipTextAt(int index, String text) {
        buttonList.get(index).setToolTipText(text);
    }

    /**
     * index のタブを選択する
     * @param index
     */
    public void setSelectedIndex(int index) {
        if (buttonList.get(index).isEnabled()) selectionModel.setSelectedIndex(index);
    }

    /**
     * 選択されている index を返す
     * @return
     */
    public int getSelectedIndex() {
        return selectionModel.getSelectedIndex();
    }

    /**
     * タブの総数を返す
     * @return
     */
    public int getTabCount() {
        return tabCount;
    }

    /**
     * 表示されている component を返す
     * @return
     */
    public Component getSelectedComponent() {
        return contentPanel.getComponent(selectionModel.getSelectedIndex());
    }

    /**
     * ChangeListener を登録する
     * @param listener
     */
    public void addChangeListener(ChangeListener listener) {
        this.listener = listener;
    }

    /**
     * index のタブが使用可能であるかどうかを設定
     * @param index
     * @param isEnabled
     */
    public void setEnabledAt(int index, boolean isEnabled) {
        buttonList.get(index).setEnabled(isEnabled);
    }

    /**
     * text をタイトルに持つタブ番号を返す。ない場合は -1 を返す
     * @param title
     * @return
     */
    public int indexOfTab(String title) {
        int ret = -1;
        for(int i=0; i< tabCount; i++) {
            if (buttonList.get(i).getText().equals(title)) {
                ret = i;
                break;
            }
        }
        return ret;
    }

    /**
     * index のタブを削除する
     * @param index
     */
    public void removeTabAt(int index) {
        buttonGroup.remove(buttonList.get(index));
        buttonList.remove(index);
        buttonPanel.remove(index);
        contentPanel.remove(index);
    }

    /**
     * ボタンレイアウトの水平方向の隙間
     * @param hgap
     */
    public void setButtonHgap(int hgap) {
        buttonLayout.setHgap(hgap);
    }

    /**
     * ボタンレイアウトの垂直方向の隙間
     * @param vgap
     */
    public void setButtonVgap(int vgap) {
        buttonLayout.setVgap(vgap);
    }

    /**
     * selection model が変更されると呼ばれる
     * @param e
     */
    @Override
    public void stateChanged(ChangeEvent e) {
        if (listener != null) listener.stateChanged(e);

        int index = selectionModel.getSelectedIndex();
        card.show(contentPanel, String.valueOf(index));
        buttonList.get(index).setSelected(true);
    }

    /**
     * ボタンパネル
     */
    public class ButtonPanel extends HorizontalPanel {
        private static final long serialVersionUID = 1L;
        private Dimension padding = new Dimension(0,0);

        public ButtonPanel() {
        }

        /**
         * ボタンパネルの回りの余白を設定する
         * @param d
         */
        public void setPadding(Dimension d) {
            padding = d;
        }

        public Dimension getPadding() {
            return padding;
        }
    }

    /**
     * タブボタンクラス
     */
    private class TabButton extends JToggleButton implements ActionListener {
        private static final long serialVersionUID = 1L;
        private final String name;
        private int index;
        public boolean isBottom;
        public boolean isRightEnd;

        // レイアウトマネージャーでボタンの大きさを調節する時使う
        // public Dimension margin = new Dimension(0,-4); // no quaqua
        public Dimension margin = new Dimension(0,-2); // quaqua 8.0
        //public Dimension margin = new Dimension(0,4); // quaqua 7.2

        public TabButton(String name, int index) {
            this.addActionListener(this);
            this.setFocusable(false);
            this.name = name;
            this.setName(name);
            this.setText(name);
            this.index = index;
            this.setBorderPainted(false);
        }
        public void setIndex(int index) {
            this.index = index;
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            selectionModel.setSelectedIndex(index);
        }
        @Override
        public Dimension getPreferredSize() {
            Dimension d = super.getPreferredSize();
            d.setSize(d.width+margin.width, d.height+margin.height);
            return d;
        }
        @Override
        public void paintComponent(Graphics graphics) {
            //super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics.create();
            int w = this.getWidth();
            int h = this.getHeight();

            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // ボタン枠
            if (parent.isFocused()) g.setColor(Color.GRAY);
            else g.setColor(Color.LIGHT_GRAY);

            //g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.2f));
            g.drawLine(0, 0, w-1, 0);
            g.drawLine(0, 0, 0, h-1);
            if (this.isBottom || buttonLayout.getVgap() != 0) g.drawLine(0, h-1, w-1, h-1);
            if (this.isRightEnd || buttonLayout.getHgap() != 0) g.drawLine(w-1, 0, w-1, h-1);

            // ボタン中身
            if (parent.isFocused()) {
                if (this.isSelected()) {
                    g.setColor(Color.BLACK);
                    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
                    for(int y=0; y< h; y++) {
                        int rgb = (int)((float)y / (float)h * 70f + 50f);
                        g.setColor(new Color(rgb,rgb,rgb));
                        g.drawLine(0, y, w, y);
                    }
                } else {
                    g.setColor(Color.WHITE);
                    float endAlpha = 0.4f;
                    if (this.isEnabled()) endAlpha = 0.8f;
                    for(int y=1; y< h-1; y++) {
                        float alpha = (float)y / (float)h * endAlpha;
                        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                        g.drawLine(1, h-y, w-2, h-y);

                    }
                }
            } else {
                if (this.isSelected()) {
                    g.setColor(Color.BLACK);
                    for(int y=0; y< h; y++) {
                        float alpha = (float)y / (float)h * 0.2f + 0.1f;
                        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                        g.drawLine(0, h-y, w, h-y);
                    }
                }
            }

            // 文字記入
            FontMetrics fm = g.getFontMetrics();
            Font font = g.getFont();
            int strWidth = fm.stringWidth(name);

            if (this.isSelected()) {
                // 影
                g.setColor(Color.BLACK);
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
                g.drawString(name, (w-strWidth)/2+1, fm.getHeight()+3);
                // boldに
                g.setFont(new Font(font.getFontName(), Font.BOLD, font.getSize()));
                g.setColor(Color.WHITE);
            } else {
                if (this.isEnabled()) g.setColor(Color.BLACK);
                else g.setColor(Color.GRAY);
            }

            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
            g.drawString(name, (w-strWidth)/2, fm.getHeight()+2);

            g.dispose();
        }
    }

    /**
     * 両端そろえる FlowLayout
     */
    private class RightJustifiedFlowLayout extends FlowLayout {
        private static final long serialVersionUID = 1L;

        public RightJustifiedFlowLayout() {
            super(FlowLayout.CENTER,0,0);
        }

        @Override
        public Dimension preferredLayoutSize(Container buttonPanel) {
            Dimension padding = ((ButtonPanel)buttonPanel).getPadding();
            int width = buttonPanel.getWidth() - padding.width;
            if (width <= 0) return new Dimension(1,1);

            int hgap = this.getHgap();
            int vgap = this.getVgap();
            int buttonCount = buttonPanel.getComponentCount();
            int tempWidth   = 0;
            int tempHeight  = 0;
            int totalHeight = 0;

            int lineCount = 1;

            int maxButtonWidth = 0;
            int tempButtonCount = 0;
            // wrap した場合の各行のボタン数
            List<Integer> buttonCountAtLine = new ArrayList<>(10);

            // 行数を計算
            for(int i=0; i< buttonCount; i++) {
                TabButton button = (TabButton) buttonPanel.getComponent(i);
                button.margin.width = 0;
                Dimension b = button.getPreferredSize();

                tempWidth += hgap;
                // 次のボタンを加えたらはみ出す場合の処理
                if (tempWidth + b.width > width) {
                    totalHeight += tempHeight;
                    tempHeight = 0;
                    tempWidth = 0;
                    buttonCountAtLine.add(tempButtonCount);
                    lineCount++;
                    tempButtonCount = 0;
                }
                tempHeight = Math.max(tempHeight, b.height);
                tempWidth += b.width;

                maxButtonWidth = Math.max(maxButtonWidth, b.width);
                tempButtonCount++;
            }
            totalHeight += tempHeight + (lineCount+1)*vgap;
            buttonCountAtLine.add(tempButtonCount);

            // １行だったら，ボタンの長さをできるだけそろえる
            if (lineCount == 1) {
                if (width >= maxButtonWidth * buttonCount) {
                    for(int i=0; i< buttonCount; i++) {
                        TabButton button = (TabButton) buttonPanel.getComponent(i);
                        button.margin.width = (maxButtonWidth - button.getPreferredSize().width);
                        button.isBottom = true;
                        button.isRightEnd = (i == buttonCount -1);
                    }
                }
            } else {
            // ２行以上だったら right justification する

                if (tempButtonCount < MIN_TAB_PER_LINE) {
                    //　再配分
                    float n = (float)buttonCount / lineCount;
                    float residue = 0;
                    int assigned = 0;

                    for(int i=0; i< lineCount - 1; i++) {
                        residue += (n - (int)n);
                        if (residue >= 1.0f) {
                            residue -= 1.0f;
                            buttonCountAtLine.set(i, (int)n + 1);
                            assigned += ((int)n + 1);
                        } else {
                            buttonCountAtLine.set(i, (int)n);
                            assigned += (int)n;
                        }
                    }
                    buttonCountAtLine.set(lineCount - 1, buttonCount - assigned);
                }

                int offset = 0;
                for(int line=0; line< lineCount; line++) {
                    int bc = buttonCountAtLine.get(line);
                    // 隙間を測る
                    int gap = hgap; // hgap の数はボタンよりも１つ多い
                    for(int i=0; i< bc; i++) {
                        TabButton b = (TabButton) buttonPanel.getComponent(i + offset);
                        gap += (b.getPreferredSize().width + hgap);
                        b.isBottom = (line == lineCount - 1 || vgap != 0);
                        b.isRightEnd = (i == bc - 1 || hgap != 0);
                    }
                    gap = width - gap;
                    // gap に応じて margin 調節
                    int delta = gap / bc;
                    for(int i=0; i< bc; i++) {
                        TabButton b = (TabButton) buttonPanel.getComponent(i + offset);
                        b.margin.width = delta;
                        gap -= delta;
                        if (i == bc - 1) b.margin.width += gap;
                    }

                    offset += buttonCountAtLine.get(line);
                }
            }
            return new Dimension(width, totalHeight + padding.height);
        }
    }

    //================== TEST =================
    public static void main(String[] argv) {
        ClientContext.setClientContextStub(new ClientContextStub());
        //testPattern1();
        //testPattern2();
        testPattern3();
    }

    /**
     * MainWindow Style
     * +---------------+
     * | Tab Panel     |
     * |---------------|
     * | Command Panel |
     * |---------------|
     * |               |
     * | Table         |
     * |               |
     * |---------------|
     * | Status Panel  |
     * +---------------+
     */
    private static void testPattern1() {
        MainFrame f = new MainFrame(false, false);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(600,800);

        JPanel mainComponentPanel_1 = createMainPanel();
        JPanel mainComponentPanel_2 = createMainPanel();
        JPanel mainComponentPanel_3 = createMainPanel();

        final PNSTabbedPane tabPane = new PNSTabbedPane();
        // content の command panel と連続にするために alpha セット
        tabPane.setButtonVgap(4);
        tabPane.addTab("受付リスト", mainComponentPanel_1);
        tabPane.addTab("患者検索", mainComponentPanel_2);
        tabPane.addTab("ラボレシーバ", mainComponentPanel_3);

        MainFrame.MainPanel mainPanel = f.getMainPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(tabPane, BorderLayout.CENTER);

        tabPane.addChangeListener(e -> tabPane.getSelectedComponent());

        f.setVisible(true);
    }

    private static JPanel createMainPanel() {
        JPanel mainComponentPanel = new JPanel();
        mainComponentPanel.setLayout(new BorderLayout(0,0));

        HorizontalPanel commandPanel = new HorizontalPanel();
        commandPanel.setPanelHeight(36);
        commandPanel.add(new JButton("テスト１"));
        commandPanel.addSeparator();
        commandPanel.add(new JButton("テスト２"));
        commandPanel.addGlue();
        commandPanel.add(new JButton("右端"));

        StatusPanel statusPanel = new StatusPanel();
        statusPanel.add("Label1");
        statusPanel.addSeparator();
        statusPanel.add("Label2");
        statusPanel.addGlue();
        statusPanel.add("END");
        statusPanel.setMargin(8);

        JTable table = new JTable(50,10);
        table.setGridColor(Color.gray);

        mainComponentPanel.add(commandPanel, BorderLayout.NORTH);
        mainComponentPanel.add(table, BorderLayout.CENTER);
        mainComponentPanel.add(statusPanel, BorderLayout.SOUTH);

        return mainComponentPanel;
    }

    /**
     * ChartImpl Style
     * +---------------+
     * | Command Panel |
     * |---------------|
     * |   | TabPanel  |
     * |p1 |-----------|
     * |---| TextPane  |
     * |   |           |
     * |p2 |           |
     * |---------------|
     * | Status Panel  |
     * +---------------+
     */
    private static void testPattern2() {
        MainFrame f = new MainFrame();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(800,600);

        MainFrame.CommandPanel commandPanel = f.getCommandPanel();
        commandPanel.setPanelHeight(56);
        commandPanel.add(new JButton("FIRST"));
        commandPanel.addSeparator();
        commandPanel.add(new JButton("NEXT"));
        commandPanel.addGlue();
        commandPanel.add(new JButton("END"));

        StatusPanel statusPanel = f.getStatusPanel();
        statusPanel.add("Label1");
        statusPanel.addSeparator();
        statusPanel.add("Label2");
        statusPanel.addGlue();
        statusPanel.add("END");
        statusPanel.setMargin(8);

        MainFrame.MainPanel mainPanel = f.getMainPanel();
        mainPanel.setLayout(new BorderLayout(1,1));

        JPanel p = new JPanel(new BorderLayout());
        JPanel p1 = new JPanel() {
            private static final long serialVersionUID = 1L;
            @Override
            public void paintComponent(Graphics grahics) {
                super.paintComponent(grahics);
                Graphics g = grahics.create();
                g.setColor(Color.GRAY);
                g.drawLine(0, 0, getWidth(), 0);
                g.dispose();
            }
        };
        p1.setPreferredSize(new Dimension(200,100));
        p1.setBorder(BorderFactory.createTitledBorder("PANEL1"));
        JPanel p2 = new JPanel();
        p2.setPreferredSize(new Dimension(200,100));
        p2.setBorder(BorderFactory.createTitledBorder("PANEL2"));
        p.add(p1, BorderLayout.NORTH);
        p.add(p2, BorderLayout.CENTER);

        PNSTabbedPane tab = new PNSTabbedPane();
        tab.setButtonVgap(4);
        tab.addTab("カルテ", new JTextPane());
        JTable table = new JTable(50,10);
        table.setGridColor(Color.gray);
        tab.addTab("病名", table);

        mainPanel.add(p, BorderLayout.WEST);
        mainPanel.add(tab, BorderLayout.CENTER);

        f.setVisible(true);
    }

    /**
     * StampBox Style
     * +---------------+
     * | Command Panel |
     * |---------------|
     * |               |
     * | Tab Panel in  |
     * |               |
     * |---------------|
     * | Status Panel  |
     * |---------------|
     * | TabPanel out  |
     * +---------------+
     *
     */
    private static void testPattern3() {
        MainFrame f = new MainFrame();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // MainFrame の StatusPanel は使わない
        f.removeStatusPanel();
        // コマンドパネル
        MainFrame.CommandPanel commandPanel = f.getCommandPanel();
        commandPanel.add(new JButton("FIRST"));
        commandPanel.addSeparator();
        commandPanel.add(new JButton("NEXT"));
        commandPanel.addGlue();
        commandPanel.add(new JButton("END"));

        // 内側のタブ
        String[] tabStr = {"細菌検査", "注 射", "処 方","初診・再診","指導・在宅",
            "処 置","手 術","放射線","検体検査","生体検査","傷病名","テキスト","パ ス","ORCA","汎 用","その他"};
        PNSTabbedPane tabIn1 = createTreeTabPane(tabStr);
        final String[] tabStr2 = {"細菌検査", "処 方","初診・再診","指導・在宅",
            "処 置","手 術","放射線","検体検査","生体検査","傷病名","テキスト","パ ス","ORCA","汎 用","その他"};
        final PNSTabbedPane tabIn2 = createTreeTabPane(tabStr2);

        // 遅延生成テスト
        tabIn2.addChangeListener(new ChangeListener(){
            public void stateChanged(ChangeEvent e) {
                System.out.println("stateChanged");
                int index = tabIn2.getSelectedIndex();
                tabIn2.setComponentAt(index, new JLabel(String.valueOf(index) + ":" + tabStr2[index]));
            }
        });

        // 外側の tabbed pane
        PNSTabbedPane tabOut = new PNSTabbedPane();
        // 上下に隙間を入れる
        tabOut.setButtonVgap(4);
        // タブ位置は下
        tabOut.setTabPlacement(JTabbedPane.BOTTOM);
        tabOut.addTab("個人用", tabIn1);
        tabOut.addTab("ネットワーク", tabIn2);


        // main panel に tab を格納
        MainFrame.MainPanel mainPanel = f.getMainPanel();
        mainPanel.setLayout(new BorderLayout(0,0));
        mainPanel.add(tabOut, BorderLayout.CENTER);

        f.pack();
        f.setVisible(true);
    }

    private static PNSTabbedPane createTreeTabPane(String[] tabStr) {
        // 内側の tabbed pane
        PNSTabbedPane tab = new PNSTabbedPane();
        // ボタンパネルの余白設定
        tab.getButtonPanel().setPadding(new Dimension(4,4));
        // status panel を inner tab に設定
        StatusPanel statusPanel = new StatusPanel();
        tab.add(statusPanel, BorderLayout.SOUTH);
        statusPanel.add(new JLabel("STATUS PANEL TEST"));
        statusPanel.setMargin(16);

        JTree[] panes = new JTree[tabStr.length];

        for (int i=0; i<tabStr.length; i++) {
            DefaultMutableTreeNode root = new DefaultMutableTreeNode(tabStr[i]);
            DefaultMutableTreeNode swing = new DefaultMutableTreeNode("Swing");
            DefaultMutableTreeNode java2d = new DefaultMutableTreeNode("Java2D");
            DefaultMutableTreeNode java3d = new DefaultMutableTreeNode("Java3D");
            DefaultMutableTreeNode javamail = new DefaultMutableTreeNode("JavaMail");

            DefaultMutableTreeNode swingSub1 = new DefaultMutableTreeNode("JLabel");
            DefaultMutableTreeNode swingSub2 = new DefaultMutableTreeNode("JButton");
            DefaultMutableTreeNode swingSub3 = new DefaultMutableTreeNode("JTextField");

            swing.add(swingSub1);
            swing.add(swingSub2);
            swing.add(swingSub3);

            root.add(swing);
            root.add(java2d);
            root.add(java3d);
            root.add(javamail);

            panes[i] = new JTree(root);
            panes[i].setPreferredSize(new Dimension(500,700));
            tab.addTab(tabStr[i], panes[i]);
        }

        return tab;
    }
}
