package com.task.rest.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

import static com.task.rest.model.dbo.Account.PRECISION;
import static com.task.rest.model.dbo.Account.SCALE;

/**
 * Represents a client request for withdraw(deposit) money from(to) account
 *
 * @author Alexander Kotov (kotov.alex.22@gmail.com)
 */
public class DepositOrWithdrawRequest {
    @NotNull
    private Long id;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    @Digits(integer = PRECISION, fraction = SCALE)
    private BigDecimal amount;

    @JsonProperty
    public Long getId() {
        return id;
    }

    @JsonProperty
    public BigDecimal getAmount() {
        return amount;
    }
}
