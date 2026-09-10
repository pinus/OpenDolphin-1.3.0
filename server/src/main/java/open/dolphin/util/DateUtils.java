package open.dolphin.util;

import open.dolphin.infomodel.IInfoModel;
import org.jetbrains.annotations.NotNull;

import java.time.*;
import java.time.format.DateTimeFormatter;

/// DateUtils
/// - isoDate: yyyy-MM-dd
/// - isoTime: HH:mm:ss
/// - isoDateTime: yyyy-MM-dd'T'HH:mm:ss
///
/// @author pns
public class DateUtils {
    private static final String MIN_DATE = "1970-01-01'T'00:00:00";
    private static final String ISO_DATE = "yyyy-MM-dd";
    private static final String ISO_TIME = "HH:mm:ss";
    private static final String ISO_DATE_TIME = ISO_DATE + "'T'" + ISO_TIME;
    private static final DateTimeFormatter ISO_DATE_FORMATTER = DateTimeFormatter.ofPattern(ISO_DATE);
    private static final DateTimeFormatter ISO_TIME_FORMATTER = DateTimeFormatter.ofPattern(ISO_TIME);
    private static final DateTimeFormatter ISO_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(ISO_DATE_TIME);

    /// ISO-DATE または ISO-DATE-TIME 型式から LocalDateTime を作る.
    ///
    /// @param isoDateTime ISO-DATE (1975-01-01) or ISO-DATE-TIME (1975-01-01T12:23:34)
    /// @return parsed LocalDateTime
    public static LocalDateTime toLocalDateTime(String isoDateTime) {
        String target = isoDateTime.contains("T") ? isoDateTime : isoDateTime + "T00:00:00";
        return LocalDateTime.parse(target, ISO_DATE_TIME_FORMATTER);
    }

    /// ISO-DATE または ISO-DATE-TIME 型式から LocalDate を作る.
    ///
    /// @param isoDate ISO-DATE (1975-01-01) or ISO-DATE-TIME (1975-01-01T12:23:34)
    /// @return parsed LocalDate
    public static LocalDate toLocalDate(String isoDate) {
        String target = isoDate.contains("T") ? isoDate.substring(0, 10) : isoDate;
        return LocalDate.parse(target, DateTimeFormatter.ofPattern(ISO_DATE));
    }

    /// LocalDate から ISO-DATE 形式を作る.
    ///
    /// @param localDate LocalDate
    /// @return 1975-01-01
    public static String toIsoDate(LocalDate localDate) {
        return localDate.format(DateTimeFormatter.ofPattern(ISO_DATE));
    }

    /// LocalDateTime から ISO-DATE 形式を作る.
    ///
    /// @param localDateTime LocalDateTime
    /// @return 1975-01-01
    public static String toIsoDate(LocalDateTime localDateTime) {
        return localDateTime.format(DateTimeFormatter.ofPattern(ISO_DATE));
    }

    /// LocalDateTime から ISO-DATE-TIME 形式を作る.
    ///
    /// @param localDateTime LocalDateTime
    /// @return 1975-01-01T12:23:34
    public static String toIsoDateTime(LocalDateTime localDateTime) {
        return localDateTime.format(DateTimeFormatter.ofPattern(ISO_DATE_TIME));
    }

    /// 今日を ISO-DATE 型式の文字列で返す.
    ///
    /// @return formatted String
    public static String todayToIsoDate() {
        return LocalDateTime.now().format(ISO_DATE_FORMATTER);
    }

    /// 現在時間を ISO-TIME 型式の文字列で返す.
    ///
    /// @return formatted String
    public static String todayToIsoTime() {
        return LocalDateTime.now().format(ISO_TIME_FORMATTER);
    }

    /// 今日を ISO-DATE-TIME 型式の文字列で返す.
    ///
    /// @return formatted String
    public static String todayToIsoDateTime() {
        return LocalDateTime.now().format(ISO_DATE_TIME_FORMATTER);
    }

    /// 今日から days 日後の日付を ISO-DATE 型式で返す.
    ///
    /// @param days 日数
    /// @return ISO-DATE
    public static String getIsoDateDaysAhead(int days) {
        return LocalDateTime.now().plusDays(days).format(ISO_DATE_FORMATTER);
    }

