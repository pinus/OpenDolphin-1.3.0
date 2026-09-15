package open.dolphin.calendar;

import open.dolphin.helper.Holiday;
import open.dolphin.infomodel.SimpleDate;

import javax.swing.table.AbstractTableModel;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;

/// CalendarTableModel.
///
/// @author Kazushi Minagawa Digital Globe, Inc.
/// @author pns
public class CalendarTableModel extends AbstractTableModel {
    
    private static final String[] COLUMN_NAME = {"日", "月", "火", "水", "木", "金", "土"};
    private final int numRows = 6; // 6週で固定
    private final int numCols = 7; // 7日で固定
    // SimpleDate を入れる HashMap
    private final HashMap<LocalDate, SimpleDate> data = new HashMap<>();
    // 現在の年月
    private LocalDate thisMonth;
    // 現在の左上隅の日
    private LocalDate startDate;
    // 外部から登録された，EventCode でマークされた SimpleDate のコレクション
    private Collection<SimpleDate> markedDates;
    // 誕生日
    private SimpleDate birthday;

    /// CalendarTableModel を生成する.
    ///
    /// @param year  カレンダの年
    /// @param month 　 カレンダの月
    public CalendarTableModel(int year, int month) {
        // 作成する月の最初の日  yyyyMM01
        thisMonth = LocalDate.of(year, month, 1);
        calculateStartDate();
    }

    private void calculateStartDate() {
        // その月の１日の日付は週の何日目か 1=SUN 7=SAT
        int diff = thisMonth.get(WeekFields.of(Locale.JAPAN).dayOfWeek());
        // このカレンダーの左上の日まで戻して左上の日を登録
        startDate = thisMonth.minusDays(diff - 1);
        fireTableDataChanged();
    }

    /// 今表示しているカレンダーがどの月か決める.
    /// startDate の２週後の週の週末(20日後)を含む月がこのモデルの表す月と定義する.
    private void calculateThisMonth() {
        LocalDate twentyDaysAfter = startDate.plusDays(20);
        thisMonth = LocalDate.of(twentyDaysAfter.getYear(), twentyDaysAfter.getMonthValue(), 1);
        fireTableDataChanged();
    }

    /// model を１週間進める.
    public void nextWeek() {
        startDate = startDate.plusWeeks(1);
        // month は startDay の位置に応じて計算し直す
        calculateThisMonth();
    }

    /// model を１週間戻す.
    public void previousWeek() {
        startDate = startDate.minusWeeks(1);
        // month は startDay の位置に応じて計算し直す
        calculateThisMonth();
    }

    /// model を１ヶ月進める.
    public void nextMonth() {
        thisMonth = thisMonth.plusMonths(1);
        calculateStartDate();
    }

    /// model を１ヶ月戻す.
    public void previousMonth() {
        thisMonth = thisMonth.minusMonths(1);
        calculateStartDate();
    }

    /// model を今日にリセットする.
    public void reset() {
        LocalDate today = LocalDate.now();
        reset(today.getYear(), today.getMonthValue());
    }

    /// model を y年 m月にリセットする.
    ///
    /// @param y year
    /// @param m month
    public void reset(int y, int m) {
        thisMonth = LocalDate.of(y, m, 1);
        calculateStartDate();
        fireTableDataChanged();
    }

    /// Returns the year of the currently displayed calendar model.
    ///
    /// @return The year of the current calendar as an integer.
    public int getYear() {
        return thisMonth.getYear();
    }

    /// Retrieves the numerical value of the month currently represented by this calendar model.
    ///
    /// @return An integer representing the current month (from 1 for January to 12 for December).
    public int getMonth() {
        return thisMonth.getMonthValue();
    }

    @Override
    public String getColumnName(int col) {
        return COLUMN_NAME[col];
    }

    @Override
    public int getRowCount() {
        return numRows;
    }

    @Override
    public int getColumnCount() {
        return numCols;
    }

    /// Retrieves the value at a specified row and column in the underlying calendar table model.
    /// If the specified date does not exist in the internal data structure, a new SimpleDate
    /// object for that date is created, added to the data map, and then returned.
    ///
    /// @param row The row index in the table.
    /// @param col The column index in the table.
    /// @return The SimpleDate object corresponding to the specified row and column.
    @Override
    public Object getValueAt(int row, int col) {

        // Cell 番号を得る
        int cellNumber = row * numCols + col;

        // startDay から cellNumber だけ進める
        LocalDate theDate = startDate.plusDays(cellNumber);

        // その日に対応する SimpleDate
        SimpleDate ret = data.get(theDate);

        // データがない場合は作る
        if (ret == null) {
            ret = createSimpleDate(theDate);
            data.put(theDate, ret);
        }

        return ret;
    }

