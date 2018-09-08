package com.task.rest.resource;

import com.task.rest.api.request.CreateAccountRequest;
import com.task.rest.api.response.CreateAccountResponse;
import com.task.rest.model.dbo.Account;
import com.task.rest.service.AccountService;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.math.BigDecimal;
import java.util.Optional;

/**
 * @author Alexander Kotov (kotov.alex.22@gmail.com)
 */

@Path("/account-resource")
@Produces(MediaType.APPLICATION_JSON)
public class AccountServiceResource {

    private final AccountService accountService;

    private final BigDecimal initialMoneyAmount;

    public AccountServiceResource(AccountService accountService, BigDecimal initialMoneyAmount) {
        this.accountService = accountService;
        this.initialMoneyAmount = initialMoneyAmount;
    }

    @POST
    @Path("/create")
    public Response create(@Valid @NotNull CreateAccountRequest createAccountRequest) {
        final Long accountId = accountService.create(new Account(Optional.ofNullable(createAccountRequest.getMoneyAmount()).orElse(initialMoneyAmount)));
        return Response.status(Response.Status.CREATED)
                .entity(new CreateAccountResponse(accountId))
                .build();
    }
}