    /// 今日から months ヶ月後の日付を ISO-DATE 型式で返す.
    ///
    /// @param months 月数
    /// @return ISO-DATE
    public static String getIsoDateMonthsAhead(int months) {
        return LocalDateTime.now().plusMonths(months).format(ISO_DATE_FORMATTER);
    }

    /// 今日から years 年後の日付を ISO-DATE 型式で返す.
    ///
    /// @param years 年数
    /// @return ISO-DATE
    public static String getIsoDateYearsAhead(int years) {
        return LocalDateTime.now().plusYears(years).format(ISO_DATE_FORMATTER);
    }

    /// ISO-DATE/ISO-DATE-TIME 形式から時間を取り除いて ISO-DATE を返す.
    ///
    /// @param isoDateTime ISO-DATE/ISO-DATE-TIME
    /// @return ISO-DATE
    public static String trimTime(@NotNull String isoDateTime) {
        int index = isoDateTime.indexOf('T');
        if (index > -1) {
            return isoDateTime.substring(0, index);
        } else {
            return isoDateTime;
        }
    }

    /// ISO-DATE/ISO-DATE-TIME 形式から時間を取り除いて ISO-TIME を返す.
    ///
    /// @param isoDateTime ISO-DATE/ISO-DATE-TIME
    /// @return ISO-TIME
    public static String trimDate(@NotNull String isoDateTime) {
        int index = isoDateTime.indexOf('T');
        if (index > -1) {
            return isoDateTime.substring(index + 1, index + 6); // THH:mm:ss -> HH:mm:ss
        } else {
            return isoDateTime;
        }
    }

    /// ISO-DATE 生年月日から年齢文字列を作る.
    ///
    /// @param isoBirthday 1975-01-01
    /// @return 32.10
    public static String toAge(String isoBirthday) {
        LocalDate birthDate = LocalDate.parse(isoBirthday);
        LocalDate today = LocalDate.now();
        Period period = Period.between(birthDate, today);
        int years = period.getYears();     // 年
        int months = period.getMonths();   // 月（0〜11）
        return String.format("%d.%d", years, months);
    }

    /// ISO-DATE 生年月日から年齢付きの形式を作る.
    ///
    /// @param isoBirthday 1975-01-01
    /// @return 32.10 歳 (S50-01-01)
    public static String toAgeBirthday(String isoBirthday) {
        String age = toAge(isoBirthday);
        return String.format("%s %s (%s)", age, IInfoModel.AGE, Gengo.isoDateToGengo(isoBirthday));
    }

    /// ORCA 形式 GYYMMDD を年号形式に.
    ///
    /// @param orcaBirthday 4220726
    /// @return h22-07-26
    public static String orcaDateToGengo(String orcaBirthday) {
        //元号
        String nengo = Gengo.gengoNumberToAlphabet(orcaBirthday.substring(0, 1));
        //年
        String y = orcaBirthday.substring(1, 3);
        String m = orcaBirthday.substring(3, 5);
        String d = orcaBirthday.substring(5, 7);

        return nengo.toLowerCase() + y + "-" + m + "-" + d;
    }

    /// Date の開始日の LocalDate.
    ///
    /// @return minimal date
    public static LocalDate getMinLocalDate() {
        return toLocalDate(MIN_DATE);
    }

    /// Date の開始日の LocalDateTime.
    ///
    /// @return minimal date
    public static LocalDateTime getMinLocalDateTime() {
        return toLocalDateTime(MIN_DATE);
    }

    public static void main(String[] arg) {
        IO.println(orcaDateToGengo("3300101"));
        IO.println(orcaDateToGengo("4300430"));
        IO.println(orcaDateToGengo("5010501"));
        IO.println(toLocalDateTime("1975-01-01"));
        IO.println(toLocalDateTime("1975-01-01T12:23:34"));
        IO.println(toIsoDate(LocalDateTime.now()));
        IO.println(toIsoDateTime(LocalDateTime.now()));
        IO.println(todayToIsoDate());
        IO.println(todayToIsoTime());
        IO.println(todayToIsoDateTime());
        IO.println(getIsoDateDaysAhead(365));
        IO.println(getIsoDateMonthsAhead(12));
        IO.println(getIsoDateYearsAhead(1));
    }
}
