package com.tomclaw.cache;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings({"unused", "WeakerAccess", "UnusedReturnValue"})
public class DiskLruCache {

    public static final Charset UTF_8 = Charset.forName("UTF-8");
    public static final String HASH_ALGORITHM = "MD5";

    private final Journal journal;
    private final long cacheSize;
    private final FileManager fileManager;
    private final Logger logger;

    private DiskLruCache(FileManager fileManager, Journal journal, Logger logger, long cacheSize) {
        this.fileManager = fileManager;
        this.journal = journal;
        this.logger = logger;
        this.cacheSize = cacheSize;
    }

    public static DiskLruCache create(File cacheDir, long cacheSize) throws IOException {
        FileManager fileManager = new SimpleFileManager(cacheDir);
        Logger logger = new SimpleLogger(false);
        return create(fileManager, logger, cacheSize);
    }

    public static DiskLruCache create(FileManager fileManager, Logger logger, long cacheSize) throws IOException {
        fileManager.prepare();
        Journal journal = Journal.readJournal(fileManager, logger);
        return new DiskLruCache(fileManager, journal, logger, cacheSize);
    }

    public File put(String key, File file) throws IOException {
        synchronized (journal) {
            assertKeyValid(key);
            String name = generateName(key, file);
            long time = System.currentTimeMillis();
            long fileSize = file.length();
            Record record = new Record(key, name, time, fileSize);
            File cacheFile = fileManager.accept(file, name);
            journal.delete(key);
            journal.put(record, cacheSize);
            journal.writeJournal();
            return cacheFile;
        }
    }

    public File get(String key) {
        synchronized (journal) {
            assertKeyValid(key);
            Record record = journal.get(key);
            if (record != null) {
                File file = fileManager.get(record.getName());
                if (!file.exists()) {
                    journal.delete(key);
                    file = null;
                }
                journal.writeJournal();
                return file;
            } else {
                logger.log("[-] No requested file with key %s in cache", key);
                return null;
            }
        }
    }

    public void delete(String key) throws IOException, RecordNotFoundException {
        delete(key, true);
    }

    private void delete(String key, boolean writeJournal)
            throws IOException, RecordNotFoundException {
        synchronized (journal) {
            assertKeyValid(key);
            Record record = journal.delete(key);
            if (record != null) {
                if (writeJournal) {
                    journal.writeJournal();
                }
                fileManager.delete(record.getName());
            } else {
                throw new RecordNotFoundException();
            }
        }
    }

    public void clearCache() throws IOException {
        synchronized (journal) {
            Set<String> keys = new HashSet<>(journal.keySet());
            for (String key : keys) {
                try {
                    delete(key, false);
                } catch (RecordNotFoundException ignored) {
                }
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
            MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
            byte[] bytes = digest.digest(base.getBytes(UTF_8));
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

}
