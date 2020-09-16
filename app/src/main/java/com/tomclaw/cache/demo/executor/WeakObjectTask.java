package com.tomclaw.cache.demo.executor;

import java.lang.ref.WeakReference;

@SuppressWarnings({"WeakerAccess"})
public abstract class WeakObjectTask<W> extends Task {

    private final WeakReference<W> weakObject;

    public WeakObjectTask(W object) {
        this.weakObject = new WeakReference<>(object);
    }

    public W getWeakObject() {
        return weakObject.get();
    }

}
