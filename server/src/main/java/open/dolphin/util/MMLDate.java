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

    public static Date toDateFromLocalDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    public static Date toDateFromLocalDateTime(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
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
}
