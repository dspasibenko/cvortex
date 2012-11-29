package org.cvortex.util;

public final class NullUtils {

    public static String reifyNull(String s) {
        return reifyNull(s, "");
    }

    public static String reifyNull(String s, String defaultValue) {
        return s == null ? defaultValue : s;
    }

    public static boolean hasText(String s) {
        if (s == null || s.length() == 0) {
            return false;
        }
        int strLen = s.length();
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(s.charAt(i))) {
                return true;
            }
        }
        return false;
    }
    
    public static String reifyEmptyString(String s) {
        return reifyEmptyString(s, "");
    }
    
    public static String reifyEmptyString(String s, String defaultValue) {
        return hasText(s) ? s : defaultValue;
    }

}
