package com.tomclaw.cache;

/**
 * Public information about a cached record.
 * This is a snapshot of the record state at the time of retrieval.
 */
@SuppressWarnings("unused")
public class RecordInfo {

    private final String key;
    private final String fileName;
    private final long size;
    private final long lastAccessed;

    RecordInfo(String key, String fileName, long size, long lastAccessed) {
        this.key = key;
        this.fileName = fileName;
        this.size = size;
        this.lastAccessed = lastAccessed;
    }

    /**
     * Returns the key used to store this record.
     */
    public String getKey() {
        return key;
    }

    /**
     * Returns the actual file name in cache directory.
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Returns the file size in bytes.
     */
    public long getSize() {
        return size;
    }

    /**
     * Returns the timestamp of last access (put or get operation).
     * Files with older timestamps will be evicted first (LRU).
     */
    public long getLastAccessed() {
        return lastAccessed;
    }

    @Override
    public String toString() {
        return "RecordInfo{" +
                "key='" + key + '\'' +
                ", fileName='" + fileName + '\'' +
                ", size=" + size +
                ", lastAccessed=" + lastAccessed +
                '}';
    }
}
