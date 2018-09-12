package com.tomclaw.cache;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;

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
        synchronized (journal) {
            assertKeyValid(key);
            String name = generateName(key, file);
            long time = System.currentTimeMillis();
            long fileSize = file.length();
            Record record = new Record(key, name, time, fileSize);
            File cacheFile = new File(cacheDir, name);
            if ((cacheDir.exists() || cacheDir.mkdirs())
                    | (cacheFile.exists() && cacheFile.delete())
                    | file.renameTo(cacheFile)) {
                journal.delete(key);
                journal.put(record, cacheSize, cacheDir);
                journal.writeJournal();
                return cacheFile;
            } else {
                throw new IOException(String.format("Unable to move file %s to the cache",
                        file.getName()));
            }
        }
    }

    public File get(String key) {
        synchronized (journal) {
            assertKeyValid(key);
            Record record = journal.get(key);
            if (record != null) {
                File file = new File(cacheDir, record.getName());
                if (!file.exists()) {
                    journal.delete(key);
                    file = null;
                }
                journal.writeJournal();
                return file;
            } else {
                log("[-] No requested file with key %s in cache", key);
                return null;
            }
        }
    }

    public boolean delete(String key) {
        return delete(key, true);
    }

    private boolean delete(String key, boolean writeJournal) {
        synchronized (journal) {
            assertKeyValid(key);
            Record record = journal.delete(key);
            if (record != null) {
                if (writeJournal) {
                    journal.writeJournal();
                }
                File file = new File(cacheDir, record.getName());
                return file.delete();
            }
            return false;
        }
    }

    public void clearCache() {
        synchronized (journal) {
            Set<String> keys = new HashSet<>(journal.keySet());
            for (String key : keys) {
                delete(key, false);
            }
            journal.writeJournal();
        }
    }

    public Set<String> keySet() {
        synchronized (journal) {
            return journal.keySet();
        }
    }

    public long getCacheSize() {
        return cacheSize;
    }

    public long getUsedSpace() {
        synchronized (journal) {
            return journal.getTotalSize();
        }
    }

    public long getFreeSpace() {
        synchronized (journal) {
            return cacheSize - journal.getTotalSize();
        }
    }

    public long getJournalSize() {
        synchronized (journal) {
            return journal.getJournalSize();
        }
    }

    private static void assertKeyValid(String key) {
        if (key == null || key.length() == 0) {
            throw new IllegalArgumentException(String.format("Invalid key value: '%s'", key));
        }
    }

    private static String keyHash(String base) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] bytes = digest.digest(base.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();
            for (byte b : bytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException ignored) {
        } catch (UnsupportedEncodingException ignored) {
        }
        throw new IllegalArgumentException("Unable to hash key");
    }

    private static String generateName(String key, File file) {
        return keyHash(key) + fileExtension(file.getName());
    }

    private static String fileExtension(String path) {
        String suffix = "";
        if (path != null && !path.isEmpty()) {
            int index = path.lastIndexOf(".");
            if (index != -1) {
                suffix = path.substring(index);
            }
        }
        return suffix;
    }

    public static void log(String format, Object... args) {
        if (LOGGING) {
            System.out.println(String.format(format, args));
        }
    }

}
