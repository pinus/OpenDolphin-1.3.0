package open.dolphin.client;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Calendar;
import java.util.GregorianCalendar;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import open.dolphin.infomodel.SimpleDate;

/**
 * Calendar を表示する JTable.
 * @author pns
 */
public class CalendarTable extends JTable {
    private static final long serialVersionUID = 1L;

    private static final String[] MONTH_NAME = new String[] {"JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC"};

    private static final Color CALENDAR_BACKGROUND = new Color(227,250,207);
    private static final Color SUNDAY_FOREGROUND = new Color(255,0,130);
    private static final Color SATURDAY_FOREGROUND = new Color(0,0,255);
    private static final Color WEEKDAY_FOREGROUND = new Color(20,20,70);
    private static final Font TITLE_FONT = new Font("Courier", Font.BOLD, 72);
    private static final Font CALENDAR_FONT = new Font("Dialog", Font.PLAIN, 14);
    private static final Font CALENDAR_FONT_SMALL = new Font("Dialog", Font.PLAIN, 10);
    private static final int ROW_HEIGHT = 24;

    private CalendarTableModel tableModel;

    // 今月からの変位月数
    private int monthDiff;
    // 日付がマウスで選択された時に呼ばれる listener
    private CalendarListener listener;
    // header 付きのカレンダー
    private JPanel calendarPanel;

    /**
     * 今月のカレンダーを作る.
     */
    public CalendarTable() {
        this(0);
    }

    /**
     * 今月から differntial 月分だけずれたカレンダーを作る.
     * @param differential
     */
    public CalendarTable(int differential) {
        monthDiff = differential;
        initComponents();
    }

    private void initComponents() {
        // TableModel をセット
        GregorianCalendar gc = new GregorianCalendar();
        gc.add(Calendar.MONTH, monthDiff);
        tableModel = new CalendarTableModel(gc);
        setModel(tableModel);

        // SelectionModel をセット
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setCellSelectionEnabled(true);
        setRowSelectionAllowed(false);

        // TableCellRenderer をセット
        setDefaultRenderer(Object.class, new DateRenderer());
        getTableHeader().setDefaultRenderer(new CalendarHeaderRenderer());

        // Header の設定
        getTableHeader().setResizingAllowed(false);
        getTableHeader().setReorderingAllowed(false);

        // その他の設定
        setRowHeight(ROW_HEIGHT);
        setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        setBackground(CALENDAR_BACKGROUND);
        setIntercellSpacing(new Dimension(0,0));
        setShowGrid(false);

        // MouseListener から CalendarListener にブリッジする
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (listener != null) {
                    Point p = e.getPoint();
                    int row = rowAtPoint(p);
                    int col = columnAtPoint(p);

                    if (row != -1 && col != -1) {
                        SimpleDate date = (SimpleDate) tableModel.getValueAt(row, col);
                        listener.dateSelected(date);
                    }
                }
            }
        });

        // ヘッダのついた Panel を作る
        calendarPanel = new JPanel(new BorderLayout());
        calendarPanel.add(getTableHeader(), BorderLayout.NORTH);
        calendarPanel.add(this, BorderLayout.CENTER);
    }

    /**
     * 曜日ヘッダ付きのカレンダーを返す.
     * @return
     */
    public JPanel getPanel() {
        return calendarPanel;
    }

    /**
     * カレンダーリスナーを登録する.
     * @param l
     */
    public void addCalendarListener(CalendarListener l) {
        listener = l;
    }

    /**
     * SimpleDate の Event 名を表示する.
     * @param e
     * @return
     */
    @Override
    public String getToolTipText(MouseEvent e) {
        int row = rowAtPoint(e.getPoint());
        int col = columnAtPoint(e.getPoint());
        SimpleDate d = (SimpleDate) getValueAt(row, col);
        return CalendarEvent.getTitle(d.getEventCode());
    }

    /**
     * バックグランドに月と年を出す.
     * @param graphics
     */
    @Override
    public void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.1f));

        String month = MONTH_NAME[tableModel.getMonth()];
        String year = String.valueOf(tableModel.getYear());

        g.setFont(TITLE_FONT);
        g.setColor(Color.BLUE);
        FontMetrics fm = g.getFontMetrics();
        int x1 = (getWidth() - fm.stringWidth(month)) / 2;
        int x2 = (getWidth() - fm.stringWidth(year)) / 2;
        int y = (getHeight() - fm.getHeight()*2) / 2;

        g.drawString(month, x1, y + fm.getAscent());
        g.drawString(year, x2, y + fm.getHeight() + fm.getAscent());

    }

    /**
     * Custom table cell renderer for CalendarTable.
     */
    private class DateRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 5817292848730765481L;

        private Color eventColor;

        public DateRenderer() {
            super();
            init();
        }

        private void init() {
            setBorder(BorderFactory.createLineBorder(Color.red));
            setOpaque(true);
            setHorizontalAlignment(SwingConstants.CENTER);
        }

        // Event の色を，円のバックグランドで描く
        @Override
        public void paintComponent (Graphics graphics) {
            //super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.8f));

            int w = getWidth();
            int h = getHeight();

            if (eventColor != null) {
                int d = Math.min(w, h);
                int x = (w-d)/2;
                int y = (h-d)/2;
                g.setColor(eventColor);
                g.fillOval(x, y, d, d);
            }

            String text = getText();
            FontMetrics fm = g.getFontMetrics();
            int strWidth = fm.stringWidth(text);

            int x = (w - strWidth)/2;
            int y = (h - fm.getHeight())/2 + fm.getAscent(); // height でセンタリングして ascent 分下げる

            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
            g.setColor(getForeground());
            g.drawString(text, x, y);

            g.dispose();
        }

        @Override
        public Component getTableCellRendererComponent(JTable table,
                Object value, boolean isSelected, boolean isFocused, int row,
                int col) {

            Component compo = super.getTableCellRendererComponent(table, value, isSelected, isFocused, row, col);

            if (compo != null && value != null) {

                SimpleDate targetDate = (SimpleDate) value;

                Color foregroundColor;

                // 曜日によって ForeColor を変える
                switch (col) {
                    // 当院は日・水は休み
                    case 0:
                    case 3:
                        foregroundColor = SUNDAY_FOREGROUND;
                        break;
                    case 6:
                        foregroundColor = SATURDAY_FOREGROUND;
                        break;
                    default:
                        foregroundColor = WEEKDAY_FOREGROUND;
                        break;
                }

                // Event "PVT", "TODAY", "BIRTHDAY"
                eventColor = CalendarEvent.getColor(targetDate.getEventCode());

                // Holiday
                if (eventColor == null && targetDate.getEventCode() != null) {
                    foregroundColor = SUNDAY_FOREGROUND;
                }

                String day = String.valueOf(targetDate.getDay());
                ((JLabel) compo).setText(day);

                // このカレンダ月内の日かどうかでフォントを変える
                if (tableModel.isOutOfMonth(row, col)) {
                    setFont(CALENDAR_FONT_SMALL);

                } else {
                    setFont(CALENDAR_FONT);
                }

                setForeground(foregroundColor);
            }
            return compo;
        }
    }
    public static void main (String[] arg) {
        open.dolphin.client.ClientContext.setClientContextStub(new open.dolphin.client.ClientContextStub());

        JFrame f = new JFrame();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        CalendarTable table = new CalendarTable();

        f.add(table.getPanel());
        f.pack();
        f.setVisible(true);
    }
}
