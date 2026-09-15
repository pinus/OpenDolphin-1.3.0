package open.dolphin.calendar;

import java.awt.*;
import java.util.stream.Stream;

/// CalendarEvent.
///
/// @author pns
public enum CalendarEvent {
    TODAY("今日", new Color(0x00947a)),
    PVT("受診日", new Color(0xa5c9c1)),
    BIRTHDAY("誕生日", new Color(0xd70035)),

    EXAM_APPO("再診", new Color(0xa5c9c1)),
    IMAGE_APPO("画像検査", new Color(0xd1bada)),
    TEST_APPO("検体検査", new Color(0xe6bfb2)),
    MISC_APPO("その他", new Color(0xabb1ad)),

    medOrder("処方", Color.PINK),
    treatmentOrder("処置", Color.PINK),
    instructionChargeOrder("指導", Color.PINK),
    testOrder("ラボテスト", Color.PINK),
    physiologyOrder("生体検査", Color.PINK),
    radiologyOrder("放射線", Color.PINK), image("画像", Color.PINK);

    private final Color color;
    private final String title;

     CalendarEvent(String t, Color c) {
        title = t;
        color = c;
    }

    /**
     * Returns the color associated with a specific calendar event code.
     *
     * @param code The code of the calendar event.
     * @return The color associated with the specified calendar event code, or null if no match is found.
     */
    public static Color getColor(String code) {
        for (CalendarEvent event : CalendarEvent.values()) {
            if (event.name().equals(code)) {
                return event.color();
            }
        }
        return null;
    }

    /**
     * Returns the title associated with a specific calendar event code.
     *
     * @param code The code of the calendar event.
     * @return The title associated with the specified calendar event code or the input code if no match is found.
     */
    public static String getTitle(String code) {
        for (CalendarEvent event : CalendarEvent.values()) {
            if (event.name().equals(code)) {
                return event.title();
            }
        }
        return code;
    }

    /**
     * Returns the code associated with a specific calendar event title.
     *
     * @param title The title of the calendar event.
     * @return The code associated with the specified calendar event title or the input title if no match is found.
     */
    public static String getCode(String title) {
        for (CalendarEvent event : CalendarEvent.values()) {
            if (event.title().equals(title)) {
                return event.name();
            }
        }
        return title;
    }

    /**
     * Checks if the given code corresponds to an appointment-related event.
     *
     * @param code The code of the calendar event to check.
     * @return true if the code is associated with an appointment-related event, false otherwise.
     */
    public static boolean isAppoint(String code) {
        return Stream.of(CalendarEvent.values())
                .filter(event -> event.name().contains("APPO"))
                .anyMatch(event -> event.code().equals(code));
    }

    /**
     * Checks if the given code corresponds to a module-related event.
     *
     * @param code The code of the calendar event to check.
     * @return true if the code is associated with a module-related event, false otherwise.
     */
    public static boolean isModule(String code) {
        return Stream.of(CalendarEvent.values())
                .filter(event -> event.name().contains("Order"))
                .anyMatch(event -> event.code().equals(code));
    }

    public String code() {
        return name();
    }

    public Color color() {
        return color;
    }

    public String title() {
        return title;
    }

    @Override
    public String toString() {
        return title;
    }
}
