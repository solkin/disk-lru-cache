package com.tomclaw.cache.demo;

import android.app.Application;

import com.tomclaw.cache.DiskLruCache;

import java.io.File;
import java.io.IOException;

public class App extends Application {

    private static final long CACHE_SIZE = 500 * 1024;

    private static DiskLruCache cache;

    @Override
    public void onCreate() {
        super.onCreate();
        File cacheDir = getCacheDir();
        try {
            cache = DiskLruCache.create(cacheDir, CACHE_SIZE);
        } catch (IOException ignored) {
        }
    }

    public static DiskLruCache cache() {
        return cache;
    }
}
