package com.crediya.util;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

/** Console input with validation, so the menus stay short and readable. */
public final class InputHelper {
    private static final Scanner SC = new Scanner(System.in);

    private InputHelper() {}

    public static String readString(String prompt) {
        while (true) {
            System.out.print(prompt);
            String s = SC.nextLine().trim();
            if (!s.isEmpty()) return s;
            System.out.println("  ! The value cannot be empty.");
        }
    }

    public static int readInt(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            try {
                int v = Integer.parseInt(SC.nextLine().trim());
                if (v >= min && v <= max) return v;
                System.out.println("  ! Enter a number between " + min + " and " + max + ".");
            } catch (NumberFormatException e) {
                System.out.println("  ! Invalid number.");
            }
        }
    }

    /** Reads a decimal number (use a dot, e.g. 2.5) greater than min. */
    public static double readDouble(String prompt, double min) {
        while (true) {
            System.out.print(prompt);
            try {
                double v = Double.parseDouble(SC.nextLine().trim());
                if (v > min) return v;
                System.out.println("  ! The value must be greater than " + min + ".");
            } catch (NumberFormatException e) {
                System.out.println("  ! Invalid number (use a dot for decimals).");
            }
        }
    }

    /** Reads a date as yyyy-MM-dd; an empty answer returns the default value. */
    public static LocalDate readDate(String prompt, LocalDate defaultValue) {
        while (true) {
            System.out.print(prompt + " [yyyy-MM-dd, Enter = " + defaultValue + "]: ");
            String s = SC.nextLine().trim();
            if (s.isEmpty()) return defaultValue;
            try {
                return LocalDate.parse(s);
            } catch (DateTimeParseException e) {
                System.out.println("  ! Invalid date format.");
            }
        }
    }

    public static boolean confirm(String prompt) {
        System.out.print(prompt + " (y/n): ");
        return SC.nextLine().trim().equalsIgnoreCase("y");
    }

    public static void pause() {
        System.out.print("\nPress Enter to continue...");
        SC.nextLine();
    }
}
