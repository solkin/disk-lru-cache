package com.tomclaw.cache.demo;

import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.tomclaw.cache.DiskLruCache;

import static com.tomclaw.cache.demo.App.cache;

public class MainActivity extends AppCompatActivity {

    private TextView cacheSizeView;
    private TextView usedSpaceView;
    private TextView freeSpaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cacheSizeView = findViewById(R.id.cache_size);
        usedSpaceView = findViewById(R.id.used_space);
        freeSpaceView = findViewById(R.id.free_space);

        bindViews();
    }

    private void bindViews() {
        DiskLruCache cache = cache();
        cacheSizeView.setText(formatBytes(cache.getCacheSize()));
        usedSpaceView.setText(formatBytes(cache.getUsedSpace()));
        freeSpaceView.setText(formatBytes(cache.getFreeSpace()));
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
