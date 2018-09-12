package com.task.rest.exceptions;

import javax.ws.rs.core.Response;

/**
 * Service specific exception - thrown when requested  money transfer to the same account
 *
 * @author Alexander Kotov (kotov.alex.22@gmail.com)
 */
public class TransferToTheSameAccountException extends AbstractServiceException {
    public TransferToTheSameAccountException() {
        super(Response.Status.BAD_REQUEST.getStatusCode(), "Transfer money to the same account is forbidden");
    }
}
