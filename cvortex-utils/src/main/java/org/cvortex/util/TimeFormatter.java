package org.cvortex.util;

public final class TimeFormatter {

    private TimeFormatter() {
        throw new AssertionError(getClass().getSimpleName() + " cannot be instantiated.");
    }

    public static String toHostTimeView(long timeMs) {
        return String.format(" %1$tH:%1$tM:%1$tS,%1$tL (%1$d) ", timeMs);
    }

    public static String toHostDateTimeView(long timeMs) {
        return String.format(" %1$tc (%1$d) ", timeMs);
    }

    public static String toGMTTimeView(long timeMs) {
        return String.format(" %02d:%02d:%02d,%03d GMT+0", getHours(timeMs), getMinutes(timeMs), getSeconds(timeMs), getMillis(timeMs));
}
    
    public static String toHumanView(long millis) {
        if (millis < 1000L) {
            return millis + " ms";
        }
        if (millis < 60000L) {
            return String.format("%02d.%03d sec.", getSeconds(millis), getMillis(millis));
        }
        if (millis < 3600000L) {
            return String.format("%02d:%02d.%03d min.", getMinutes(millis), getSeconds(millis), getMillis(millis));
        }
        if (millis < 86400000L) {
            return String.format("%02d:%02d:%02d", getHours(millis), getMinutes(millis), getSeconds(millis));
        }
        return String.format("%dd-%02d:%02d:02d", getDays(millis), getHours(millis), getMinutes(millis),
                getSeconds(millis));
    }

    private static long getMillis(long timeMs) {
        return timeMs % 1000L;
    }

    private static long getSeconds(long timeMs) {
        return (timeMs / 1000L) % 60L;
    }

    private static long getMinutes(long timeMs) {
        return (timeMs / 60000L) % 60L;
    }

    private static long getHours(long timeMs) {
        return (timeMs / 3600000L) % 24L;
    }

    private static long getDays(long timeMs) {
        return timeMs / 86400000L;
    }
}
