package com.tomclaw.cache;

class Record {

    private final String key;
    private final long time;
    private final long size;

    Record(Record record, long time) {
        this(record.key, time, record.size);
    }

    Record(String key, long time, long size) {
        this.key = key;
        this.time = time;
        this.size = size;
    }

    public String getKey() {
        return key;
    }

    public long getTime() {
        return time;
    }

    public long getSize() {
        return size;
    }
}
