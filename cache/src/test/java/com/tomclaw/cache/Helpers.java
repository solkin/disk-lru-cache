package com.tomclaw.cache;

import java.util.Random;

class Helpers {

    private static final Random random = new Random(System.currentTimeMillis());

    static String randomString() {
        return randomString(16);
    }

    static String randomString(int length) {
        return randomString(random, length, length);
    }

    static String randomString(Random r, int minChars, int maxChars) {
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

    static long randomLong() {
        return random.nextLong();
    }

}
