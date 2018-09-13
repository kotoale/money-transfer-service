package com.task.rest.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Objects;

import static com.task.rest.model.dbo.Account.PRECISION;
import static com.task.rest.model.dbo.Account.SCALE;

/**
 * Represents a client request for transfer money from one account to another
 *
 * @author Alexander Kotov (kotov.alex.22@gmail.com)
 */
public class TransferRequest {
    @NotNull
    private Long fromId;

    @NotNull
    private Long toId;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    @Digits(integer = PRECISION, fraction = SCALE)
    private BigDecimal amount;

    @JsonProperty
    public Long getFromId() {
        return fromId;
    }

    @JsonProperty
    public Long getToId() {
        return toId;
    }

    @JsonProperty
    public BigDecimal getAmount() {
        return amount;
    }

    public TransferRequest() {
    }

    public TransferRequest(Long fromId, Long toId, BigDecimal amount) {
        this.fromId = fromId;
        this.toId = toId;
        this.amount = amount;
    }
}
