package de.iai.ilcd.webgui.controller.admin;

import java.util.Random;

/**
 * Utility class for generation of random string passwords
 */
public class RandomPassword {

    /**
     * The possible characters for the password to create
     */
    private static final String ALPHABET = "!0123456789ABCDEFGHIJKLMNOPQRSTUVWXYzabcdefghijklmnopqrstuvwxyz";

    /**
     * Get a random password
     *
     * @param length count of characters
     * @return created password with specified length
     */
    public static String getPassword(int length) {
        Random rand = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int pos = rand.nextInt(RandomPassword.ALPHABET.length());
            sb.append(RandomPassword.ALPHABET.charAt(pos));
        }
        return sb.toString();
    }

}
