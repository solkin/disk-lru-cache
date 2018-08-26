package com.tomclaw.cache;

import java.io.*;

@SuppressWarnings("unused")
public class DiskLruCache {

    public static final int JOURNAL_FORMAT_VERSION = 1;
    private static final boolean LOGGING = false;

    private final File cacheDir;
    private final Journal journal;
    private final long cacheSize;

    private DiskLruCache(File cacheDir, Journal journal, long cacheSize) {
        this.cacheDir = cacheDir;
        this.journal = journal;
        this.cacheSize = cacheSize;
    }

    public static DiskLruCache create(File cacheDir, long cacheSize) throws IOException {
        if (!cacheDir.exists()) {
            if (!cacheDir.mkdirs()) {
                throw new IOException("Unable to create specified cache directory");
            }
        }
        File file = new File(cacheDir, "journal.bin");
        Journal journal = Journal.readJournal(file);
        return new DiskLruCache(cacheDir, journal, cacheSize);
    }

    public File put(String key, File file) throws IOException {
        if (!isKeyValid(key)) {
            throw new IllegalArgumentException(String.format("Key %s is not valid", key));
        }
        long time = System.currentTimeMillis();
        long fileSize = file.length();
        Record record = new Record(key, time, fileSize);
        File cacheFile = new File(cacheDir, key);
        if ((cacheFile.exists() && cacheFile.delete()) | file.renameTo(cacheFile)) {
            journal.put(record, cacheSize, cacheDir);
            journal.writeJournal();
            return cacheFile;
        } else {
            throw new IOException(String.format("Unable to move file %s to the cache",
                    file.getName()));
        }
    }

    public File get(String key) {
        Record record = journal.get(key);
        if (record != null) {
            journal.writeJournal();
            return new File(cacheDir, record.getKey());
        } else {
            log("[-] No requested file with key %s in cache", key);
            return null;
        }
    }

    public long getCacheSize() {
        return cacheSize;
    }

    public long getUsedSpace() {
        return journal.getTotalSize();
    }

    public long getFreeSpace() {
        return cacheSize - journal.getTotalSize();
    }

    public static void log(String format, Object... args) {
        if (LOGGING) {
            System.out.println(String.format(format, args));
        }
    }

    @SuppressWarnings("all")
    public static boolean isKeyValid(String key) {
        File file = new File(key);
        try {
            file.getCanonicalPath();
            return true;
        } catch (IOException ignored) {
        }
        return false;
    }

}
