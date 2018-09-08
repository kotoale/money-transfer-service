package com.task.rest.exceptions;

import javax.ws.rs.core.Response;

/**
 * @author Alexander Kotov (kotov.alex.22@gmail.com)
 */
public class TransferToTheSameAccountException extends AbstractServiceException {
    public TransferToTheSameAccountException() {
        super(Response.Status.PRECONDITION_FAILED.getStatusCode(), "Transfer to the same account is forbidden");
    }
}
