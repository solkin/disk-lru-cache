package com.tomclaw.cache;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;

import static com.tomclaw.cache.Helpers.randomString;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SimpleFileManagerUnitTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private File cacheDir;
    private SimpleFileManager fileManager;

    @Before
    public void setUp() {
        cacheDir = new File(folder.getRoot(), "cache");
        fileManager = new SimpleFileManager(cacheDir);
    }

    // ==================== journal() tests ====================

    @Test
    public void journal_returnsCorrectPath() {
        File journal = fileManager.journal();

        assertEquals("journal.bin", journal.getName());
        assertEquals(cacheDir, journal.getParentFile());
    }

    // ==================== prepare() tests ====================

    @Test
    public void prepare_createsDirectory() throws IOException {
        assertFalse(cacheDir.exists());

        fileManager.prepare();

        assertTrue(cacheDir.exists());
        assertTrue(cacheDir.isDirectory());
    }

    @Test
    public void prepare_directoryAlreadyExists_noError() throws IOException {
        assertTrue(cacheDir.mkdirs());

        fileManager.prepare(); // Should not throw

        assertTrue(cacheDir.exists());
    }

    // ==================== get() tests ====================

    @Test
    public void get_returnsFileInCacheDir() {
        String name = "test-file.txt";

        File file = fileManager.get(name);

        assertEquals(name, file.getName());
        assertEquals(cacheDir, file.getParentFile());
    }

    // ==================== accept() tests ====================

    @Test
    public void accept_movesFileToCache() throws IOException {
        fileManager.prepare();
        File sourceFile = createTempFile(100);
        String targetName = "cached-file.dat";

        File result = fileManager.accept(sourceFile, targetName);

        assertTrue(result.exists());
        assertEquals(targetName, result.getName());
        assertEquals(cacheDir, result.getParentFile());
        assertEquals(100, result.length());
        assertFalse(sourceFile.exists()); // Original should be moved/deleted
    }

    @Test
    public void accept_createsCacheDirIfNotExists() throws IOException {
        assertFalse(cacheDir.exists());
        File sourceFile = createTempFile(50);
        String targetName = "file.dat";

        File result = fileManager.accept(sourceFile, targetName);

        assertTrue(cacheDir.exists());
        assertTrue(result.exists());
    }

    @Test
    public void accept_replacesExistingFile() throws IOException {
        fileManager.prepare();

        // Create first file in cache
        File source1 = createTempFile(100);
        String targetName = "file.dat";
        File cached1 = fileManager.accept(source1, targetName);
        assertEquals(100, cached1.length());

        // Replace with second file
        File source2 = createTempFile(200);
        File cached2 = fileManager.accept(source2, targetName);

        assertTrue(cached2.exists());
        assertEquals(200, cached2.length());
    }

    @Test
    public void accept_preservesFileContent() throws IOException {
        fileManager.prepare();
        byte[] content = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        File sourceFile = createTempFileWithContent(content);
        String targetName = "data.bin";

        File result = fileManager.accept(sourceFile, targetName);

        byte[] resultContent = Files.readAllBytes(result.toPath());
        assertArrayEquals(content, resultContent);
    }

    // ==================== exists() tests ====================

    @Test
    public void exists_fileExists_returnsTrue() throws IOException {
        fileManager.prepare();
        File file = new File(cacheDir, "existing.txt");
        assertTrue(file.createNewFile());

        boolean result = fileManager.exists("existing.txt");

        assertTrue(result);
    }

    @Test
    public void exists_fileNotExists_returnsFalse() throws IOException {
        fileManager.prepare();

        boolean result = fileManager.exists("non-existing.txt");

        assertFalse(result);
    }

    // ==================== delete() tests ====================

    @Test
    public void delete_existingFile_deletesFile() throws IOException {
        fileManager.prepare();
        File file = new File(cacheDir, "to-delete.txt");
        assertTrue(file.createNewFile());
        assertTrue(file.exists());

        fileManager.delete("to-delete.txt");

        assertFalse(file.exists());
    }

    @Test
    public void delete_nonExistingFile_noError() throws IOException {
        fileManager.prepare();

        fileManager.delete("non-existing.txt"); // Should not throw
    }

    // ==================== Helper methods ====================

    private File createTempFile(int size) throws IOException {
        String name = randomString(8);
        File file = folder.newFile(name + ".tmp");
        try (DataOutputStream stream = new DataOutputStream(new FileOutputStream(file))) {
            for (int c = 0; c < size; c++) {
                stream.writeByte(c % 256);
            }
            stream.flush();
        }
        return file;
    }

    private File createTempFileWithContent(byte[] content) throws IOException {
        String name = randomString(8);
        File file = folder.newFile(name + ".tmp");
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(content);
            fos.flush();
        }
        return file;
    }

}
