package com.tomclaw.cache;

import java.util.Comparator;

class RecordComparator implements Comparator<Record> {

    @Override
    public int compare(Record record1, Record record2) {
        return compare(record2.getTime(), record1.getTime());
    }

    private static int compare(long x, long y) {
        return (x < y) ? -1 : ((x == y) ? 0 : 1);
    }
}
