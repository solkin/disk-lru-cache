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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Record record = (Record) o;

        if (time != record.time) return false;
        if (size != record.size) return false;
        if (!key.equals(record.key)) return false;
        return name.equals(record.name);
    }

    @Override
    public int hashCode() {
        int result = key.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + (int) (time ^ (time >>> 32));
        result = 31 * result + (int) (size ^ (size >>> 32));
        return result;
    }
}
