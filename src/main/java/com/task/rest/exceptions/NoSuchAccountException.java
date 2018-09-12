package com.task.rest.exceptions;

import javax.ws.rs.core.Response;

/**
 * Service specific exception - thrown when requested an operation with not existed account
 *
 * @author Alexander Kotov (kotov.alex.22@gmail.com)
 */
public class NoSuchAccountException extends AbstractServiceException {
    public NoSuchAccountException(long id) {
        super(Response.Status.BAD_REQUEST.getStatusCode(), String.format("There's no account with id: %d", id));
    }
}
