package com.task.rest.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * @author Alexander Kotov (kotov.alex.22@gmail.com)
 */
public class CreateAccountRequest {
    //@NotNull
    @DecimalMin("0.0")
    private BigDecimal moneyAmount;

    public CreateAccountRequest() {
    }

    @JsonProperty
    public BigDecimal getMoneyAmount() {
        return moneyAmount;
    }
}
