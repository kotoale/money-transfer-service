package com.task.rest.exceptions;

import javax.ws.rs.core.Response;

/**
 * @author Alexander Kotov (kotov.alex.22@gmail.com)
 */
public class NoSuchAccountException extends AbstractServiceException {
    public NoSuchAccountException(long id) {
        super(Response.Status.NOT_FOUND.getStatusCode(), String.format("There's no account with id: %d", id));
    }
}
