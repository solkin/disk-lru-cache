package com.tomclaw.cache;

import java.io.File;
import java.io.IOException;

public class SimpleFileManager implements FileManager {

    private final File dir;

    @SuppressWarnings("WeakerAccess")
    public SimpleFileManager(File dir) {
        this.dir = dir;
    }

    @Override
    public File journal() {
        return new File(dir, "journal.bin");
    }

    @Override
    public void prepare() throws IOException {
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                throw new IOException("Unable to create specified cache directory");
            }
        }
    }

    @Override
    public File get(String name) {
        return new File(dir, name);
    }

    @Override
    public File accept(File extFile, String name) throws IOException {
        File newFile = get(name);

        // Create cache directory if needed
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("Unable to create cache directory");
        }

        // Delete existing file if present
        if (newFile.exists() && !newFile.delete()) {
            throw formatException("Unable to delete existing file %s", newFile);
        }

        // Move file to cache
        if (!extFile.renameTo(newFile)) {
            // Fallback: copy file if rename fails (e.g., cross-filesystem)
            copyFile(extFile, newFile);
            if (!extFile.delete()) {
                // Log but don't fail - file is already copied
            }
        }

        return newFile;
    }

    private void copyFile(File source, File dest) throws IOException {
        java.io.FileInputStream fis = null;
        java.io.FileOutputStream fos = null;
        try {
            fis = new java.io.FileInputStream(source);
            fos = new java.io.FileOutputStream(dest);
            byte[] buffer = new byte[8192];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                fos.write(buffer, 0, length);
            }
        } finally {
            if (fis != null) {
                try { fis.close(); } catch (IOException ignored) {}
            }
            if (fos != null) {
                try { fos.close(); } catch (IOException ignored) {}
            }
        }
    }

    @Override
    public boolean exists(String name) {
        return new File(dir, name).exists();
    }

    @Override
    public void delete(String name) throws IOException {
        File file = new File(dir, name);
        if (file.exists() && !file.delete()) {
            throw formatException("Unable to delete file %s", file);
        }
    }

    private IOException formatException(String format, File file) {
        String message = String.format(format, file.getName());
        return new IOException(message);
    }

}
