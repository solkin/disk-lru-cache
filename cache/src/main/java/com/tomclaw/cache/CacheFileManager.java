package com.tomclaw.cache;

import java.io.File;
import java.io.IOException;

public class CacheFileManager {

    private File cacheDir;

    public CacheFileManager(File cacheDir) {
        this.cacheDir = cacheDir;
    }

    public File get(String name) {
        return new File(cacheDir, name);
    }

    public File accept(File extFile, String name) throws IOException {
        File cacheFile = get(name);
        if ((cacheDir.exists() || cacheDir.mkdirs())
                | (cacheFile.exists() && cacheFile.delete())
                | extFile.renameTo(cacheFile)) {
            return extFile;
        } else {
            throw formatException("Unable to move file %s to the cache", extFile);
        }
    }

    public boolean exists(String name) {
        return new File(cacheDir, name).exists();
    }

    public void delete(String name) throws IOException {
        File file = new File(cacheDir, name);
        if (file.exists() && !file.delete()) {
            throw formatException("Unable to delete file %s from cache", file);
        }
    }

    private IOException formatException(String format, File file) {
        String message = String.format(format, file.getName());
        return new IOException(message);
    }

}
