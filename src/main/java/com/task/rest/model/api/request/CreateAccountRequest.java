package com.task.rest.model.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import java.math.BigDecimal;

import static com.task.rest.model.dbo.Account.PRECISION;
import static com.task.rest.model.dbo.Account.SCALE;

/**
 * Represents a client request for account creation
 *
 * @author Alexander Kotov (kotov.alex.22@gmail.com)
 */
public class CreateAccountRequest {
    @DecimalMin("0.0")
    @Digits(integer = PRECISION, fraction = SCALE)
    private BigDecimal amount;

    public CreateAccountRequest() {
    }

    @JsonProperty
    public BigDecimal getAmount() {
        return amount;
    }

    public CreateAccountRequest(BigDecimal amount) {
        this.amount = amount;
    }

}
