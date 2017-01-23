package open.dolphin.client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashMap;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;

import open.dolphin.infomodel.SimpleDate;
import open.dolphin.ui.PNSBorderFactory;
import open.dolphin.util.Holiday;

/**
 * LiteCalendarPanel
 *
 * Kazushi Minagawa
 */
public class LiteCalendarPanel extends JPanel implements PropertyChangeListener {

    private static final long serialVersionUID = -3472737594106311587L;

    public static final String SELECTED_DATE_PROP = "selectedDateProp";
    public static final String MARK_LIST_PROP = "markListProp";

    // 表示のデフォルト設定
    private static final int TITLE_ALIGN = SwingConstants.CENTER;
//pns    private static final int TITLE_FONT_SIZE = 14;
    private static final int TITLE_FONT_SIZE = 13;
    private static final Font TITLE_FONT = new Font("Dialog", Font.PLAIN, TITLE_FONT_SIZE);
    private static final Font CALENDAR_FONT = new Font("Dialog", Font.PLAIN, ClientContext.getInt("calendar.font.size"));
    private static final Font OUTOF_MONTH_FONT = new Font("Dialog", Font.PLAIN, ClientContext.getInt("calendar.font.size.outOfMonth"));

    // カレンダテーブル
    private int relativeMonth;
    private int year;
    private int month;
    private CalendarTableModel tableModel;
    private JTable table;
    private PropertyChangeSupport boundSupport;
    private Object selectedDate;
    private JLabel titleLabel;
    private SimpleDate today;

    private HashMap eventColorTable;

    // 表示用の属性
    private Color titleFore = ClientContext.getColor("color.calendar.title.fore");
    private Color titleBack = ClientContext.getColor("color.calendar.title.back");
    private int titleAlign = TITLE_ALIGN;
    private Font titleFont = TITLE_FONT;
    private int cellWidth = ClientContext.getInt("calendar.cell.width");
    private int cellHeight = ClientContext.getInt("calendar.cell.height");
    private int autoResizeMode = JTable.AUTO_RESIZE_ALL_COLUMNS;
    private boolean cellSelectionEnabled = true;

    private Color sundayFore = ClientContext.getColor("color.SUNDAY_FORE");
    private Color saturdayFore = ClientContext.getColor("color.SATURDAY_FORE");
    private Color weekdayFore = ClientContext.getColor("color.WEEKDAY_FORE");
    private Color outOfMothFore = ClientContext.getColor("color.OUTOFMONTH_FORE");
    private Color calendarBack = ClientContext.getColor("color.CALENDAR_BACK");
    private Color todayBack = ClientContext.getColor("color.TODAY_BACK");
    private Color birthdayBack = ClientContext.getColor("color.BIRTHDAY_BACK");

    private Font calendarFont = CALENDAR_FONT;
    private Font outOfMonthFont = OUTOF_MONTH_FONT;

//pns^
    private JPanel titlePanel = new JPanel();
    public JPanel getTitlePanel() {
        return titlePanel;
    }
//pns$

    public LiteCalendarPanel() {
        super();
    }

    public LiteCalendarPanel(int n) {
        this(n, true);
    }

