package com.tomclaw.cache.demo;

import java.util.Random;

public class Randomizer {

    public static Random random = new Random(System.currentTimeMillis());

    public static String generateRandomString() {
        return generateRandomString(16);
    }

    public static String generateRandomString(int length) {
        return generateRandomString(random, length, length);
    }

    public static String generateRandomString(Random r, int minChars, int maxChars) {
        int wordLength = minChars;
        int delta = maxChars - minChars;
        if (delta > 0) {
            wordLength += r.nextInt(delta);
        }
        StringBuilder sb = new StringBuilder(wordLength);
        for (int i = 0; i < wordLength; i++) {
            char tmp = (char) ('a' + r.nextInt('z' - 'a'));
            sb.append(tmp);
        }
        return sb.toString();
    }

}
