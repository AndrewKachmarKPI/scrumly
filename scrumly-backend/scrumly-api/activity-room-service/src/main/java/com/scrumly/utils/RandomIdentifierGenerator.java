package com.scrumly.utils;

import java.security.SecureRandom;
import java.util.Random;

public class RandomIdentifierGenerator {
    private static final String LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final Random random = new SecureRandom();

    public static String generateRandomIssueIdentifier() {
        // Randomly decide whether to generate a 2 or 3 letter combination
        int length = random.nextInt(2) + 2;  // Generates 2 or 3
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < length; i++) {
            // Randomly pick a letter from the LETTERS string
            int randomIndex = random.nextInt(LETTERS.length());
            result.append(LETTERS.charAt(randomIndex));
        }

        return result.toString();
    }
}
