package com.tomclaw.cache.demo;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tomclaw.cache.DiskLruCache;
import com.tomclaw.cache.demo.executor.TaskExecutor;
import com.tomclaw.cache.demo.executor.WeakObjectTask;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static com.tomclaw.cache.demo.App.cache;

public class MainActivity extends AppCompatActivity {

    private static Random random = new Random(System.currentTimeMillis());

    private TextView cacheSizeView;
    private TextView usedSpaceView;
    private TextView freeSpaceView;
    private TextView journalSizeView;
    private TextView filesCountView;
    private ProgressBar cacheUsageView;

    private CacheAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cacheSizeView = findViewById(R.id.cache_size);
        usedSpaceView = findViewById(R.id.used_space);
        freeSpaceView = findViewById(R.id.free_space);
        cacheUsageView = findViewById(R.id.cache_usage);
        journalSizeView = findViewById(R.id.journal_size);
        filesCountView = findViewById(R.id.files_count);
        View createFileButton = findViewById(R.id.create_file_button);
        View clearCacheButton = findViewById(R.id.clear_cache_button);
        createFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TaskExecutor.getInstance().execute(new CreateFileTask(MainActivity.this));
            }
        });
        clearCacheButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TaskExecutor.getInstance().execute(new ClearCacheTask(MainActivity.this));
            }
        });

        RecyclerView recyclerView = findViewById(R.id.recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CacheAdapter(this);
        recyclerView.setAdapter(adapter);

        bindViews();
    }

    private void bindViews() {
        DiskLruCache cache = cache();
        cacheSizeView.setText(formatBytes(cache.getCacheSize()));
        usedSpaceView.setText(formatBytes(cache.getUsedSpace()));
        freeSpaceView.setText(formatBytes(cache.getFreeSpace()));
        journalSizeView.setText(formatBytes(cache.getJournalSize()));
        filesCountView.setText(String.valueOf(cache.keySet().size()));
        cacheUsageView.setProgress((int) (100 * cache.getUsedSpace() / cache.getCacheSize()));
        List<CacheAdapter.CacheItem> cacheItems = new ArrayList<>();
        Set<String> keySet = Collections.unmodifiableSet(cache.keySet());
        for (String key : keySet) {
            File file = cache.get(key);
            cacheItems.add(new CacheAdapter.CacheItem(key, formatBytes(file.length())));
        }
        adapter.setCacheItems(cacheItems);
        adapter.notifyDataSetChanged();
    }

    public String formatBytes(long bytes) {
        Resources resources = getResources();
        if (bytes < 1024) {
            return resources.getString(R.string.bytes, bytes);
        } else if (bytes < 1024 * 1024) {
            return resources.getString(R.string.kibibytes, bytes / 1024.0f);
        } else if (bytes < 1024 * 1024 * 1024) {
            return resources.getString(R.string.mibibytes, bytes / 1024.0f / 1024.0f);
        } else {
            return resources.getString(R.string.gigibytes, bytes / 1024.0f / 1024.0f / 1024.0f);
        }
    }

    public static String generateRandomString() {
        return generateRandomString(16);
    }

    public static String generateRandomString(int length) {
        return generateRandomString(random, length, length);
    }

    public static String generateRandomString(Random r, int minChars, int maxChars) {
        int wordLength = minChars;
        int delta = maxChars - minChars;
        if (delta > 0) {
            wordLength += r.nextInt(delta);
        }
        StringBuilder sb = new StringBuilder(wordLength);
        for (int i = 0; i < wordLength; i++) {
            char tmp = (char) ('a' + r.nextInt('z' - 'a'));
            sb.append(tmp);
        }
        return sb.toString();
    }

    private static class CreateFileTask extends WeakObjectTask<MainActivity> {

        CreateFileTask(MainActivity activity) {
            super(activity);
        }

        @Override
        public void executeBackground() throws Throwable {
            Context context = getWeakObject();
            if (context != null) {
                String extension = generateRandomString(3);
                File file = File.createTempFile("rnd", "." + extension);
                DataOutputStream stream = null;
                try {
                    stream = new DataOutputStream(new FileOutputStream(file));
                    int blocks = 2000 + random.nextInt(2000);
                    for (int c = 0; c < blocks; c++) {
                        stream.writeLong(random.nextLong());
                        stream.flush();
                    }
                } finally {
                    if (stream != null) {
                        stream.close();
                    }
                }
                String key = generateRandomString();
                cache().put(key, file);
            }
        }

        @Override
        public void onSuccessMain() {
            MainActivity activity = getWeakObject();
            if (activity != null) {
                activity.bindViews();
            }
        }
    }

    private static class ClearCacheTask extends WeakObjectTask<MainActivity> {

        ClearCacheTask(MainActivity activity) {
            super(activity);
        }

        @Override
        public void executeBackground() {
            Context context = getWeakObject();
            if (context != null) {
                try {
                    cache().clearCache();
                } catch (IOException ignored) {
                }
            }
        }

        @Override
        public void onSuccessMain() {
            MainActivity activity = getWeakObject();
            if (activity != null) {
                activity.bindViews();
            }
        }
    }

}
