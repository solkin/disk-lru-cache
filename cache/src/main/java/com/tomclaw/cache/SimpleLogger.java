package com.tomclaw.cache;

public class SimpleLogger implements Logger {

    private final boolean isLoggingEnabled;

    public SimpleLogger(Boolean isLoggingEnabled) {
        this.isLoggingEnabled = isLoggingEnabled;
    }

    public void log(String format, Object... args) {
        if (isLoggingEnabled) {
            System.out.printf((format) + "%n", args);
        }
    }

}
