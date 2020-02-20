package com.tomclaw.cache;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;
import java.util.Set;

import static com.tomclaw.cache.Helpers.randomString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class JournalUnitTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private Random random = new Random(System.currentTimeMillis());

    private Journal journal;

    private FileManager fileManager;

    @Before
    public void setUp() {
        fileManager = new SimpleFileManager(folder.getRoot());
    }

    @Test
    public void putRecords_totalSizeIncreasedCorrectly() throws Exception {
        long cacheSize = 1024;
        journal = createJournal();
        File file1 = createRandomFile(100);
        File file2 = createRandomFile(200);
        File file3 = createRandomFile(150);
        Record record1 = randomRecord(file1, 1001);
        Record record2 = randomRecord(file2, 1002);
        Record record3 = randomRecord(file3, 1003);

        journal.put(record1, cacheSize);
        journal.put(record2, cacheSize);
        journal.put(record3, cacheSize);

        assertEquals(450, journal.getTotalSize());
    }

    @Test
    public void putRecords_recordsAccessible() throws Exception {
        long cacheSize = 300;
        journal = createJournal();
        File file1 = createRandomFile(100);
        File file2 = createRandomFile(200);
        Record record1 = randomRecord(file1, 1001);
        Record record2 = randomRecord(file2, 1002);

        journal.put(record1, cacheSize);
        journal.put(record2, cacheSize);

        assertEquals(record1, journal.get(record1.getKey()));
        assertTrue(file1.exists());
        assertEquals(record2, journal.get(record2.getKey()));
        assertTrue(file2.exists());
    }

    @Test
    public void putRecords_keySetIsCorrect() throws Exception {
        long cacheSize = 300;
        journal = createJournal();
        File file1 = createRandomFile(100);
        File file2 = createRandomFile(200);
        Record record1 = randomRecord(file1, 1001);
        Record record2 = randomRecord(file2, 1002);
        journal.put(record1, cacheSize);
        journal.put(record2, cacheSize);

        Set<String> keySet = journal.keySet();

        assertEquals(2, keySet.size());
        assertTrue(keySet.contains(record1.getKey()));
        assertTrue(keySet.contains(record2.getKey()));
    }

    @Test
    public void putRecords_firstInsertedRecordPurged_recordsSizeMoreThanCacheSize() throws Exception {
        long cacheSize = 300;
        journal = createJournal();
        File file1 = createRandomFile(100);
        File file2 = createRandomFile(200);
        File file3 = createRandomFile(150);
        Record record1 = randomRecord(file1, 1001);
        Record record2 = randomRecord(file2, 1002);
        Record record3 = randomRecord(file3, 1003);

        journal.put(record1, cacheSize);
        journal.put(record2, cacheSize);
        journal.put(record3, cacheSize);

        assertNull(journal.get(record1.getKey()));
        assertFalse(file1.exists());
    }

    @Test
    public void getRecords_recordTimeUpdates() throws Exception {
        long cacheSize = 300;
        journal = createJournal();
        File file = createRandomFile(100);
        Record original = randomRecord(file, 1001);
        String key = original.getKey();

        journal.put(original, cacheSize);
        journal.get(key);

        Record updated = journal.get(key);
        assertNotEquals(original.getTime(), updated.getTime());
        assertEquals(original.getName(), updated.getName());
    }

    @Test
    public void putRecords_leastUsedRecordIsPurged() throws Exception {
        long cacheSize = 300;
        journal = createJournal();
        File file1 = createRandomFile(100);
        File file2 = createRandomFile(200);
        File file3 = createRandomFile(150);
        Record record1 = randomRecord(file1, 1001);
        Record record2 = randomRecord(file2, 1002);
        Record record3 = randomRecord(file3, 1003);

        journal.put(record1, cacheSize);
        journal.put(record2, cacheSize);
        journal.get(record1.getKey());
        journal.put(record3, cacheSize);

        assertNull(journal.get(record2.getKey()));
        assertFalse(file2.exists());
        assertEquals(record1.getName(), journal.get(record1.getKey()).getName());
        assertTrue(file1.exists());
    }

    @Test
    public void deleteRecords_recordIsNotAccessible() throws Exception {
        long cacheSize = 300;
        journal = createJournal();
        File file = createRandomFile(100);
        Record record = randomRecord(file, 1001);
        journal.put(record, cacheSize);

        journal.delete(record.getKey());

        assertNull(journal.get(record.getKey()));
    }

    @Test
    public void writeJournal_journalSizeIsCorrect() throws Exception {
        long cacheSize = 1000;
        Journal journal = Journal.readJournal(fileManager);
        File file = createRandomFile(100);
        Record record = randomRecord(file, 1001);
        journal.put(record, cacheSize);
        journal.writeJournal();

        long journalSize = journal.getJournalSize();

        File journalFile = fileManager.journal();
        assertEquals(journalFile.length(), journalSize);
    }

    @Test
    public void writeAndParseJournal_journalRestoresCorrectly() throws Exception {
        long cacheSize = 1000;
        Journal original = Journal.readJournal(fileManager);
        File file1 = createRandomFile(100);
        File file2 = createRandomFile(200);
        File file3 = createRandomFile(150);
        Record record1 = randomRecord(file1, 1001);
        Record record2 = randomRecord(file2, 1002);
        Record record3 = randomRecord(file3, 1003);
        original.put(record1, cacheSize);
        original.put(record2, cacheSize);
        original.put(record3, cacheSize);

        original.writeJournal();
        Journal restored = Journal.readJournal(fileManager);

        assertEquals(record1, restored.get(record1.getKey()));
        assertEquals(record2, restored.get(record2.getKey()));
        assertEquals(record3, restored.get(record3.getKey()));
    }

    private Record randomRecord(File file, long time) {
        String key = randomString();
        String name = file.getName();
        long size = file.length();
        return new Record(key, name, time, size);
    }

    private File createRandomFile(int size) throws IOException {
        String name = randomString(8);
        String extension = randomString(3);
        File file = folder.newFile(name + "." + extension);
        try (DataOutputStream stream = new DataOutputStream(new FileOutputStream(file))) {
            for (int c = 0; c < size; c++) {
                stream.writeByte(random.nextInt(255));
            }
            stream.flush();
        }
        return file;
    }

    private Journal createJournal() {
        return Journal.readJournal(fileManager);
    }

}