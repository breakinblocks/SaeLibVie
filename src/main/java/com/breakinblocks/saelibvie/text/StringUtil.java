package com.breakinblocks.saelibvie.text;

import net.minecraft.util.StringRepresentable;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public final class StringUtil {
    public static final String ALLOWED_TEXT_CHARS = " .-_!@#$%^&*()+=\\/,<>?'\"[]{}|;:`~";
    public static final char FORMATTING_CHAR = '§';
    public static final String HEX = "0123456789ABCDEF";
    public static final Predicate<String> ALWAYS_TRUE = s -> true;
    public static final DecimalFormat DOUBLE_FORMATTER_00 = new DecimalFormat("#0.00", DecimalFormatSymbols.getInstance(Locale.ROOT));
    public static final DecimalFormat DOUBLE_FORMATTER_0 = new DecimalFormat("#0.0", DecimalFormatSymbols.getInstance(Locale.ROOT));
    public static final int[] INT_SIZE_TABLE = {9, 99, 999, 9999, 99999, 999999, 9999999, 99999999, 999999999, Integer.MAX_VALUE};
    public static final Map<String, String> TEMP_MAP = new HashMap<>();
    public static boolean ignoreIdentifierErrors = false;

    public static final int ALLOW_EMPTY = 1;
    public static final int FIX = 2;
    public static final int ONLY_LOWERCASE = 4;
    public static final int ONLY_UNDERLINE = 8;
    public static final int ONLY_UNDERLINE_OR_PERIOD = ONLY_UNDERLINE | 16;
    public static final int DEFAULTS = FIX | ONLY_LOWERCASE | ONLY_UNDERLINE;

    public static final Comparator<Object> IGNORE_CASE_COMPARATOR = (a, b) -> String.valueOf(a).compareToIgnoreCase(String.valueOf(b));
    public static final Comparator<Object> ID_COMPARATOR = (a, b) -> getID(a, FIX).compareToIgnoreCase(getID(b, FIX));

    private static final Pattern FORMATTING_PATTERN = Pattern.compile("(?i)[&§]([0-9A-FK-OR])");

    static {
        DOUBLE_FORMATTER_00.setRoundingMode(RoundingMode.DOWN);
        DOUBLE_FORMATTER_0.setRoundingMode(RoundingMode.DOWN);
    }

    private StringUtil() {
    }

    public static String unformatted(String s) {
        return FORMATTING_PATTERN.matcher(s).replaceAll("");
    }

    public static String addFormatting(String s) {
        return FORMATTING_PATTERN.matcher(s).replaceAll(FORMATTING_CHAR + "$1");
    }

    public static String toSnakeCase(String s) {
        String lower = unformatted(s).toLowerCase(Locale.ROOT);
        StringBuilder out = new StringBuilder(lower.length());
        boolean lastUnderscore = false;
        for (int i = 0; i < lower.length(); i++) {
            char c = lower.charAt(i);
            boolean ok = (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || c == '_';
            char emit = ok ? c : '_';
            if (emit == '_') {
                if (lastUnderscore) continue;
                lastUnderscore = true;
            } else {
                lastUnderscore = false;
            }
            out.append(emit);
        }
        return out.toString();
    }

    public static String emptyIfNull(Object o) {
        return o == null ? "" : String.valueOf(o);
    }

    public static String getRawID(Object o) {
        if (o instanceof StringRepresentable representable) {
            return representable.getSerializedName();
        }
        if (o instanceof Enum<?> e) {
            return e.name();
        }
        return String.valueOf(o);
    }

    public static String getID(Object o, int flags) {
        String id = getRawID(o);
        if (flags == 0) return id;
        boolean fix = getFlag(flags, FIX);
        if (!fix && id.isEmpty() && !getFlag(flags, ALLOW_EMPTY)) {
            throw new NullPointerException("ID can't be empty!");
        }
        if (getFlag(flags, ONLY_LOWERCASE)) {
            if (fix) {
                id = id.toLowerCase(Locale.ROOT);
            } else if (!id.equals(id.toLowerCase(Locale.ROOT))) {
                throw new IllegalArgumentException("ID must be lowercase!");
            }
        }
        if (getFlag(flags, ONLY_UNDERLINE)) {
            if (fix) {
                id = id.toLowerCase(Locale.ROOT);
            } else if (!id.equals(id.toLowerCase(Locale.ROOT))) {
                throw new IllegalArgumentException("ID must be lowercase!");
            }
            boolean allowPeriod = getFlag(flags, 16);
            char[] chars = id.toCharArray();
            for (int i = 0; i < chars.length; i++) {
                char c = chars[i];
                boolean valid = (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || (allowPeriod && c == '.');
                if (!valid) {
                    if (fix) {
                        chars[i] = '_';
                    } else {
                        throw new IllegalArgumentException("ID contains invalid character: '" + c + "'!");
                    }
                }
            }
            id = new String(chars);
        }
        return id;
    }

    private static boolean getFlag(int flags, int flag) {
        return (flags & flag) != 0;
    }

    public static String[] shiftArray(String[] array) {
        if (array == null || array.length <= 1) return new String[0];
        String[] out = new String[array.length - 1];
        System.arraycopy(array, 1, out, 0, out.length);
        return out;
    }

    public static boolean isASCIIChar(char c) {
        return c > 0 && c < 256;
    }

    public static boolean isTextChar(char c, boolean onlyAZ09) {
        if (!isASCIIChar(c)) return false;
        if ((c >= '0' && c <= '9') || (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) return true;
        return !onlyAZ09 && ALLOWED_TEXT_CHARS.indexOf(c) >= 0;
    }

    public static void replace(List<String> list, String from, String to) {
        for (int i = 0; i < list.size(); i++) {
            if (from.equals(list.get(i))) {
                list.set(i, to);
            }
        }
    }

    public static String replace(String s, char from, char to) {
        return s.replace(from, to);
    }

    public static String joinSpaceUntilEnd(int start, String[] parts) {
        if (start < 0 || start >= parts.length) return "";
        StringBuilder out = new StringBuilder();
        for (int i = start; i < parts.length; i++) {
            if (i > start) out.append(' ');
            out.append(parts[i]);
        }
        return out.toString();
    }

    public static String firstUppercase(String s) {
        if (s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    public static String fillString(String s, char fill, int length) {
        StringBuilder out = new StringBuilder(s);
        while (out.length() < length) {
            out.append(fill);
        }
        return out.toString();
    }

    public static String removeAllWhitespace(String s) {
        StringBuilder out = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c > ' ') out.append(c);
        }
        return out.toString();
    }

    public static String formatDouble0(double value) {
        if (value == Math.rint(value) && !Double.isInfinite(value)) {
            return String.format(Locale.ROOT, "%,d", (long) value);
        }
        return DOUBLE_FORMATTER_0.format(value);
    }

    public static String formatDouble00(double value) {
        if (value == Math.rint(value) && !Double.isInfinite(value)) {
            return String.format(Locale.ROOT, "%,d", (long) value);
        }
        return DOUBLE_FORMATTER_00.format(value);
    }

    public static String formatDouble(double value) {
        return formatDouble(value, false);
    }

    public static String formatDouble(double value, boolean fancy) {
        if (Double.isNaN(value)) return "NaN";
        if (value == Double.POSITIVE_INFINITY) return "+Inf";
        if (value == Double.NEGATIVE_INFINITY) return "-Inf";
        if (value == Long.MAX_VALUE) return "2^63-1";
        if (value == Long.MIN_VALUE) return "-2^63";
        if (value == 0D) return "0";
        if (!fancy) return formatDouble00(value);
        if (value >= 1_000_000_000D) return formatDouble00(value / 1_000_000_000D) + "B";
        if (value >= 1_000_000D) return formatDouble00(value / 1_000_000D) + "M";
        if (value >= 10_000D) return formatDouble00(value / 1_000D) + "K";
        return formatDouble00(value);
    }

    public static Map<String, String> parse(Map<String, String> map, String s) {
        if (map == TEMP_MAP) {
            map.clear();
        }
        if (s.isEmpty()) return map;
        for (String entry : s.split(",")) {
            int eq = entry.indexOf('=');
            if (eq < 0) throw new IllegalArgumentException("Missing '=' in '" + entry + "'");
            String value = entry.substring(eq + 1);
            for (String key : entry.substring(0, eq).split("&")) {
                map.put(key, value);
            }
        }
        return map;
    }

    public static String fixTabs(String s, int tabSize) {
        return s.replace("\t", " ".repeat(Math.max(0, tabSize)));
    }

    public static int stringSize(int x) {
        for (int i = 0; ; i++) {
            if (x <= INT_SIZE_TABLE[i]) return i + 1;
        }
    }

    public static String add0s(int n, int max) {
        String s = Integer.toString(n);
        int digits = stringSize(max);
        StringBuilder out = new StringBuilder();
        for (int i = s.length(); i < digits; i++) out.append('0');
        return out.append(s).toString();
    }

    public static String camelCaseToWords(String s) {
        StringBuilder out = new StringBuilder(s.length() + 4);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (i > 0 && Character.isUpperCase(c) && !Character.isUpperCase(s.charAt(i - 1))) {
                out.append(' ');
            }
            out.append(i == 0 ? Character.toUpperCase(c) : c);
        }
        return out.toString();
    }

    public static Map<String, String> splitProperties(String s) {
        Map<String, String> map = new LinkedHashMap<>();
        for (String part : s.split(" ")) {
            if (part.isEmpty()) continue;
            int colon = part.indexOf(':');
            if (colon < 0) {
                map.put(part, "");
            } else {
                map.put(part.substring(0, colon), part.substring(colon + 1).replace("%20", " "));
            }
        }
        return map;
    }
}
