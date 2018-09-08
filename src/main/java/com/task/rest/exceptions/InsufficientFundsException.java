package com.task.rest.exceptions;

import javax.ws.rs.core.Response;
import java.math.BigDecimal;

/**
 * @author Alexander Kotov (kotov.alex.22@gmail.com)
 */
public class InsufficientFundsException extends AbstractServiceException {
    public InsufficientFundsException(BigDecimal currentAmount, BigDecimal amountToTransfer) {
        super(Response.Status.PRECONDITION_FAILED.getStatusCode(),
                String.format("Current amount (%s) is less than amount to withdraw/transfer (%s)", currentAmount.toPlainString(),
                        amountToTransfer.toPlainString()));
    }
}
