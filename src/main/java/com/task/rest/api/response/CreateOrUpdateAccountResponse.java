package com.task.rest.api.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

/**
 * @author Alexander Kotov (kotov.alex.22@gmail.com)
 */
public class CreateOrUpdateAccountResponse {
    private final long id;
    private final BigDecimal amount;

    @JsonCreator
    public CreateOrUpdateAccountResponse(@JsonProperty("accountId") long id, @JsonProperty("moneyAmount") BigDecimal amount) {
        this.id = id;
        this.amount = amount;
    }

    public long getId() {
        return id;
    }

    public BigDecimal getAmount() {
        return amount;
    }
}
