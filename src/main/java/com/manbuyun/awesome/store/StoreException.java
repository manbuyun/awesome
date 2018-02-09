package com.manbuyun.awesome.store;

/**
 * User: cs
 * Date: 2018-02-02
 */
public class StoreException extends RuntimeException {

    public StoreException() {
        super();
    }

    public StoreException(String message) {
        super(message);
    }

    public StoreException(Throwable cause) {
        super(cause);
    }

    public StoreException(String message, Throwable cause) {
        super(message, cause);
    }
}