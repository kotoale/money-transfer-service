package com.task.rest.exceptions;

/**
 * @author Alexander Kotov (kotov.alex.22@gmail.com)
 */
public abstract class AbstractServiceException extends Throwable {
    private int code;

    public AbstractServiceException(int code) {
        this(code, "Error while processing the request", null);
    }
    public AbstractServiceException(int code, String message) {
        this(code, message, null);
    }
    public AbstractServiceException(int code, String message, Throwable throwable) {
        super(message, throwable);
        this.code = code;
    }
    public int getCode() {
        return code;
    }
}
