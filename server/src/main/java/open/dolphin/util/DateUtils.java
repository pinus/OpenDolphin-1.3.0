package open.dolphin.util;

import open.dolphin.infomodel.IInfoModel;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

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
    private static final String MIN_DATE = "1970-01-01T00:00:00";
    public static final DateTimeFormatter ISO_DATE_FORMATTER = DateTimeFormatter.ofPattern(IInfoModel.ISO_DATE);
    public static final DateTimeFormatter ISO_TIME_FORMATTER = DateTimeFormatter.ofPattern(IInfoModel.ISO_TIME);
    public static final DateTimeFormatter ISO_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(IInfoModel.ISO_DATE_TIME);
    public static final DateTimeFormatter KARTE_DATE_FORMATTER = DateTimeFormatter.ofPattern(IInfoModel.KARTE_DATE, Locale.JAPAN); // yyyy年M月d日(E) HH:mm
    public static final DateTimeFormatter ORCA_DATE_FORMATTER = DateTimeFormatter.ofPattern(IInfoModel.ORCA_DATE); // yyyyMMdd

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

    static void main(String[] arg) {
        IO.println("1." + toGengoDateFromOrcaGengoDate("3300101"));
        IO.println("2." + toGengoDateFromOrcaGengoDate("4300430"));
        IO.println("3." + toGengoDateFromOrcaGengoDate("5010501"));
        IO.println("4." + toLocalDateFromIsoDate("1975-01-01"));
        IO.println("5." + toLocalDateTimeFromIsoDateTime("1975-01-01"));
        IO.println("6." + toLocalDateFromIsoDate("1975-01-01T12:23:34"));
        IO.println("7." + toLocalDateTimeFromIsoDateTime("1975-01-01T12:23:34"));
        IO.println("8." + LocalDate.now().format(ISO_DATE_FORMATTER));
        IO.println("9." + LocalDateTime.now().format(ISO_TIME_FORMATTER));
        IO.println("10." + LocalDateTime.now().format(ISO_DATE_TIME_FORMATTER));
        IO.println("11." + LocalDateTime.now().plusYears(1).format(ISO_DATE_FORMATTER));
        IO.println("12." + LocalDateTime.now().plusMonths(12).format(ISO_DATE_FORMATTER));
        IO.println("13." + LocalDateTime.now().plusDays(365).format(ISO_DATE_FORMATTER));
    }
}
