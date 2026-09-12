package open.dolphin.util;

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
}
