package com.task.rest.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.DecimalMin;
import java.math.BigDecimal;

/**
 * @author Alexander Kotov (kotov.alex.22@gmail.com)
 */
public class CreateAccountRequest {
    @DecimalMin("0.0")
    private BigDecimal initAmount;

    public CreateAccountRequest() {
    }

    @JsonProperty
    public BigDecimal getInitAmount() {
        return initAmount;
    }
}
