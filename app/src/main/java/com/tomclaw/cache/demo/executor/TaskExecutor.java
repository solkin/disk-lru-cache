package com.tomclaw.cache.demo.executor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SuppressWarnings({"WeakerAccess"})
public class TaskExecutor {

    private final ExecutorService threadExecutor = Executors.newSingleThreadExecutor();

    private static class Holder {

        static TaskExecutor instance = new TaskExecutor();

    }

    public static TaskExecutor getInstance() {
        return Holder.instance;
    }

    public void execute(final Task task) {
        if (task.isPreExecuteRequired()) {
            MainExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    task.onPreExecuteMain();
                    threadExecutor.submit(task);
                }
            });
        } else {
            threadExecutor.submit(task);
        }
    }

}
