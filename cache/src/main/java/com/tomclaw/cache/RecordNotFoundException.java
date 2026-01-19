package com.tomclaw.cache;

@SuppressWarnings("WeakerAccess")
public class RecordNotFoundException extends Exception {

    public RecordNotFoundException() {
        super("Record not found in cache");
    }

}