    /// Updates the value at the specified row and column in the calendar table model.
    /// The provided value must be an instance of SimpleDate, which will be used to update
    /// the internal data map after converting it to a LocalDate object.
    ///
    /// @param value The new value to be set, expected to be an instance of SimpleDate.
    /// @param row The row index in the table where the value should be set.
    /// @param col The column index in the table where the value should be set.
    @Override
    public void setValueAt(Object value, int row, int col) {
        SimpleDate d = (SimpleDate) value;
        LocalDate theDate = LocalDate.of(d.getYear(), d.getMonth(), d.getDay());
        data.put(theDate, d);
    }

    /// Creates a new SimpleDate object based on the given LocalDate instance.
    ///
    /// @param localDate The LocalDate instance from which to create the SimpleDate.
    /// @return A new SimpleDate instance representing the given LocalDate.
    private SimpleDate createSimpleDate(LocalDate localDate) {
        return createSimpleDate(new SimpleDate(localDate));
    }

    /// Enhances a given SimpleDate object by setting event codes based on certain conditions.
    /// If the given date matches the current date, the "TODAY" event code is applied. Additionally,
    /// if the given date matches the registered birthday, the "BIRTHDAY" event code is applied.
    /// The method also invokes a holiday marking logic to set any applicable holiday event codes.
    ///
    /// @param date The SimpleDate object to enhance with event codes and holiday information.
    /// @return The enhanced SimpleDate object with updated event codes, if applicable.
    private SimpleDate createSimpleDate(SimpleDate date) {
        SimpleDate today = new SimpleDate(LocalDate.now());
        // 休日登録
        Holiday.setTo(date);
        // 今日なら上書き登録
        if (date.equals(today)) {
            date.setEventCode(CalendarEvent.TODAY.name());
        }
        // さらに誕生日なら上書き登録
        if (birthday != null && birthday.getMonth() == date.getMonth() && birthday.getDay() == date.getDay()) {
            date.setEventCode(CalendarEvent.BIRTHDAY.name());
        }
        return date;
    }

    /// Sets the birthday date using the provided string representation.
    /// The string should follow the format "YYYY-MM-DDThh:mm:ss".
    /// This method initializes the birthday field with a SimpleDate object
    /// created from the given string.
    ///
    /// @param mmlBirthday A string representing the birthday in the format "YYYY-MM-DDThh:mm:ss".
    public void setBirthday(String mmlBirthday) {
        birthday = new SimpleDate(mmlBirthday);
    }

    /// Retrieves the collection of marked SimpleDate instances maintained by the calendar model.
    ///
    /// @return A collection of SimpleDate objects that have been marked, representing specific dates
    ///         with associated events or special significance.
    public Collection<SimpleDate> getMarkDates() {
        return markedDates;
    }

    /// Sets the collection of marked dates for the calendar model. Each date in the given collection
    /// is processed to associate specific event codes if applicable. If a date matches the current
    /// day's date, the "TODAY" event code is assigned. The processed dates are stored internally
    /// and used for rendering the calendar data.
    ///
    /// @param c A collection of SimpleDate objects representing the dates to be marked. This can be null,
    ///          in which case no dates are processed or marked.
    public void setMarkDates(Collection<SimpleDate> c) {
        markedDates = c;
        SimpleDate today = new SimpleDate(LocalDate.now());
        if (c != null) {
            c.forEach(date -> {
                // 今日
                if (today.equals(date)) {
                    date.setEventCode(CalendarEvent.TODAY.name());
                }
                LocalDate target = LocalDate.of(date.getYear(), date.getMonth(), date.getDay());
                data.put(target, date);
            });
            fireTableDataChanged();
        }
    }

    /// Clears the event codes of dates in the calendar model that match the specified event.
    /// For each matching date, this method removes the association with the specified event
    /// and restores other date-related information such as holidays, the current-day marking,
    /// and birthday information.
    /// Finally, it refreshes the table data to reflect the changes.
    ///
    /// @param event The event code to be cleared from the dates in the calendar model.
    public void clearMarkDates(String event) {
        data.values().stream().filter(date -> event.equals(date.getEventCode())).forEach(date -> {
            date.setEventCode(null);
            // 休日，今日，誕生日情報をリストア
            createSimpleDate(date);
        });
        fireTableDataChanged();
    }

    /// Checks if the cell specified by the given row and column indices falls outside the currently
    /// represented month in the calendar model.
    ///
    /// @param row The row index of the cell in the calendar table.
    /// @param col The column index of the cell in the calendar table.
    /// @return `true` if the specified cell's date is not within the currently displayed month;
    ///         `false` otherwise.
    public boolean isOutOfMonth(int row, int col) {
        // startDate から cellNumber 進める
        int cellNumber = row * numCols + col;
        LocalDate test = startDate.plusDays(cellNumber);
        return thisMonth.getYear() != test.getYear() || thisMonth.getMonthValue() != test.getMonthValue();
    }
}
