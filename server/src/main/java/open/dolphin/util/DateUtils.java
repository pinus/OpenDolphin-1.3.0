package open.dolphin.util;

import open.dolphin.infomodel.IInfoModel;
import org.jetbrains.annotations.NotNull;

import java.time.*;
import java.time.format.DateTimeFormatter;

/// DateUtils
/// - isoDate: yyyy-MM-dd
/// - isoTime: HH:mm:ss
/// - isoDateTime: yyyy-MM-dd'T'HH:mm:ss
/// - gengoDate: Gyy-MM-dd
/// - orcaDate: yyyyMMdd
/// - orcaGengoDate: GyyMMdd
/// - karteDate: yyyy年M月d日(E) HH:mm
///
/// @author pns
public class DateUtils {
    private static final String MIN_DATE = "1970-01-01'T'00:00:00";
    public static final DateTimeFormatter ISO_DATE_FORMATTER = DateTimeFormatter.ofPattern(IInfoModel.ISO_DATE);
    public static final DateTimeFormatter ISO_TIME_FORMATTER = DateTimeFormatter.ofPattern(IInfoModel.ISO_TIME);
    public static final DateTimeFormatter ISO_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(IInfoModel.ISO_DATE_TIME);
    public static final DateTimeFormatter KARTE_DATE_FORMATTER = DateTimeFormatter.ofPattern(IInfoModel.KARTE_DATE);

    /// ISO-DATE または ISO-DATE-TIME 型式から LocalDateTime を作る.
    ///
    /// @param isoDateTime ISO-DATE (1975-01-01) or ISO-DATE-TIME (1975-01-01T12:23:34)
    /// @return parsed LocalDateTime
    public static LocalDateTime toLocalDateTimeFromIsoDateTime(String isoDateTime) {
        String target = isoDateTime.contains("T") ? isoDateTime : isoDateTime + "T00:00:00";
        return LocalDateTime.parse(target, ISO_DATE_TIME_FORMATTER);
    }

    /// ISO-DATE または ISO-DATE-TIME 型式から LocalDate を作る.
    ///
    /// @param isoDate ISO-DATE (1975-01-01) or ISO-DATE-TIME (1975-01-01T12:23:34)
    /// @return parsed LocalDate
    public static LocalDate toLocalDateFromIsoDate(String isoDate) {
        String target = isoDate.contains("T") ? isoDate.substring(0, 10) : isoDate;
        return LocalDate.parse(target, ISO_DATE_FORMATTER);
    }

    /// LocalDate から ISO-DATE 形式を作る.
    ///
    /// @param localDate LocalDate
    /// @return 1975-01-01
    public static String toIsoDateFromLocalDate(LocalDate localDate) {
        return localDate.format(ISO_DATE_FORMATTER);
    }

    /// LocalDate から KARTE-DATE 形式を作る.
    ///
    /// @param localDate LocalDate
    /// @return 1975-01-01
    public static String toKarteDateFromLocalDate(LocalDate localDate) {
        return localDate.format(KARTE_DATE_FORMATTER);
    }

    /// LocalDateTime から ISO-DATE 形式を作る.
    ///
    /// @param localDateTime LocalDateTime
    /// @return 1975-01-01
    public static String toIsoDateFromLocalDateTime(LocalDateTime localDateTime) {
        return localDateTime.format(ISO_DATE_FORMATTER);
    }

    /// LocalDateTime から ISO-DATE-TIME 形式を作る.
    ///
    /// @param localDateTime LocalDateTime
    /// @return 1975-01-01T12:23:34
    public static String toIsoDateTimeFromLocalDateTime(LocalDateTime localDateTime) {
        return localDateTime.format(ISO_DATE_TIME_FORMATTER);
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
    public static String toAgeFromIsoBirthday(String isoBirthday) {
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
    public static String toAgeBirthdayFromIsoBirthday(String isoBirthday) {
        String age = toAgeFromIsoBirthday(isoBirthday);
        return String.format("%s %s (%s)", age, IInfoModel.AGE, Gengo.isoDateToGengo(isoBirthday));
    }

    /// orcaDate（20120401）を ISO-DATE（2012-04-01）に変換.
    ///
    /// @param orcaDate ORCA日付
    /// @return ISO-DATE
    public static String toIsoDateFromOrcaDate(String orcaDate) {
        return orcaDate == null || !orcaDate.matches("[0-9]+")
                ? null
                : String.join("-", orcaDate.substring(0, 4), orcaDate.substring(4, 6), orcaDate.substring(6, 8));
    }

    /// orcaGengoDate (GYYMMDD) を元号形式 (gengoDate) に.
    ///
    /// @param orcaGengoDate 4220726
    /// @return gengoDate h22-07-26
    public static String toGengoDateFromOrcaGengoDate(String orcaGengoDate) {
        //元号
        String gengo = Gengo.gengoNumberToAlphabet(orcaGengoDate.substring(0, 1));
        //年
        String y = orcaGengoDate.substring(1, 3);
        String m = orcaGengoDate.substring(3, 5);
        String d = orcaGengoDate.substring(5, 7);

        return gengo.toLowerCase() + y + "-" + m + "-" + d;
    }

    /// ISO-DATE を元号形式 (gengoDate) に.
    ///
    /// @param isoDate ISO-DATE
    /// @return gengoDate h22-07-26
    public static String toGengoDateFromIsoDate(String isoDate) {
        return Gengo.isoDateToGengo(isoDate);
    }

    /// Date の開始日の LocalDate.
    ///
    /// @return minimal date
    public static LocalDate getMinLocalDate() {
        return toLocalDateFromIsoDate(MIN_DATE);
    }

    /// Date の開始日の LocalDateTime.
    ///
    /// @return minimal date
    public static LocalDateTime getMinLocalDateTime() {
        return toLocalDateTimeFromIsoDateTime(MIN_DATE);
    }

    public static void main(String[] arg) {
        IO.println(toGengoDateFromOrcaGengoDate("3300101"));
        IO.println(toGengoDateFromOrcaGengoDate("4300430"));
        IO.println(toGengoDateFromOrcaGengoDate("5010501"));
        IO.println(toLocalDateTimeFromIsoDateTime("1975-01-01"));
        IO.println(toLocalDateTimeFromIsoDateTime("1975-01-01T12:23:34"));
        IO.println(toIsoDateFromLocalDateTime(LocalDateTime.now()));
        IO.println(toIsoDateTimeFromLocalDateTime(LocalDateTime.now()));
        IO.println(todayToIsoDate());
        IO.println(todayToIsoTime());
        IO.println(todayToIsoDateTime());
        IO.println(getIsoDateDaysAhead(365));
        IO.println(getIsoDateMonthsAhead(12));
        IO.println(getIsoDateYearsAhead(1));
    }
}
