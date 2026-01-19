package com.tomclaw.cache;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Set;

import static com.tomclaw.cache.Helpers.randomString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class DiskLruCacheUnitTest {

    private static final long CACHE_SIZE = 1024;

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private File cacheDir;

    @Before
    public void setUp() throws IOException {
        cacheDir = folder.newFolder("cache");
    }

    // ==================== create() tests ====================

    @Test(expected = IllegalArgumentException.class)
    public void create_nullCacheDir_throwsException() throws IOException {
        DiskLruCache.create(null, CACHE_SIZE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void create_zeroCacheSize_throwsException() throws IOException {
        DiskLruCache.create(cacheDir, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void create_negativeCacheSize_throwsException() throws IOException {
        DiskLruCache.create(cacheDir, -100);
    }

    @Test
    public void create_validParams_cacheCreated() throws IOException {
        DiskLruCache cache = DiskLruCache.create(cacheDir, CACHE_SIZE);

        assertNotNull(cache);
        assertEquals(CACHE_SIZE, cache.getCacheSize());
        assertEquals(0, cache.getUsedSpace());
        assertEquals(CACHE_SIZE, cache.getFreeSpace());
    }

    // ==================== put() tests ====================

    @Test
    public void put_fileAddedToCache() throws IOException {
        DiskLruCache cache = DiskLruCache.create(cacheDir, CACHE_SIZE);
        File file = createTempFile(100, ".txt");
        String key = randomString();

        File cachedFile = cache.put(key, file);

        assertNotNull(cachedFile);
        assertTrue(cachedFile.exists());
        assertEquals(100, cachedFile.length());
        assertFalse(file.exists()); // Original file should be moved
    }

    @Test
    public void put_usedSpaceUpdated() throws IOException {
        DiskLruCache cache = DiskLruCache.create(cacheDir, CACHE_SIZE);
        File file = createTempFile(100, ".txt");
        String key = randomString();

        cache.put(key, file);

        assertEquals(100, cache.getUsedSpace());
        assertEquals(CACHE_SIZE - 100, cache.getFreeSpace());
    }

    @Test
    public void put_sameKeyDifferentExtension_oldFileDeleted() throws IOException {
        DiskLruCache cache = DiskLruCache.create(cacheDir, CACHE_SIZE);
        String key = "test-key";

        // Put first file with .txt extension
        File file1 = createTempFile(100, ".txt");
        File cachedFile1 = cache.put(key, file1);
        assertTrue(cachedFile1.exists());
        String cachedFileName1 = cachedFile1.getName();

        // Put second file with .jpg extension using same key
        File file2 = createTempFile(150, ".jpg");
        File cachedFile2 = cache.put(key, file2);

        // New file should exist
        assertTrue(cachedFile2.exists());
        assertEquals(150, cachedFile2.length());

        // Old file should be deleted (different extension = different filename)
        assertFalse(cachedFile1.exists());

        // Used space should reflect only the new file
        assertEquals(150, cache.getUsedSpace());

        // Only one key in cache
        assertEquals(1, cache.keySet().size());
    }

    @Test
    public void put_sameKeySameExtension_fileReplaced() throws IOException {
        DiskLruCache cache = DiskLruCache.create(cacheDir, CACHE_SIZE);
        String key = "test-key";

        // Put first file
        File file1 = createTempFile(100, ".txt");
        cache.put(key, file1);

        // Put second file with same key and extension
        File file2 = createTempFile(200, ".txt");
        File cachedFile2 = cache.put(key, file2);

        assertTrue(cachedFile2.exists());
        assertEquals(200, cachedFile2.length());
        assertEquals(200, cache.getUsedSpace());
        assertEquals(1, cache.keySet().size());
    }

    @Test
    public void put_fileLargerThanCache_throwsException() throws IOException {
        long smallCacheSize = 100;
        DiskLruCache cache = DiskLruCache.create(cacheDir, smallCacheSize);
        File file = createTempFile(200, ".txt");
        String key = randomString();

        try {
            cache.put(key, file);
            fail("Expected IOException for file larger than cache");
        } catch (IOException e) {
            assertTrue(e.getMessage().contains("exceeds cache size"));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void put_nullKey_throwsException() throws IOException {
        DiskLruCache cache = DiskLruCache.create(cacheDir, CACHE_SIZE);
        File file = createTempFile(100, ".txt");

        cache.put(null, file);
    }

    @Test(expected = IllegalArgumentException.class)
    public void put_emptyKey_throwsException() throws IOException {
        DiskLruCache cache = DiskLruCache.create(cacheDir, CACHE_SIZE);
        File file = createTempFile(100, ".txt");

        cache.put("", file);
    }

    @Test
    public void put_lruEviction_oldestFileRemoved() throws IOException {
        long smallCacheSize = 250;
        DiskLruCache cache = DiskLruCache.create(cacheDir, smallCacheSize);

        File file1 = createTempFile(100, ".txt");
        File file2 = createTempFile(100, ".txt");
        File file3 = createTempFile(100, ".txt");

        cache.put("key1", file1);
        cache.put("key2", file2);
        // This should evict key1
        cache.put("key3", file3);

        assertNull(cache.get("key1"));
        assertNotNull(cache.get("key2"));
        assertNotNull(cache.get("key3"));
    }

    // ==================== get() tests ====================

    @Test
    public void get_existingKey_returnsFile() throws IOException {
        DiskLruCache cache = DiskLruCache.create(cacheDir, CACHE_SIZE);
        File file = createTempFile(100, ".txt");
        String key = randomString();
        cache.put(key, file);

        File result = cache.get(key);

        assertNotNull(result);
        assertTrue(result.exists());
        assertEquals(100, result.length());
    }

    @Test
    public void get_nonExistingKey_returnsNull() throws IOException {
        DiskLruCache cache = DiskLruCache.create(cacheDir, CACHE_SIZE);

        File result = cache.get("non-existing-key");

        assertNull(result);
    }

    @Test
    public void get_fileDeletedExternally_returnsNull() throws IOException {
        DiskLruCache cache = DiskLruCache.create(cacheDir, CACHE_SIZE);
        File file = createTempFile(100, ".txt");
        String key = randomString();
        File cachedFile = cache.put(key, file);

        // Delete file externally
        assertTrue(cachedFile.delete());

        // get() should return null and clean up journal
        File result = cache.get(key);
        assertNull(result);

        // Key should be removed from cache
        assertFalse(cache.keySet().contains(key));
    }

    @Test(expected = IllegalArgumentException.class)
    public void get_nullKey_throwsException() throws IOException {
        DiskLruCache cache = DiskLruCache.create(cacheDir, CACHE_SIZE);
        cache.get(null);
    }

    // ==================== delete() tests ====================

    @Test
    public void delete_existingKey_fileRemoved() throws IOException, RecordNotFoundException {
        DiskLruCache cache = DiskLruCache.create(cacheDir, CACHE_SIZE);
        File file = createTempFile(100, ".txt");
        String key = randomString();
        File cachedFile = cache.put(key, file);

        cache.delete(key);

        assertFalse(cachedFile.exists());
        assertNull(cache.get(key));
        assertEquals(0, cache.getUsedSpace());
    }

    @Test(expected = RecordNotFoundException.class)
    public void delete_nonExistingKey_throwsException() throws IOException, RecordNotFoundException {
        DiskLruCache cache = DiskLruCache.create(cacheDir, CACHE_SIZE);
        cache.delete("non-existing-key");
    }

    // ==================== clearCache() tests ====================

    @Test
    public void clearCache_allFilesRemoved() throws IOException {
        DiskLruCache cache = DiskLruCache.create(cacheDir, CACHE_SIZE);
        cache.put("key1", createTempFile(100, ".txt"));
        cache.put("key2", createTempFile(100, ".txt"));
        cache.put("key3", createTempFile(100, ".txt"));

        cache.clearCache();

        assertEquals(0, cache.keySet().size());
        assertEquals(0, cache.getUsedSpace());
        assertEquals(CACHE_SIZE, cache.getFreeSpace());
    }

    @Test
    public void clearCache_emptyCache_noError() throws IOException {
        DiskLruCache cache = DiskLruCache.create(cacheDir, CACHE_SIZE);

        cache.clearCache(); // Should not throw

        assertEquals(0, cache.keySet().size());
    }

    // ==================== keySet() tests ====================

    @Test
    public void keySet_returnsAllKeys() throws IOException {
        DiskLruCache cache = DiskLruCache.create(cacheDir, CACHE_SIZE);
        cache.put("key1", createTempFile(100, ".txt"));
        cache.put("key2", createTempFile(100, ".txt"));
        cache.put("key3", createTempFile(100, ".txt"));

        Set<String> keys = cache.keySet();

        assertEquals(3, keys.size());
        assertTrue(keys.contains("key1"));
        assertTrue(keys.contains("key2"));
        assertTrue(keys.contains("key3"));
    }

    @Test
    public void keySet_emptyCache_returnsEmptySet() throws IOException {
        DiskLruCache cache = DiskLruCache.create(cacheDir, CACHE_SIZE);

        Set<String> keys = cache.keySet();

        assertTrue(keys.isEmpty());
    }

    // ==================== Status methods tests ====================

    @Test
    public void getJournalSize_returnsNonZeroAfterPut() throws IOException {
        DiskLruCache cache = DiskLruCache.create(cacheDir, CACHE_SIZE);
        cache.put("key", createTempFile(100, ".txt"));

        long journalSize = cache.getJournalSize();

        assertTrue(journalSize > 0);
    }

    // ==================== Persistence tests ====================

    @Test
    public void persistence_cacheRestoredAfterReopen() throws IOException {
        // Create cache and add files
        DiskLruCache cache1 = DiskLruCache.create(cacheDir, CACHE_SIZE);
        cache1.put("key1", createTempFile(100, ".txt"));
        cache1.put("key2", createTempFile(150, ".jpg"));

        // Reopen cache
        DiskLruCache cache2 = DiskLruCache.create(cacheDir, CACHE_SIZE);

        assertEquals(2, cache2.keySet().size());
        assertNotNull(cache2.get("key1"));
        assertNotNull(cache2.get("key2"));
        assertEquals(250, cache2.getUsedSpace());
    }

    // ==================== Helper methods ====================

    private File createTempFile(int size, String extension) throws IOException {
        String name = randomString(8);
        File file = folder.newFile(name + extension);
        try (DataOutputStream stream = new DataOutputStream(new FileOutputStream(file))) {
            for (int c = 0; c < size; c++) {
                stream.writeByte(c % 256);
            }
            stream.flush();
        }
        return file;
    }

}
