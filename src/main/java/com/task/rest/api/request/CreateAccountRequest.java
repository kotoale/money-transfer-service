package com.task.rest.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import java.math.BigDecimal;

/**
 * This class represents a client request for account creation
 *
 * @author Alexander Kotov (kotov.alex.22@gmail.com)
 */
public class CreateAccountRequest {
    @DecimalMin("0.0")
    @Digits(integer = 37, fraction = 8)
    private BigDecimal initAmount;

    public CreateAccountRequest() {
    }

    @JsonProperty
    public BigDecimal getInitAmount() {
        return initAmount;
    }
}
