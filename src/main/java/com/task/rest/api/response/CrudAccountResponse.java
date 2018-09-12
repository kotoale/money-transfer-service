package com.task.rest.api.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.task.rest.utils.serialization.BigDecimalSerializer;

import java.math.BigDecimal;

/**
 * This class represents a response to a client request for create/read/update/delete account
 *
 * @author Alexander Kotov (kotov.alex.22@gmail.com)
 */
public class CrudAccountResponse {
    private final long id;
    @JsonSerialize(using = BigDecimalSerializer.class)
    private final BigDecimal amount;

    private final OperationStatus operationStatus;

    @JsonCreator
    public CrudAccountResponse(long id, BigDecimal amount, OperationStatus operationStatus) {
        this.id = id;
        this.amount = amount;
        this.operationStatus = operationStatus;
    }

    public long getId() {
        return id;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public OperationStatus getOperationStatus() {
        return operationStatus;
    }
}
