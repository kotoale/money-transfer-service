package com.task.rest.exceptions;

/**
 * Represents service specific exceptions
 * that mapped by {@link com.task.rest.exceptions.mappers.ServiceExceptionsProvider} to {@link javax.ws.rs.core.Response}
 *
 * @author Alexander Kotov (kotov.alex.22@gmail.com)
 * @see com.task.rest.exceptions.mappers.ServiceExceptionsProvider
 */
public abstract class AbstractServiceException extends RuntimeException {
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
