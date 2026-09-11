package open.dolphin.util;

import open.dolphin.infomodel.IInfoModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/// Utility class to handle MML Date format.
///
/// @author Kazushi Minagawa, Digital Globe, Inc.
/// @author pns
public final class MMLDate {

    private MMLDate() {
    }

    // transition bridge
    public static LocalDate toLocalDateFromDate(Date date) {
        LocalDate localDate = date.toInstant()
                .atZone(ZoneId.systemDefault()).toLocalDate();
        return localDate;
    }
    // transition bridge
    public static LocalDateTime toLocalDateTimeFromDate(Date date) {
        LocalDateTime localDateTime = date.toInstant()
                .atZone(ZoneId.systemDefault()).toLocalDateTime();
        return localDateTime;
    }

    /// 時間なしの mmlDate 形式から Date を作る.
    /// ====> DateUtils.toLocalDate
    /// @param mmlDate 1975-01-01
    /// @return parsed Date
    public static Date getDateAsObject(String mmlDate) {
        if (mmlDate != null) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(IInfoModel.ISO_DATE);
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
                SimpleDateFormat sdf = new SimpleDateFormat(IInfoModel.ISO_DATE_TIME);
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
        SimpleDateFormat sdf = new SimpleDateFormat(IInfoModel.ISO_DATE);
        return sdf.format(date);
    }

    /// Date から時間付きの mmlDate 形式を作る.
    ///
    /// @param date Date
    /// @return 1975-01-01T12:23:34
    public static String getDateTimeAsString(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat(IInfoModel.ISO_DATE_TIME);
        return sdf.format(date);
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
}
