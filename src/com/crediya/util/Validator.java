package com.crediya.util;

import java.util.regex.Pattern;

/** Small reusable validation rules. */
public final class Validator {
    private static final Pattern EMAIL = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");

    private Validator() {}

    public static boolean isValidEmail(String s)    { return s != null && EMAIL.matcher(s).matches(); }
    public static boolean isValidDocument(String s) { return s != null && s.matches("\\d{5,15}"); }
    public static boolean isValidPhone(String s)    { return s != null && s.matches("\\d{7,15}"); }
}
