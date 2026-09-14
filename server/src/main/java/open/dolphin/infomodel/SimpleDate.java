package open.dolphin.infomodel;

import java.time.LocalDate;

/// SimpleDate.
///
/// @author Kazushi Minagawa
public class SimpleDate extends InfoModel implements Comparable<SimpleDate> {
    private final LocalDate localDate;
    private String eventCode;

    public SimpleDate(int year, int month, int day) {
        localDate = LocalDate.of(year, month, day);
    }

    public SimpleDate(LocalDate localDate) {
        this.localDate = localDate;
    }

    public SimpleDate(String mmlDate) {
        // mmlDate = YYYY-MM-DDThh:mm:ss
        int year = Integer.parseInt(mmlDate.substring(0, 4));
        int month = Integer.parseInt(mmlDate.substring(5, 7));
        int date = Integer.parseInt(mmlDate.substring(8, 10));
        this(year, month, date);
    }

    public LocalDate toLocalDate() {
        return localDate;
    }

    public String toIsoDate() {
        return localDate.toString();
    }

    public int getYear() {
        return localDate.getYear();
    }

    public int getMonth() {
        return localDate.getMonthValue();
    }

    public int getDay() {
        return localDate.getDayOfMonth();
    }

    public String getEventCode() {
        return eventCode;
    }

    @Override
    public String toString() {
        return String.valueOf(getDay());
    }

    public void setEventCode(String c) {
        this.eventCode = c;
    }

    @Override
    public int compareTo(SimpleDate other) {
        return localDate.compareTo(other.toLocalDate());
    }

    @Override
    public boolean equals(Object other) {
        return (other instanceof SimpleDate) && compareTo((SimpleDate) other) == 0;
    }
}
