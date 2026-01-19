package com.tomclaw.cache;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SuppressWarnings({"unused", "WeakerAccess", "UnusedReturnValue"})
public class DiskLruCache {

    public static final Charset UTF_8 = StandardCharsets.UTF_8;
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
        if (cacheDir == null) {
            throw new IllegalArgumentException("Cache directory must not be null");
        }
        if (cacheSize <= 0) {
            throw new IllegalArgumentException("Cache size must be positive");
        }
        FileManager fileManager = new SimpleFileManager(cacheDir);
        Logger logger = new SimpleLogger(false);
        return create(fileManager, logger, cacheSize);
    }

    public static DiskLruCache create(FileManager fileManager, Logger logger, long cacheSize) throws IOException {
        if (fileManager == null) {
            throw new IllegalArgumentException("FileManager must not be null");
        }
        if (logger == null) {
            throw new IllegalArgumentException("Logger must not be null");
        }
        if (cacheSize <= 0) {
            throw new IllegalArgumentException("Cache size must be positive");
        }
        fileManager.prepare();
        Journal journal = Journal.readJournal(fileManager, logger);
        return new DiskLruCache(fileManager, journal, logger, cacheSize);
    }

    public File put(String key, File file) throws IOException {
        synchronized (journal) {
            assertKeyValid(key);
            // Delete old file if exists to prevent file leaks when extension changes
            Record oldRecord = journal.delete(key);
            if (oldRecord != null) {
                fileManager.delete(oldRecord.getName());
            }

            String name = generateName(key, file);
            long time = System.currentTimeMillis();
            long fileSize = file.length();
            Record record = new Record(key, name, time, fileSize);
            File cacheFile = fileManager.accept(file, name);
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

    /**
     * Returns information about all cached records sorted by last access time
     * (most recently accessed first). This method does not update access times.
     *
     * @return list of RecordInfo objects sorted by LRU order
     */
    public List<RecordInfo> getRecordsInfo() {
        synchronized (journal) {
            List<Record> records = journal.getRecordsSortedByTime();
            List<RecordInfo> result = new ArrayList<>(records.size());
            for (Record record : records) {
                result.add(new RecordInfo(
                        record.getKey(),
                        record.getName(),
                        record.getSize(),
                        record.getTime()
                ));
            }
            return result;
        }
    }

    /**
     * Returns information about a specific cached record without updating access time.
     *
     * @param key the key to look up
     * @return RecordInfo or null if not found
     */
    public RecordInfo getRecordInfo(String key) {
        synchronized (journal) {
            assertKeyValid(key);
            Record record = journal.peek(key);
            if (record != null) {
                return new RecordInfo(
                        record.getKey(),
                        record.getName(),
                        record.getSize(),
                        record.getTime()
                );
            }
            return null;
        }
    }

    private static void assertKeyValid(String key) {
        if (key == null || key.isEmpty()) {
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
