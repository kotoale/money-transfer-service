package com.task.rest.resource;

import com.task.rest.api.request.CreateAccountRequest;
import com.task.rest.api.request.DepositOrWithdrawRequest;
import com.task.rest.api.request.TransferRequest;
import com.task.rest.api.response.CrudAccountResponse;
import com.task.rest.api.response.ListAllResponse;
import com.task.rest.api.response.OperationStatus;
import com.task.rest.model.dbo.Account;
import com.task.rest.service.AccountService;
import io.dropwizard.hibernate.UnitOfWork;

import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Represents REST Service resource/controller with mappings http methods to corresponding implementations
 *
 * @author Alexander Kotov (kotov.alex.22@gmail.com)
 */

@Path("/accounts")
@Produces(MediaType.APPLICATION_JSON)
public class AccountServiceResource {

    public static final String INIT_AMOUNT_FIELD_NAME = "initialMoneyAmount";

    private final AccountService accountService;

    /**
     * default value of money amount for account creation
     */
    private final BigDecimal initialMoneyAmount;

    @Inject
    public AccountServiceResource(AccountService accountService, @Named(INIT_AMOUNT_FIELD_NAME) BigDecimal initialMoneyAmount) {
        this.accountService = accountService;
        this.initialMoneyAmount = initialMoneyAmount;
    }

    /**
     * Mapping for the HTTP POST method for create new account
     *
     * @param request {@link CreateAccountRequest}
     * @return {@link Response} object with http status Created and {@link CrudAccountResponse} in its body in case of successful completion
     * @see CreateAccountRequest
     * @see CrudAccountResponse
     */
    @POST
    @UnitOfWork
    @Path("/create")
    public Response create(@Valid CreateAccountRequest request) {
        BigDecimal reqInitValue = request != null ? request.getInitAmount() : null;
        final Account account = accountService.create(Optional.ofNullable(reqInitValue).orElse(initialMoneyAmount));
        return Response.status(Response.Status.CREATED)
                .entity(new CrudAccountResponse(account.getId(), account.getAmount(), OperationStatus.CREATED))
                .build();
    }

    /**
     * Mapping for the HTTP GET method for get list of all account
     *
     * @return {@link Response} object with http status Ok and {@link ListAllResponse} in its body in case of successful completion
     * @see ListAllResponse
     */
    @GET
    @UnitOfWork
    @Path("/list")
    public Response listAll() {
        List<Account> accounts = accountService.listAll();
        return Response.status(Response.Status.OK)
                .entity(new ListAllResponse(accounts))
                .build();
    }

    /**
     * Mapping for the HTTP GET method for get account with specified id
     *
     * @param id - specified account id
     * @return {@link Response} object with http status Ok and {@link CrudAccountResponse} in its body in case of successful completion
     * @see CrudAccountResponse
     */
    @GET
    @UnitOfWork
    public Response getById(@QueryParam("id") @NotNull Long id) {
        final Account account = accountService.get(id);
        return Response.status(Response.Status.OK)
                .entity(new CrudAccountResponse(account.getId(), account.getAmount(), OperationStatus.READ))
                .build();
    }

    /**
     * Mapping for the HTTP PUT method for withdraw money from the account
     *
     * @param request - {@link DepositOrWithdrawRequest}
     * @return {@link Response} object with http status Ok and {@link CrudAccountResponse} in its body in case of successful completion
     * @see DepositOrWithdrawRequest
     * @see CrudAccountResponse
     */
    @PUT
    @UnitOfWork
    @Path("/withdraw")
    public Response withdraw(@Valid @NotNull DepositOrWithdrawRequest request) {
        final Account account = accountService.withdraw(request.getId(), request.getAmount());
        return Response.status(Response.Status.OK)
                .entity(new CrudAccountResponse(account.getId(), account.getAmount(), OperationStatus.UPDATED))
                .build();
    }

    /**
     * Mapping for the HTTP PUT method for deposit money to the account
     *
     * @param request - {@link DepositOrWithdrawRequest}
     * @return {@link Response} object with http status Ok and {@link CrudAccountResponse} in its body in case of successful completion
     * @see DepositOrWithdrawRequest
     * @see CrudAccountResponse
     */
    @PUT
    @UnitOfWork
    @Path("/deposit")
    public Response deposit(@Valid @NotNull DepositOrWithdrawRequest request) {
        final Account account = accountService.deposit(request.getId(), request.getAmount());
        return Response.status(Response.Status.OK)
                .entity(new CrudAccountResponse(account.getId(), account.getAmount(), OperationStatus.UPDATED))
                .build();
    }

    /**
     * Mapping for the HTTP PUT method for transfer money from one account to another
     *
     * @param request - {@link TransferRequest}
     * @return {@link Response} object with http status Ok and {@link CrudAccountResponse} in its body in case of successful completion
     * @see TransferRequest
     * @see CrudAccountResponse
     */
    @PUT
    @UnitOfWork
    @Path("/transfer")
    public Response transfer(@Valid @NotNull TransferRequest request) {
        final Account account = accountService.transfer(request.getFromId(), request.getToId(), request.getAmount());
        return Response.status(Response.Status.OK)
                .entity(new CrudAccountResponse(account.getId(), account.getAmount(), OperationStatus.UPDATED))
                .build();
    }

    /**
     * Mapping for the HTTP DELETE method for delete specified account
     *
     * @param id - specified account id
     * @return {@link Response} object with http status Ok and {@link CrudAccountResponse} in its body in case of successful completion
     * @see CrudAccountResponse
     */
    @DELETE
    @UnitOfWork
    @Path("/delete")
    public Response delete(@QueryParam("id") @NotNull Long id) {
        final Account account = accountService.delete(id);
        return Response.status(Response.Status.OK)
                .entity(new CrudAccountResponse(account.getId(), account.getAmount(), OperationStatus.DELETED))
                .build();
    }
}
