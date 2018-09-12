package com.task.rest.api.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.task.rest.model.dbo.Account;

import java.util.List;

/**
 * This class represents a response to a client request to list all accounts
 *
 * @author Alexander Kotov (kotov.alex.22@gmail.com)
 */
public class ListAllResponse {
    private final List<Account> accounts;

    @JsonCreator
    public ListAllResponse(@JsonProperty("accounts") List<Account> accounts) {
        this.accounts = accounts;
    }

    public List<Account> getAccounts() {
        return accounts;
    }
}
