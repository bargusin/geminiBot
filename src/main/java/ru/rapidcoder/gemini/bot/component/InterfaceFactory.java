package ru.rapidcoder.gemini.bot.component;

public class InterfaceFactory {

    public static String format(String text) {
        return text;
    }

    public static KeyboardButton createButton(String text, String callbackData) {
        return new KeyboardButton(text, callbackData);
    }
}
