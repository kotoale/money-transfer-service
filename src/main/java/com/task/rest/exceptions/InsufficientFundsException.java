package com.task.rest.exceptions;

import javax.ws.rs.core.Response;
import java.math.BigDecimal;

/**
 * Service specific exception - thrown when it is impossible to withdraw money from an account
 *
 * @author Alexander Kotov (kotov.alex.22@gmail.com)
 */
public class InsufficientFundsException extends AbstractServiceException {
    public InsufficientFundsException(BigDecimal currentAmount, BigDecimal amountToWithdraw, Long id) {
        super(Response.Status.BAD_REQUEST.getStatusCode(),
                String.format("Current amount (%s) is less than amount to withdraw (%s) for the account with id = %d", currentAmount.toPlainString(),
                        amountToWithdraw.toPlainString(), id));
    }
}
