package com.tomclaw.cache.demo;

import android.annotation.SuppressLint;
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
import com.tomclaw.cache.demo.task.ClearCacheTask;
import com.tomclaw.cache.demo.task.CreateFileTask;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.tomclaw.cache.demo.App.cache;

public class MainActivity extends AppCompatActivity {

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
        createFileButton.setOnClickListener(v ->
                TaskExecutor.getInstance().execute(new CreateFileTask(MainActivity.this))
        );
        clearCacheButton.setOnClickListener(v ->
                TaskExecutor.getInstance().execute(new ClearCacheTask(MainActivity.this))
        );

        RecyclerView recyclerView = findViewById(R.id.recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CacheAdapter(this);
        recyclerView.setAdapter(adapter);

        bindViews();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void bindViews() {
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

}
