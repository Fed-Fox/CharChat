package ru.fedfox.charchat.utils;

public class TextValidator {

    public static boolean textValidationPassword(String text) {
        return text.matches("^[a-zA-Z0-9_-]+") && text.length() >= 8;
    }

    public static boolean textValidationMessage(String text) {
        return !text.matches("^[^<>'\"]*$");
    }

    public static boolean textValidationEmail(String text) {
        return text.matches("^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$");
    }

    public static boolean textValidationDisplayName(String text) {
        return text.matches("^[a-zA-Z0-9_-]+") && text.length() >= 6;
    }

    public static boolean textValidationTag(String text) {
        return text.matches("^[a-z0-9_-]+") && text.length() >= 5;
    }
}
