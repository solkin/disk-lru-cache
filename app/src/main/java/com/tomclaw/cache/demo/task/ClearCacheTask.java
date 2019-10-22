package com.tomclaw.cache.demo.task;

import android.content.Context;

import com.tomclaw.cache.demo.MainActivity;
import com.tomclaw.cache.demo.executor.WeakObjectTask;

import java.io.IOException;

import static com.tomclaw.cache.demo.App.cache;

public class ClearCacheTask extends WeakObjectTask<MainActivity> {

    public ClearCacheTask(MainActivity activity) {
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
