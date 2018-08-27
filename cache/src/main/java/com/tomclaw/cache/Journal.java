package com.tomclaw.cache;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("unused")
class Journal {

    private final File file;
    private final Map<String, Record> map = new HashMap<>();
    private long totalSize = 0;

    private Journal(File file) {
        this.file = file;
    }

    public void put(Record record, long cacheSize, File cacheDir) throws IOException {
        long fileSize = record.getSize();
        prepare(fileSize, cacheSize, cacheDir);
        put(record);
    }

    private void put(Record record) {
        map.put(record.getKey(), record);
        totalSize += record.getSize();
        DiskLruCache.log("[+] Put %s (%d bytes) and cache size became %d bytes",
                record.getKey(), record.getSize(), totalSize);
    }

    public Record get(String key) {
        Record record = map.get(key);
        if (record != null) {
            updateTime(record);
            DiskLruCache.log("[^] Update time of %s (%d bytes)", record.getKey(), record.getSize());
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

    private void prepare(long fileSize, long cacheSize, File cacheDir) throws IOException {
        if (totalSize + fileSize > cacheSize) {
            DiskLruCache.log("[!] File %d bytes is not fit in cache %d bytes", fileSize, totalSize);
            List<Record> records = new ArrayList<>(map.values());
            Collections.sort(records, new RecordComparator());
            for (int c = records.size() - 1; c > 0; c--) {
                Record record = records.remove(c);
                long nextTotalSize = totalSize - record.getSize();
                DiskLruCache.log("[x] Delete %s [%d ms] %d bytes and free cache to %d bytes",
                        record.getKey(), record.getTime(), record.getSize(), nextTotalSize);
                File file = new File(cacheDir, record.getName());
                if (file.exists() && !file.delete()) {
                    throw new IOException(String.format("Unable to delete file %s from cache",
                            file.getName()));
                }
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
        DataOutputStream stream = null;
        try {
            stream = new DataOutputStream(new FileOutputStream(file));
            stream.writeShort(DiskLruCache.JOURNAL_FORMAT_VERSION);
            stream.writeInt(map.size());
            for (Record record : map.values()) {
                stream.writeUTF(record.getKey());
                stream.writeUTF(record.getName());
                stream.writeLong(record.getTime());
                stream.writeLong(record.getSize());
            }
        } catch (IOException ex) {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    public static Journal readJournal(File file) {
        DiskLruCache.log("[.] Start journal reading", file.getName());
        Journal journal = new Journal(file);
        DataInputStream stream = null;
        try {
            stream = new DataInputStream(new FileInputStream(file));
            int version = stream.readShort();
            if (version != DiskLruCache.JOURNAL_FORMAT_VERSION) {
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
            DiskLruCache.log("[.] Journal read. Files count is %d and total size is %d",
                    count, totalSize);
        } catch (IOException ex) {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException ignored) {
                }
            }
        }
        return journal;
    }

}
