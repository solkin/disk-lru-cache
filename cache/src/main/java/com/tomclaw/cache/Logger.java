package com.tomclaw.cache;

class Logger {

    private static final boolean LOGGING = false;

    static void log(String format, Object... args) {
        if (LOGGING) {
            System.out.println(String.format(format, args));
        }
    }

}
