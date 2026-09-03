package com.breakinblocks.saelibvie.text;

import java.util.Locale;

public final class TimeUtil {
    private TimeUtil() {
    }

    public static String getTimeString(long millis) {
        StringBuilder out = new StringBuilder();
        if (millis < 0) {
            out.append('-');
            millis = -millis;
        }
        if (millis < 1000L) {
            return out.append(millis).append("ms").toString();
        }
        long seconds = millis / 1000L;
        long days = seconds / 86400L;
        long hours = (seconds / 3600L) % 24L;
        long minutes = (seconds / 60L) % 60L;
        long secs = seconds % 60L;
        if (days > 0) {
            out.append(days).append("d ");
        }
        if (hours > 0 || days > 0) {
            out.append(String.format(Locale.ROOT, "%02d", hours)).append(':');
        }
        out.append(String.format(Locale.ROOT, "%02d", minutes)).append(':');
        out.append(String.format(Locale.ROOT, "%02d", secs));
        return out.toString();
    }

    public static String prettyTimeString(long seconds) {
        if (seconds <= 0) return "0 seconds";
        StringBuilder out = new StringBuilder();
        long rest;
        if (seconds < 60L) {
            return unit(seconds, "second");
        } else if (seconds < 3600L) {
            long minutes = seconds / 60L;
            rest = seconds % 60L;
            out.append(unit(minutes, "minute"));
            if (rest > 0) out.append(" and ").append(unit(rest, "second"));
        } else if (seconds < 86400L) {
            long hours = seconds / 3600L;
            rest = (seconds % 3600L) / 60L;
            out.append(unit(hours, "hour"));
            if (rest > 0) out.append(" and ").append(unit(rest, "minute"));
        } else {
            long days = seconds / 86400L;
            rest = (seconds % 86400L) / 3600L;
            out.append(unit(days, "day"));
            if (rest > 0) out.append(" and ").append(unit(rest, "hour"));
        }
        return out.toString();
    }

    private static String unit(long value, String name) {
        return value + " " + name + (value == 1 ? "" : "s");
    }
}
