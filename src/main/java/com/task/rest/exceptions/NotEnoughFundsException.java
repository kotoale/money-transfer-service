package com.task.rest.exceptions;

import javax.ws.rs.core.Response;
import java.math.BigDecimal;

/**
 * @author Alexander Kotov (kotov.alex.22@gmail.com)
 */
public class NotEnoughFundsException extends AbstractServiceException {
    public NotEnoughFundsException(BigDecimal currentAmount, BigDecimal amountToTransfer) {
        super(Response.Status.PRECONDITION_FAILED.getStatusCode(), String.format("Current amount (%s) is less than amount to transfer (%s)", currentAmount, amountToTransfer));
    }
}
