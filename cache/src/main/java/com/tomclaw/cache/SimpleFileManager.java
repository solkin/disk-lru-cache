package com.tomclaw.cache;

import java.io.File;
import java.io.IOException;

public class SimpleFileManager implements FileManager {

    private File dir;

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
        if ((dir.exists() || dir.mkdirs())
                | (newFile.exists() && newFile.delete())
                | extFile.renameTo(newFile)) {
            return newFile;
        } else {
            throw formatException("Unable to accept file %s", extFile);
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
