package com.task.rest.model.api.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.task.rest.model.dbo.Account;

/**
 * Represents a response to a client request for create/read/saveOrUpdate/delete account
 *
 * @author Alexander Kotov (kotov.alex.22@gmail.com)
 */
public class CrudAccountResponse {
    private final Account account;

    private final OperationStatus status;

    @JsonCreator
    public CrudAccountResponse(@JsonProperty("account") Account account, @JsonProperty("status") OperationStatus status) {
        this.account = account;
        this.status = status;
    }

    public Account getAccount() {
        return account;
    }

    public OperationStatus getStatus() {
        return status;
    }

}
