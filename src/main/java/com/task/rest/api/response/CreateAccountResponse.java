package com.task.rest.api.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Alexander Kotov (kotov.alex.22@gmail.com)
 */
public class CreateAccountResponse {
    private final long accountID;

    @JsonCreator
    public CreateAccountResponse(@JsonProperty("accountID") long accountID) {
        this.accountID = accountID;
    }
}
