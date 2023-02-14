package com.tomclaw.cache;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressWarnings({"unused", "WeakerAccess"})
class Journal {

    public static final int JOURNAL_FORMAT_VERSION = 1;

    private final File file;
    private final FileManager fileManager;
    private final Logger logger;
    private final Map<String, Record> map = new HashMap<>();
    private long totalSize = 0;

    private Journal(File file, FileManager fileManager, Logger logger) {
        this.file = file;
        this.fileManager = fileManager;
        this.logger = logger;
    }

    public void put(Record record, long cacheSize) throws IOException {
        long fileSize = record.getSize();
        prepare(fileSize, cacheSize);
        put(record);
    }

    private void put(Record record) {
        map.put(record.getKey(), record);
        totalSize += record.getSize();
        logger.log("[+] Put %s (%d bytes) and cache size became %d bytes",
                record.getKey(), record.getSize(), totalSize);
    }

    public Record get(String key) {
        Record record = map.get(key);
        if (record != null) {
            updateTime(record);
            logger.log("[^] Update time of %s (%d bytes)", record.getKey(), record.getSize());
        }
        return record;
    }

    public Record delete(String key) {
        Record record = map.remove(key);
        if (record != null) {
            totalSize -= record.getSize();
        }
        return record;
    }

    public Set<String> keySet() {
        return Collections.unmodifiableSet(map.keySet());
    }

    private void updateTime(Record record) {
        long time = System.currentTimeMillis();
        map.put(record.getKey(), new Record(record, time));
    }

    private void prepare(long fileSize, long cacheSize) throws IOException {
        if (totalSize + fileSize > cacheSize) {
            logger.log("[!] File %d bytes is not fit in cache %d bytes", fileSize, totalSize);
            List<Record> records = new ArrayList<>(map.values());
            Collections.sort(records, new RecordComparator());
            for (int c = records.size() - 1; c > 0; c--) {
                Record record = records.remove(c);
                long nextTotalSize = totalSize - record.getSize();
                logger.log("[x] Delete %s [%d ms] %d bytes and free cache to %d bytes",
                        record.getKey(), record.getTime(), record.getSize(), nextTotalSize);
                fileManager.delete(record.getName());
                map.remove(record.getKey());
                totalSize = nextTotalSize;

                if (totalSize + fileSize <= cacheSize) {
                    break;
                }
            }
        }
    }

    public long getTotalSize() {
        return totalSize;
    }

    public long getJournalSize() {
        return file.length();
    }

    private void setTotalSize(long totalSize) {
        this.totalSize = totalSize;
    }

    public void writeJournal() {
        try (FileOutputStream fileStream = new FileOutputStream(file)) {
            try (DataOutputStream stream = new DataOutputStream(new BufferedOutputStream(fileStream))) {
                stream.writeShort(JOURNAL_FORMAT_VERSION);
                stream.writeInt(map.size());
                for (Record record : map.values()) {
                    stream.writeUTF(record.getKey());
                    stream.writeUTF(record.getName());
                    stream.writeLong(record.getTime());
                    stream.writeLong(record.getSize());
                }
            }
        } catch (IOException ex) {
            logger.log("[.] Failed to write journal %s", ex.getMessage());
            ex.printStackTrace();
        }
    }

    public static Journal readJournal(FileManager fileManager, Logger logger) {
        File file = fileManager.journal();
        logger.log("[.] Start journal reading", file.getName());
        Journal journal = new Journal(file, fileManager, logger);
        try (FileInputStream fileStream = new FileInputStream(file)) {
            try (DataInputStream stream = new DataInputStream(new BufferedInputStream(fileStream))) {
                int version = stream.readShort();
                if (version != JOURNAL_FORMAT_VERSION) {
                    throw new IllegalArgumentException("Invalid journal format version");
                }
                int count = stream.readInt();
                long totalSize = 0;
                for (int c = 0; c < count; c++) {
                    String key = stream.readUTF();
                    String name = stream.readUTF();
                    long time = stream.readLong();
                    long size = stream.readLong();
                    totalSize += size;
                    Record record = new Record(key, name, time, size);
                    journal.put(record);
                }
                journal.setTotalSize(totalSize);
                logger.log("[.] Journal read. Files count is %d and total size is %d", count, totalSize);
            }
        } catch (FileNotFoundException ignored) {
            logger.log("[.] Journal not found and will be created");
        } catch (IOException ex) {
            logger.log("[.] Failed to read journal %s", ex.getMessage());
            ex.printStackTrace();
        }
        return journal;
    }

}
