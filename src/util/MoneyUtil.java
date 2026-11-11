package util;

import java.text.NumberFormat;
import java.util.Locale;

public final class MoneyUtil {
    private MoneyUtil() { }
    public static String format(long amount) {
        return NumberFormat.getNumberInstance(Locale.US).format(amount) + " VND";
    }
}