    public LiteCalendarPanel(int n, boolean addTitle) {

        // 作成するカレンダの当月を起点とする相対月数（n ケ月前/後)
        relativeMonth = n;
        GregorianCalendar gc = new GregorianCalendar();
        gc.clear(Calendar.MILLISECOND);
        gc.clear(Calendar.SECOND);
        gc.clear(Calendar.MINUTE);
        gc.clear(Calendar.HOUR_OF_DAY);
        gc.add(Calendar.MONTH, relativeMonth);
        year = gc.get(Calendar.YEAR);
        month = gc.get(Calendar.MONTH);

        tableModel = new CalendarTableModel(year, month);
        table = new JTable(tableModel);
        setAutoResizeMode(autoResizeMode);
        table.setBackground(calendarBack);

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setCellSelectionEnabled(cellSelectionEnabled);

        setCellWidth(cellWidth);
        setCellHeight(cellHeight);

        // Replace DefaultRender
        DateRenderer dateRenderer = new DateRenderer();
//pns^  dateRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        dateRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        // for retina iMac
        //table.setShowHorizontalLines(true);
        //table.setShowVerticalLines(false);
        //table.setGridColor(new Color(250,250,250));
        table.setRowSelectionAllowed(false);
        table.setIntercellSpacing(new Dimension(0,0));
        //header
        table.getTableHeader().setDefaultRenderer(new CalendarHeaderRenderer());
        Dimension dim = table.getTableHeader().getPreferredSize();
        dim.height += 2;
        table.getTableHeader().setPreferredSize(dim);
        table.getTableHeader().setResizingAllowed(false);
        table.getTableHeader().setReorderingAllowed(false);
//pns$
        table.setDefaultRenderer(java.lang.Object.class, dateRenderer);
        // MouseAdapter
        table.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {

                if (e.getClickCount() != 1) {
                    return;
                }

                Point p = e.getPoint();
                int row = table.rowAtPoint(p);
                int col = table.columnAtPoint(p);
                if (row != -1 && col != -1) {
                    //Object o = table.getValueAt(row, col);
                    Object o = tableModel.getValueAt(row, col);
                    setSelectedDate(o);
                }
            }
        });

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(table.getTableHeader(), BorderLayout.NORTH);
        panel.add(table, BorderLayout.CENTER);

        StringBuilder buf = new StringBuilder();
        buf.append(year);
        buf.append(ClientContext.getString("calendar.title.year"));
        buf.append(month + 1);
        buf.append(ClientContext.getString("calendar.title.month"));
        setTitleLabel(new JLabel(buf.toString()));
        setTitleAlign(titleAlign);
        setTitleFont(titleFont);
        setTitleFore(titleFore);
        setTitleBack(titleBack);
        getTitleLabel().setOpaque(true);

        // レイアウトする
        this.setLayout(new BorderLayout(0,0));
        if (addTitle) {
//pns^
            titleLabel.setOpaque(false);
            titlePanel.setPreferredSize(new Dimension(10, 22));
            titlePanel.setBorder(PNSBorderFactory.createTitleBarBorder(new Insets(0,0,0,0)));
            titlePanel.setLayout(new BorderLayout(0,0));
            titlePanel.add(titleLabel, BorderLayout.CENTER);

            this.add(titlePanel, BorderLayout.NORTH);
            //this.add(getTitleLabel(), BorderLayout.NORTH);
//pns$
        }
        this.add(panel, BorderLayout.CENTER);
