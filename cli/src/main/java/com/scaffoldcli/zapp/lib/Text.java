package com.scaffoldcli.zapp.lib;

import java.util.Scanner;

public class Text {
    private static final String ENDC = "\033[0m";
    private static final String BOLD = "\033[1m";

    public static String input(String prompt) {
        Scanner scanner = new Scanner(System.in);
        Text.print(prompt, true);

        String input = scanner.nextLine();
        while (input.isBlank())
            input = scanner.nextLine();

        return input;
    }

    public static void print(String text, String textColour) {
        text = Text.style(text, textColour) + ENDC;
        System.out.println(text);
    }

    public static void print(String text) {
        System.out.println(text);
    }

    public static void print(String text, boolean bold) {
        text = BOLD + text + ENDC;
        System.out.println(text);
    }

    public static void print(String text, String textColour, boolean bold) {
        text = BOLD + Text.style(text, textColour) + ENDC;
        System.out.println(text);
    }

    public static void separator(String colour, int size) {
        if (size < 10) size = 10;
        String text = Text.style(String.valueOf('-').repeat(size), colour);
        Text.print(text, colour);
    }

    public static void separator(String colour, int size, boolean bold) {
        if (size < 10) size = 10;
        String text = Text.style(String.valueOf('-').repeat(size), colour);
        Text.print(text, colour, bold);
    }

    public static void separator(String colour, int size, char symbol) {
        if (size < 10) size = 10;
        String text = Text.style(String.valueOf(symbol).repeat(size), colour);
        Text.print(text, colour);
    }

    public static void separator(String colour, int size, char symbol, boolean bold) {
        if (size < 10) size = 10;
        String text = Text.style(String.valueOf(symbol).repeat(size), colour);
        Text.print(text, colour, bold);
    }

    public static String style(String text, String textColour) {
        return textColour + text + ENDC;
    }

    public static String style(String text, String textColour, boolean bold) {
        if (bold) {
            return BOLD + textColour + text + ENDC;
        } else {
            return textColour + text + ENDC;

        }
    }

    public static class Colour {
        public static final String DEFAULT = "\033[39m";
        public static final String black = "\033[30m";
        public static final String white = "\033[37m";
        public static final String bright_white = "\033[97m";
        public static final String red = "\033[31m";
        public static final String bright_red = "\033[91m";
        public static final String green = "\033[32m";
        public static final String bright_green = "\033[92m";
        public static final String yellow = "\033[33m";
        public static final String bright_yellow = "\033[93m";
        public static final String blue = "\033[34m";
        public static final String bright_blue = "\033[94m";
        public static final String magenta = "\033[35m";
        public static final String bright_magenta = "\033[95m";
        public static final String cyan = "\033[36m";
        public static final String bright_cyan = "\033[96m";
    }
}