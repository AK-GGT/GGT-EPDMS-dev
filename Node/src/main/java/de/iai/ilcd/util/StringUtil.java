package de.iai.ilcd.util;

public class StringUtil {

    public static String concat(String... strings) {
        StringBuilder b = new StringBuilder();

        for (int i = 0; i < strings.length; i++)
            b.append(strings[i]);

        return b.toString();
    }
}
