package util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public final class TimeUtil {
    public static final ZoneId ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    public static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private TimeUtil() { }

    public static String format(long epochMillis) {
        return FORMAT.format(Instant.ofEpochMilli(epochMillis).atZone(ZONE));
    }

    public static long parse(String text) {
        try {
            return LocalDateTime.parse(text.trim(), FORMAT).atZone(ZONE).toInstant().toEpochMilli();
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Ngày giờ không hợp lệ. Định dạng: dd/MM/yyyy HH:mm");
        }
    }
}
