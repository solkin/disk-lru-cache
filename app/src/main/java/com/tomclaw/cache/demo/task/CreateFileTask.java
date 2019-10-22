package com.tomclaw.cache.demo.task;

import android.content.Context;

import com.tomclaw.cache.demo.MainActivity;
import com.tomclaw.cache.demo.Randomizer;
import com.tomclaw.cache.demo.executor.WeakObjectTask;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import static com.tomclaw.cache.demo.App.cache;

public class CreateFileTask extends WeakObjectTask<MainActivity> {

    public CreateFileTask(MainActivity activity) {
        super(activity);
    }

    @SuppressWarnings("TryFinallyCanBeTryWithResources")
    @Override
    public void executeBackground() throws Throwable {
        Context context = getWeakObject();
        if (context != null) {
            String extension = Randomizer.generateRandomString(3);
            File file = File.createTempFile("rnd", "." + extension);
            DataOutputStream stream = null;
            try {
                stream = new DataOutputStream(new FileOutputStream(file));
                int blocks = 2000 + Randomizer.random.nextInt(6000);
                for (int c = 0; c < blocks; c++) {
                    stream.writeLong(Randomizer.random.nextLong());
                    stream.flush();
                }
            } finally {
                if (stream != null) {
                    stream.close();
                }
            }
            String key = Randomizer.generateRandomString();
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
