package com.dianping.zebra.exception;

/**
 * Created by root on 16-12-22.
 */
public class ZebraTransportException extends ZebraException{

    private static final long serialVersionUID = 2557173153784247508L;

    public ZebraTransportException() {
        super();
    }

    public ZebraTransportException(String message) {
        super(message);
    }

    public ZebraTransportException(String message, Throwable cause) {
        super(message, cause);
    }

    public ZebraTransportException(Throwable cause) {
        super(cause);
    }
}