//pns   this.setBorder(BorderFactory.createEtchedBorder());

        boundSupport = new PropertyChangeSupport(this);
    }

    public int getYear() {
        return year;
    }

    public int getMonth() {
        return month;
    }

    @Override
    public void addPropertyChangeListener(String prop, PropertyChangeListener l) {
        if (boundSupport == null) {
            boundSupport = new PropertyChangeSupport(this);
        }
        boundSupport.addPropertyChangeListener(prop, l);
    }

    @Override
    public void removePropertyChangeListener(String prop,
            PropertyChangeListener l) {
        if (boundSupport == null) {
            boundSupport = new PropertyChangeSupport(this);
        }
        boundSupport.removePropertyChangeListener(prop, l);
    }

    public void propertyChange(PropertyChangeEvent e) {

        String prop = e.getPropertyName();
        if (prop.equals(MARK_LIST_PROP)) {
            Collection list = (Collection) e.getNewValue();
            tableModel.setMarkDates(list);
        }
    }

    /**
     * 選択された日を通知する.
     */
    public void setSelectedDate(Object o) {
        Object old = selectedDate;
        selectedDate = o;
        if (selectedDate instanceof String) {
            SimpleDate sd = new SimpleDate(getYear(), getMonth(), Integer.parseInt((String) selectedDate));
            selectedDate = sd;
        }
        boundSupport.firePropertyChange(SELECTED_DATE_PROP, old, selectedDate);
    }

    public JTable getTable() {
        return table;
    }

    public CalendarTableModel getTableModel() {
        return tableModel;
    }

    public int getRelativeMonth() {
        return relativeMonth;
    }

    public HashMap getEventColorTable() {
        return eventColorTable;
    }

    public void setEventColorTable(HashMap ht) {
        eventColorTable = ht;
    }

    /**
     * @param titleFore
     *            The titleFore to set.
     */
    public void setTitleFore(Color titleFore) {
        this.titleFore = titleFore;
        getTitleLabel().setForeground(titleFore);
    }

    /**
     * @return Returns the titleFore.
     */
    public Color getTitleFore() {
        return titleFore;
    }

    /**
     * @param titleBack
     *            The titleBack to set.
     */
    private void setTitleBack(Color titleBack) {
        this.titleBack = titleBack;
        getTitleLabel().setBackground(titleBack);
    }

    /**
     * @param titleAlign
     *            The titleAlign to set.
     */
    private void setTitleAlign(int titleAlign) {
        this.titleAlign = titleAlign;
        getTitleLabel().setHorizontalAlignment(titleAlign);
    }

    /**
     * @param titleFont
     *            The titleFont to set.
     */
    private void setTitleFont(Font titleFont) {
        this.titleFont = titleFont;
        getTitleLabel().setFont(titleFont);
    }

    /**
     * @param cellWidth
     *            The cellWidth to set.
     */
    public void setCellWidth(int cellWidth) {
        this.cellWidth = cellWidth;
        TableColumn column = null;
        for (int i = 0; i < 7; i++) {
            column = table.getColumnModel().getColumn(i);
            column.setPreferredWidth(cellWidth);
        }
    }

    /**
     * @return Returns the cellWidth.
     */
    public int getCellWidth() {
        return cellWidth;
    }

    /**
     * @param cellHeight
     *            The cellHeight to set.
     */
    public void setCellHeight(int cellHeight) {
        this.cellHeight = cellHeight;
        table.setRowHeight(cellHeight);
    }

    /**
     * @return Returns the cellHeight.
     */
    public int getCellHeight() {
        return cellHeight;
    }

    /**
     * @param autoResize
     *            The autoResize to set.
     */
    public void setAutoResizeMode(int mode) {
        this.autoResizeMode = mode;
    }

    /**
     * @return Returns the autoResize.
     */
    public int getAutoResizeMode() {
        return autoResizeMode;
    }

    /**
     * @param cellSelectionEnabled
     *            The cellSelectionEnabled to set.
     */
    public void setCellSelectionEnabled(boolean cellSelectionEnabled) {
        this.cellSelectionEnabled = cellSelectionEnabled;
    }

    /**
     * @return Returns the cellSelectionEnabled.
     */
    public boolean isCellSelectionEnabled() {
        return cellSelectionEnabled;
    }

    /**
     * @param sundayFore
     *            The sundayFore to set.
     */
    public void setSundayFore(Color sundayFore) {
        this.sundayFore = sundayFore;
    }

    /**
     * @return Returns the sundayFore.
     */
    public Color getSundayFore() {
        return sundayFore;
    }

    /**
     * @param saturdayFore
     *            The saturdayFore to set.
     */
    public void setSaturdayFore(Color saturdayFore) {
        this.saturdayFore = saturdayFore;
    }

    /**
     * @return Returns the saturdayFore.
     */
    public Color getSaturdayFore() {
        return saturdayFore;
    }

    /**
     * @param weekdayFore
     *            The weekdayFore to set.
     */
    public void setWeekdayFore(Color weekdayFore) {
        this.weekdayFore = weekdayFore;
    }

    /**
     * @return Returns the weekdayFore.
     */
    public Color getWeekdayFore() {
        return weekdayFore;
    }

    /**
     * @param outOfMothFore
     *            The outOfMothFore to set.
     */
    public void setOutOfMothFore(Color outOfMothFore) {
        this.outOfMothFore = outOfMothFore;
    }

    /**
     * @return Returns the outOfMothFore.
     */
    public Color getOutOfMothFore() {
        return outOfMothFore;
    }

    /**
     * @param calendarBack
     *            The calendarBack to set.
     */
    public void setCalendarBack(Color calendarBack) {
        this.calendarBack = calendarBack;
    }

    /**
     * @return Returns the calendarBack.
     */
    public Color getCalendarBack() {
        return calendarBack;
    }

    /**
     * @param todayBack
     *            The todayBack to set.
     */
    public void setTodayBack(Color todayBack) {
        this.todayBack = todayBack;
    }

    /**
     * @return Returns the todayBack.
     */
    public Color getTodayBack() {
        return todayBack;
    }

    /**
     * @param birthdayBack
     *            The birthdayBack to set.
     */
    public void setBirthdayBack(Color birthdayBack) {
        this.birthdayBack = birthdayBack;
    }

    /**
     * @return Returns the birthdayBack.
     */
    public Color getBirthdayBack() {
        return birthdayBack;
    }

    /**
     * @param calendarFont
     *            The calendarFont to set.
     */
    public void setCalendarFont(Font calendarFont) {
        this.calendarFont = calendarFont;
    }

    /**
     * @return Returns the calendarFont.
     */
    public Font getCalendarFont() {
        return calendarFont;
    }

    /**
     * @param outOfMonthFont
     *            The outOfMonthFont to set.
     */
    public void setOutOfMonthFont(Font outOfMonthFont) {
        this.outOfMonthFont = outOfMonthFont;
    }

    /**
     * @return Returns the outOfMonthFont.
     */
    public Font getOutOfMonthFont() {
        return outOfMonthFont;
    }

    public void setToday(SimpleDate today) {
        this.today = today;
    }

    /**
     * @param titleLabel
     *            The titleLabel to set.
     */
    public void setTitleLabel(JLabel titleLabel) {
        this.titleLabel = titleLabel;
    }

    /**
     * @return Returns the titleLabel.
     */
    public JLabel getTitleLabel() {
        return titleLabel;
    }

    /**
     * Custom table cell renderer for the carendar panel.
     */
    protected class DateRenderer extends DefaultTableCellRenderer {

        private static final long serialVersionUID = 5817292848730765481L;

        public DateRenderer() {
            super();
            this.setOpaque(true);
//pns       this.setHorizontalAlignment(SwingConstants.RIGHT);
            this.setHorizontalAlignment(SwingConstants.CENTER);
        }

        // grid for retina iMac
        @Override
        public void paint (Graphics graphics) {
            super.paint(graphics);
            Graphics g = graphics.create();
            g.setColor(Color.WHITE);
            g.drawLine(0, getHeight(), getWidth(), getHeight());
            g.dispose();
        }

        @Override
        public Component getTableCellRendererComponent(JTable table,
                Object value, boolean isSelected, boolean isFocused, int row,
                int col) {

            Component compo = super.getTableCellRendererComponent(table, value,
                    isSelected, isFocused, row, col);
            if (compo != null && value != null) {
//pns^
                if (getOutOfMonthFont() == null && tableModel.isOutOfMonth(row, col)) {
                    return null;
                }
//pns$

                // 日を書く
                String day = null;
                Color color = null;

                if (value instanceof SimpleDate) {
                    day = ((SimpleDate) value).toString();
                    if (today != null
                            && today.compareTo((SimpleDate) value) == 0) {
                        // color = todayBack;
                        color = (Color) eventColorTable.get("TODAY");
                    } else {
                        color = (Color) eventColorTable
                                .get(((SimpleDate) value).getEventCode());
                        // color = Color.black;
                    }

                } else if (value instanceof String) {
                    day = (String) value;
                    if (today != null
                            && today.equalDate(year, month, Integer
                            .parseInt(day))) {
                        // color = todayBack;
                        color = (Color) eventColorTable.get("TODAY");
                    } else {
                        color = getCalendarBack();
                    }
                }

                ((JLabel) compo).setText(day);

                // 曜日によって ForeColor を変える
                if (col == 0) {
                    this.setForeground(getSundayFore());

                } else if (col == 6) {
                    this.setForeground(getSaturdayFore());

                } else {
                    this.setForeground(getWeekdayFore());
                }

                // 休日 masuda
                SimpleDate sd = (SimpleDate) tableModel.getValueAt(row, col);
                if (Holiday.isHoliday(new GregorianCalendar(sd.getYear(), sd.getMonth(), sd.getDay()))) {
                    this.setForeground(getSundayFore());
                }
                // masuda

                // このカレンダ月内の日かどうかでフォントを変える
                if (tableModel.isOutOfMonth(row, col)) {
                    this.setFont(getOutOfMonthFont());
                    this.setBackground(getCalendarBack());

                } else {
                    this.setFont(getCalendarFont());
                    this.setBackground(color);
                }
            }
            return compo;
        }
    }
}
