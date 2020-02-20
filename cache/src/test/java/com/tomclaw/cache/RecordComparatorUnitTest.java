package com.tomclaw.cache;

import org.junit.Before;
import org.junit.Test;

import static com.tomclaw.cache.Helpers.randomLong;
import static com.tomclaw.cache.Helpers.randomString;
import static org.junit.Assert.assertEquals;

public class RecordComparatorUnitTest {

    private RecordComparator comparator;

    @Before
    public void setUp() {
        comparator = createComparator();
    }

    @Test
    public void compare_leftRecordTimeIsLessThanRight() {
        Record record1 = randomRecord(1000);
        Record record2 = randomRecord(1001);

        int result = comparator.compare(record1, record2);

        assertEquals(1, result);
    }

    @Test
    public void compare_recordsTimesAreEquals() {
        Record record1 = randomRecord(1000);
        Record record2 = randomRecord(1000);

        int result = comparator.compare(record1, record2);

        assertEquals(0, result);
    }

    @Test
    public void compare_leftRecordTimeIsMoreThanRight() {
        Record record1 = randomRecord(1001);
        Record record2 = randomRecord(1000);

        int result = comparator.compare(record1, record2);

        assertEquals(-1, result);
    }

    private Record randomRecord(long time) {
        String key = randomString();
        String name = randomString();
        long size = randomLong();
        return new Record(key, name, time, size);
    }

    private RecordComparator createComparator() {
        return new RecordComparator();
    }

}