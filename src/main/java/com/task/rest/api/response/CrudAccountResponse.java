package com.task.rest.api.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.task.rest.utils.serialization.BigDecimalSerializer;

import java.math.BigDecimal;

/**
 * Represents a response to a client request for create/read/saveOrUpdate/delete account
 *
 * @author Alexander Kotov (kotov.alex.22@gmail.com)
 */
public class CrudAccountResponse {
    private final long id;
    @JsonSerialize(using = BigDecimalSerializer.class)
    private final BigDecimal amount;

    private final OperationStatus status;

    @JsonCreator
    public CrudAccountResponse(@JsonProperty("id") long id, @JsonProperty("amount") BigDecimal amount, @JsonProperty("status") OperationStatus status) {
        this.id = id;
        this.amount = amount;
        this.status = status;
    }

    public long getId() {
        return id;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public OperationStatus getStatus() {
        return status;
    }
}
