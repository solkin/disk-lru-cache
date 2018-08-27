package com.tomclaw.cache;

class Record {

    private final String key;
    private final String name;
    private final long time;
    private final long size;

    Record(Record record, long time) {
        this(record.key, record.name, time, record.size);
    }

    Record(String key, String name, long time, long size) {
        this.key = key;
        this.name = name;
        this.time = time;
        this.size = size;
    }

    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public long getTime() {
        return time;
    }

    public long getSize() {
        return size;
    }
}
