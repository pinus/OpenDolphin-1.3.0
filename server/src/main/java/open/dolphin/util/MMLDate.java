package open.dolphin.util;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.GregorianCalendar;

/// Utility class to handle MML Date format.
///
/// @author Kazushi Minagawa, Digital Globe, Inc.
/// @author pns
public final class MMLDate {

    private MMLDate() {
    }

    /// GregorianCalendar の日付を SimpleDateFormat の pattern 形式で文字列にして返す.
    ///
    /// @param gc
    /// @param pattern
    /// @return
    public static String getDateTime(GregorianCalendar gc, String pattern) {
        SimpleDateFormat f = new SimpleDateFormat(pattern);
        return f.format(gc.getTime());
    }

    /// GregorianCalendar の日付を，時間なしの mmlDate 形式 (yyyy-MM-dd) で返す.
    ///
    /// @param gc
    /// @return
    public static String getDate(GregorianCalendar gc) {
        return getDateTime(gc, "yyyy-MM-dd");
    }

    /// Date 型式の日付を，時間なしの mmlDate 形式 (yyyy-MM-dd) で返す.
    ///
    /// @param date
    /// @return
    public static String getDate(Date date) {
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(date);
        return getDate(gc);
    }

    /// LocalDate 型式の日付を，時間なしの mmlDate 形式 (yyyy-MM-dd) で返す.
    ///
    /// @param date
    /// @return
    public static String getDate(LocalDate date) {
        return date.toString();
    }

    /// 今日を時間なしの mmlDate 形式 (yyyy-MM-dd) で返す.
    ///
    /// @return
    public static String getDate() {
        return LocalDate.now().toString();
    }

    /// GregorianCalendar の日付を，日付なしの mmlDate 形式 (HH:mm:ss) で返す.
    ///
    /// @param gc
    /// @return
    public static String getTime(GregorianCalendar gc) {
        return getDateTime(gc, "HH:mm:ss");
    }

    /// 今日の時間だけを mmlDate 形式 (HH:mm:ss) で返す.
    ///
    /// @return
    public static String getTime() {
        return LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }
}
