package open.dolphin.util;

import open.dolphin.infomodel.IInfoModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

/// Utility class to handle MML Date format.
///
/// @author Kazushi Minagawa, Digital Globe, Inc.
/// @author pns
public final class MMLDate {

    private MMLDate() {
    }

    /// 時間なしの mmlDate 形式から Date を作る.
    /// ====> DateUtils.toLocalDate
    /// @param mmlDate 1975-01-01
    /// @return parsed Date
    public static Date getDateAsObject(String mmlDate) {
        if (mmlDate != null) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(IInfoModel.DATE_WITHOUT_TIME);
                return sdf.parse(mmlDate);

            } catch (ParseException e) {
                e.printStackTrace(System.err);
            }
        }
        return null;
    }

    /// 時間付きの mmlDate 形式から Date を作る.
    /// ====> DateUtils.toLocalDateTime
    /// @param mmlDate 1975-01-01T12:23:34
    /// @return parsed Date
    public static Date getDateTimeAsObject(String mmlDate) {
        if (mmlDate != null) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(IInfoModel.ISO_8601_DATE_FORMAT);
                return sdf.parse(mmlDate);

            } catch (ParseException e) {
                e.printStackTrace(System.err);
            }
        }
        return null;
    }

    /// Date から時間なしの mmlDate 形式を作る.
    /// ====> DateUtils.toIsoDate
    /// @param date Date
    /// @return 1975-01-01
    public static String getDateAsString(Date date) {
        return getDateAsFormatString(date, IInfoModel.DATE_WITHOUT_TIME);
    }

    /// Date から時間付きの mmlDate 形式を作る.
    ///
    /// @param date Date
    /// @return 1975-01-01T12:23:34
    public static String getDateTimeAsString(Date date) {
        return getDateAsFormatString(date, IInfoModel.ISO_8601_DATE_FORMAT);
    }

    /// Date から format で指定した形式の日付文字列を作る.
    ///
    /// @param date   Date
    /// @param format SimpleDateFormat string
    /// @return formatted string
    public static String getDateAsFormatString(Date date, String format) {
        if (date == null) return null;
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(date);
    }

    /// ORCA日付（20120401）を MMLフォーマット（2012-04-01）に変換.
    ///
    /// @param orcaDateString ORCA日付
    /// @return MML日付
    public static String toDolphinDateString(String orcaDateString) {
        if (orcaDateString == null || !orcaDateString.matches("[0-9]+")) {
            return null;
        }
        return String.join("-", orcaDateString.substring(0, 4), orcaDateString.substring(4, 6), orcaDateString.substring(6, 8));
    }

    /// ISO-DATE -> 元号変換の簡易呼び出し.
    ///
    /// @param isoDate ISO-DATE
    /// @return gengo date
    public static String toNengo(String isoDate) {
        return Gengo.isoDateToGengo(isoDate);
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

}
