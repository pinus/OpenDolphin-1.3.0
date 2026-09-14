package open.dolphin.calendar;

import jakarta.ejb.Local;
import open.dolphin.helper.Holiday;
import open.dolphin.infomodel.SimpleDate;

import javax.swing.table.AbstractTableModel;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;

/**
 * CalendarTableModel.
 *
 * @author Kazushi Minagawa Digital Globe, Inc.
 * @author pns
 */
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

    /**
     * CalendarTableModel を生成する.
     *
     * @param year  カレンダの年
     * @param month 　 カレンダの月
     */
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

    /**
     * 今表示しているカレンダーがどの月か決める.
     * startDate の２週後の週の週末(20日後)を含む月がこのモデルの表す月と定義する.
     */
    private void calculateThisMonth() {
        LocalDate twentyDaysAfter = startDate.plusDays(20);
        thisMonth = LocalDate.of(twentyDaysAfter.getYear(), twentyDaysAfter.getMonthValue(), 1);
        fireTableDataChanged();
    }

    /**
     * model を１週間進める.
     */
    public void nextWeek() {
        startDate = startDate.plusWeeks(1);
        // month は startDay の位置に応じて計算し直す
        calculateThisMonth();
    }

    /**
     * model を１週間戻す.
     */
    public void previousWeek() {
        startDate = startDate.minusWeeks(1);
        // month は startDay の位置に応じて計算し直す
        calculateThisMonth();
    }

    /**
     * model を１ヶ月進める.
     */
    public void nextMonth() {
        thisMonth = thisMonth.plusMonths(1);
        calculateStartDate();
    }

    /**
     * model を１ヶ月戻す.
     */
    public void previousMonth() {
        thisMonth = thisMonth.minusMonths(1);
        calculateStartDate();
    }

    /**
     * model を今日にリセットする.
     */
    public void reset() {
        LocalDate today = LocalDate.now();
        reset(today.getYear(), today.getMonthValue());
    }

    /**
     * model を y年 m月にリセットする.
     *
     * @param y year
     * @param m month
     */
    public void reset(int y, int m) {
        thisMonth = LocalDate.of(y, m, 1);
        calculateStartDate();
        fireTableDataChanged();
    }

    /**
     * 現在の model の年を返す.
     *
     * @return
     */
    public int getYear() {
        return thisMonth.getYear();
    }

    /**
     * 現在の model の月を返す.
     *
     * @return
     */
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

    /**
     * 指定された row, col から SimpleDate を取り出す.
     *
     * @param row
     * @param col
     * @return
     */
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

    /**
     * 指定された row, col に SimpleDate を設定する.
     * SimpleDate の日付に設定することにしたので，row, col は使ってない.
     *
     * @param value
     * @param row
     * @param col
     */
    @Override
    public void setValueAt(Object value, int row, int col) {
        SimpleDate d = (SimpleDate) value;
        LocalDate theDate = LocalDate.of(d.getYear(), d.getMonth(), d.getDay());
        data.put(theDate, d);
    }

    /**
     * EventCode に今日情報，休日情報，誕生日を入れた SimpleDate を作る.
     *
     * @param localDate
     * @return
     */
    private SimpleDate createSimpleDate(LocalDate localDate) {
        return createSimpleDate(new SimpleDate(localDate));
    }

    /**
     * SimpleDate の EventCode に今日情報，休日情報，誕生日を入れる.
     *
     * @param date
     * @return
     */
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

    /**
     * 誕生日を登録する.
     *
     * @param mmlBirthday
     */
    public void setBirthday(String mmlBirthday) {
        birthday = new SimpleDate(mmlBirthday);
    }

    /**
     * 登録された マーク付き SimpleDate のリストを返す.
     *
     * @return
     */
    public Collection<SimpleDate> getMarkDates() {
        return markedDates;
    }

    /**
     * マークされた SimpleDate をモデルに追加する.
     *
     * @param c
     */
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

    /**
     * event のマークをクリアする.
     *
     * @param event
     */
    public void clearMarkDates(String event) {
        data.values().stream().filter(date -> event.equals(date.getEventCode())).forEach(date -> {
            date.setEventCode(null);
            // 休日，今日，誕生日情報をリストア
            createSimpleDate(date);
        });
        fireTableDataChanged();
    }

    /**
     * 指定 row, col が今月から外れているかどうか.
     *
     * @param row
     * @param col
     * @return
     */
    public boolean isOutOfMonth(int row, int col) {
        // startDate から cellNumber 進める
        int cellNumber = row * numCols + col;
        LocalDate test = startDate.plusDays(cellNumber);
        return thisMonth.getYear() != test.getYear() || thisMonth.getMonthValue() != test.getMonthValue();
    }
}
