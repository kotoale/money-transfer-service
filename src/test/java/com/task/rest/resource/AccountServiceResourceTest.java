package com.task.rest.resource;

import com.task.rest.exceptions.InsufficientFundsException;
import com.task.rest.exceptions.NoSuchAccountException;
import com.task.rest.exceptions.TransferToTheSameAccountException;
import com.task.rest.exceptions.mappers.DefaultExceptionsProvider;
import com.task.rest.exceptions.mappers.ServiceExceptionsProvider;
import com.task.rest.model.api.request.CreateAccountRequest;
import com.task.rest.model.api.request.DepositOrWithdrawRequest;
import com.task.rest.model.api.request.TransferRequest;
import com.task.rest.model.api.response.CrudAccountResponse;
import com.task.rest.model.api.response.ListAllResponse;
import com.task.rest.model.api.response.OperationStatus;
import com.task.rest.model.dbo.Account;
import com.task.rest.service.AccountService;
import io.dropwizard.testing.junit.ResourceTestRule;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.stubbing.Answer;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

/**
 * @author Alexander Kotov (kotov.alex.22@gmail.com)
 */
public class AccountServiceResourceTest {

    private static AccountService accountService = mock(AccountService.class);
    private static final int HTTP_UNPROCESSABLE_ENTITY_CODE = 422;
    private static final BigDecimal DEFAULT_INIT_AMOUNT = BigDecimal.TEN;
    private static final Long DEFAULT_ID_FOR_CREATED_ACCOUNT = 1L;

    private static final String AMOUNT_NUMERIC_VALUE_VALIDATION_FAIL_MSG = "{\"errors\":[\"amount numeric value out of bounds (<37 digits>.<8 digits> expected)\"]}";
    private static final String AMOUNT_GREATER_OR_EQUAL_ZERO_VALIDATION_FAIL_MSG = "{\"errors\":[\"amount must be greater than or equal to 0.0\"]}";
    private static final String AMOUNT_GREATER_THAN_ZERO_VALIDATION_FAIL_MSG = "{\"errors\":[\"amount must be greater than 0.0\"]}";
    private static final String TRANSFER_MONEY_TO_THE_SAME_ACCOUNT_MSG = "Transfer money to the same account is forbidden";
    private static final String QUERY_PARAM_ID_NOT_NULL_VALIDATION_FAIL_MSG = "{\"errors\":[\"query param id may not be null\"]}";
    private static final String ID_NOT_NULL_VALIDATION_FAIL_MSG = "{\"errors\":[\"id may not be null\"]}";
    private static final String AMOUNT_NOT_NULL_VALIDATION_FAIL_MSG = "{\"errors\":[\"amount may not be null\"]}";


    @ClassRule
    public static final ResourceTestRule resources = ResourceTestRule.builder()
            .addResource(new AccountServiceResource(accountService, DEFAULT_INIT_AMOUNT))
            .addResource(new ServiceExceptionsProvider())
            .addResource(new DefaultExceptionsProvider())
            .build();

    @Before
    public void setup() {
        Answer<Account> answer = invocation -> {
            if (invocation == null) {
                return null;
            }
            Account account = invocation.getArgumentAt(0, Account.class);
            return account.setId(DEFAULT_ID_FOR_CREATED_ACCOUNT);
        };
        when(accountService.create(any(Account.class))).thenAnswer(answer);
    }

    @After
    public void tearDown() {
        reset(accountService);
    }

    @Test
    public void testGetById_WithNullId() {
        Response response = resources.target("/accounts").queryParam("id").request().get();

        verifyZeroInteractions(accountService);
        assertThat(response.readEntity(String.class)).isEqualTo(QUERY_PARAM_ID_NOT_NULL_VALIDATION_FAIL_MSG);
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testGetById_WithNotExistedId() {
        when(accountService.get(2L)).thenThrow(new NoSuchAccountException(2L));

        Response response = resources.target("/accounts").queryParam("id", 2L).request().get();
        String expectedEntityAsString = "There's no account with id: 2";

        assertThat(response.readEntity(String.class)).isEqualTo(expectedEntityAsString);
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        verify(accountService).get(2L);
    }

    @Test
    public void testGetById() {
        Account account = new Account(1L, BigDecimal.TEN);
        when(accountService.get(1L)).thenReturn(account);

        Response response = resources.target("/accounts").queryParam("id", 1).request().get();
        CrudAccountResponse crudAccountResponse = response.readEntity(CrudAccountResponse.class);
        CrudAccountResponse expectedResponse = new CrudAccountResponse(account, OperationStatus.READ);

        assertThat(crudAccountResponse).isEqualToComparingFieldByField(expectedResponse);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(accountService).get(1L);
    }

    @Test
    public void testCreate_WithNegativeAmount() throws Exception {
        CreateAccountRequest request = new CreateAccountRequest(new BigDecimal("-1"));

        Response response = resources.target("/accounts/create").request()
                .post(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE));

        verifyZeroInteractions(accountService);
        assertThat(response.readEntity(String.class)).isEqualTo(AMOUNT_GREATER_OR_EQUAL_ZERO_VALIDATION_FAIL_MSG);
        assertThat(response.getStatus()).isEqualTo(HTTP_UNPROCESSABLE_ENTITY_CODE);
    }

    @Test
    public void testCreate_WithNotValidAmount() throws Exception {
        CreateAccountRequest request = new CreateAccountRequest(new BigDecimal("0.000000001"));

        Response response = resources.target("/accounts/create").request()
                .post(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE));

        verifyZeroInteractions(accountService);
        assertThat(response.readEntity(String.class)).isEqualTo(AMOUNT_NUMERIC_VALUE_VALIDATION_FAIL_MSG);
        assertThat(response.getStatus()).isEqualTo(HTTP_UNPROCESSABLE_ENTITY_CODE);
    }

    @Test
    public void testCreate_WithEmptyBody() throws Exception {
        Account expectedAccount = new Account(DEFAULT_ID_FOR_CREATED_ACCOUNT, DEFAULT_INIT_AMOUNT);

        Response response = resources.target("/accounts/create").request().post(null);
        CrudAccountResponse crudAccountResponse = response.readEntity(CrudAccountResponse.class);
        CrudAccountResponse expectedResponse = new CrudAccountResponse(expectedAccount, OperationStatus.CREATED);

        assertThat(crudAccountResponse).isEqualToComparingFieldByField(expectedResponse);
        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
        verify(accountService).create(any(Account.class));
    }

    @Test
    public void testCreate_WithNullAmountInRequestBody() throws Exception {
        Account expectedAccount = new Account(DEFAULT_ID_FOR_CREATED_ACCOUNT, DEFAULT_INIT_AMOUNT);
        CreateAccountRequest request = new CreateAccountRequest(null);

        Response response = resources.target("/accounts/create").request()
                .post(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE));
        CrudAccountResponse crudAccountResponse = response.readEntity(CrudAccountResponse.class);
        CrudAccountResponse expectedResponse = new CrudAccountResponse(expectedAccount, OperationStatus.CREATED);

        assertThat(crudAccountResponse).isEqualToComparingFieldByField(expectedResponse);
        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
        verify(accountService).create(any(Account.class));
    }

    @Test
    public void testCreate() throws Exception {
        BigDecimal amount = new BigDecimal("123.45");
        Account expectedAccount = new Account(DEFAULT_ID_FOR_CREATED_ACCOUNT, amount);
        CreateAccountRequest request = new CreateAccountRequest(amount);

        Response response = resources.target("/accounts/create").request()
                .post(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE));

        CrudAccountResponse crudAccountResponse = response.readEntity(CrudAccountResponse.class);
        CrudAccountResponse expectedResponse = new CrudAccountResponse(expectedAccount, OperationStatus.CREATED);

        assertThat(crudAccountResponse).isEqualToComparingFieldByField(expectedResponse);
        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
        verify(accountService).create(any(Account.class));
    }

    @Test
    public void testListAll() throws Exception {
        List<Account> accounts = new ArrayList<>();
        accounts.add(new Account(3L, new BigDecimal("100.03")));
        accounts.add(new Account(4L, new BigDecimal("190.07")));
        accounts.add(new Account(5L, new BigDecimal("100.0345")));
        when(accountService.listAll()).thenReturn(accounts);

        ListAllResponse response = resources.target("/accounts/list").request().get().readEntity(ListAllResponse.class);

        ListAllResponse expectedResponse = new ListAllResponse(accounts);
        assertThat(response).isEqualToComparingFieldByField(expectedResponse);
        verify(accountService).listAll();
    }

    @Test
    public void testWithdraw_WithNullId() throws Exception {
        DepositOrWithdrawRequest request = new DepositOrWithdrawRequest(null, BigDecimal.ONE);

        Response response = resources.target("/accounts/withdraw").request()
                .put(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE));

        verifyZeroInteractions(accountService);
        assertThat(response.readEntity(String.class)).isEqualTo(ID_NOT_NULL_VALIDATION_FAIL_MSG);
        assertThat(response.getStatus()).isEqualTo(HTTP_UNPROCESSABLE_ENTITY_CODE);
    }

    @Test
    public void testWithdraw_WithNullAmount() throws Exception {
        DepositOrWithdrawRequest request = new DepositOrWithdrawRequest(1L, null);

        Response response = resources.target("/accounts/withdraw").request()
                .put(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE));

        verifyZeroInteractions(accountService);
        assertThat(response.readEntity(String.class)).isEqualTo(AMOUNT_NOT_NULL_VALIDATION_FAIL_MSG);
        assertThat(response.getStatus()).isEqualTo(HTTP_UNPROCESSABLE_ENTITY_CODE);
    }

    @Test
    public void testWithdraw_WithNegativeAmount() throws Exception {
        DepositOrWithdrawRequest request = new DepositOrWithdrawRequest(1L, new BigDecimal("-1"));

        Response response = resources.target("/accounts/withdraw").request()
                .put(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE));

        verifyZeroInteractions(accountService);
        assertThat(response.readEntity(String.class)).isEqualTo(AMOUNT_GREATER_THAN_ZERO_VALIDATION_FAIL_MSG);
        assertThat(response.getStatus()).isEqualTo(HTTP_UNPROCESSABLE_ENTITY_CODE);
    }

    @Test
    public void testWithdraw_WithZeroAmount() throws Exception {
        DepositOrWithdrawRequest request = new DepositOrWithdrawRequest(1L, BigDecimal.ZERO);

        Response response = resources.target("/accounts/withdraw").request()
                .put(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE));

        verifyZeroInteractions(accountService);
        assertThat(response.readEntity(String.class)).isEqualTo(AMOUNT_GREATER_THAN_ZERO_VALIDATION_FAIL_MSG);
        assertThat(response.getStatus()).isEqualTo(HTTP_UNPROCESSABLE_ENTITY_CODE);
    }

    @Test
    public void testWithdraw_WithNotValidAmount() throws Exception {
        DepositOrWithdrawRequest request = new DepositOrWithdrawRequest(1L, new BigDecimal("0.123456789"));

        Response response = resources.target("/accounts/withdraw").request()
                .put(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE));

        verifyZeroInteractions(accountService);
        assertThat(response.readEntity(String.class)).isEqualTo(AMOUNT_NUMERIC_VALUE_VALIDATION_FAIL_MSG);
        assertThat(response.getStatus()).isEqualTo(HTTP_UNPROCESSABLE_ENTITY_CODE);
    }

    @Test
    public void testWithdraw_WithNotExistedId() throws Exception {
        BigDecimal amount = new BigDecimal("0.12345678");
        DepositOrWithdrawRequest request = new DepositOrWithdrawRequest(1L, amount);
        when(accountService.withdraw(1L, amount)).thenThrow(new NoSuchAccountException(1L));

        Response response = resources.target("/accounts/withdraw").request()
                .put(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE));

        verify(accountService).withdraw(1L, amount);
        assertThat(response.readEntity(String.class)).isEqualTo("There's no account with id: 1");
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testWithdraw_WithInsufficientFunds() throws Exception {
        BigDecimal amount = new BigDecimal("0.12345678");
        DepositOrWithdrawRequest request = new DepositOrWithdrawRequest(1L, amount);
        when(accountService.withdraw(1L, amount)).thenThrow(new InsufficientFundsException(BigDecimal.ZERO, amount, 1L));

        Response response = resources.target("/accounts/withdraw").request()
                .put(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE));

        String expectedEntityAsString = "Current amount (0) is less than amount to withdraw (0.12345678) for the account with id = 1";

        verify(accountService).withdraw(1L, amount);
        assertThat(response.readEntity(String.class)).isEqualTo(expectedEntityAsString);
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testWithdraw() throws Exception {
        BigDecimal amount = new BigDecimal("0.12345678");
        Account expectedAccount = new Account(1L, BigDecimal.TEN);
        DepositOrWithdrawRequest request = new DepositOrWithdrawRequest(1L, amount);
        when(accountService.withdraw(1L, amount)).thenReturn(expectedAccount);

        Response response = resources.target("/accounts/withdraw").request()
                .put(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE));

        CrudAccountResponse crudAccountResponse = response.readEntity(CrudAccountResponse.class);
        CrudAccountResponse expectedResponse = new CrudAccountResponse(expectedAccount, OperationStatus.UPDATED);

        assertThat(crudAccountResponse).isEqualToComparingFieldByField(expectedResponse);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(accountService).withdraw(1L, amount);
    }

    @Test
    public void testDeposit_WithNullId() throws Exception {
        DepositOrWithdrawRequest request = new DepositOrWithdrawRequest(null, BigDecimal.ONE);

        Response response = resources.target("/accounts/deposit").request()
                .put(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE));

        verifyZeroInteractions(accountService);
        assertThat(response.readEntity(String.class)).isEqualTo(ID_NOT_NULL_VALIDATION_FAIL_MSG);
        assertThat(response.getStatus()).isEqualTo(HTTP_UNPROCESSABLE_ENTITY_CODE);
    }

    @Test
    public void testDeposit_WithNullAmount() throws Exception {
        DepositOrWithdrawRequest request = new DepositOrWithdrawRequest(1L, null);

        Response response = resources.target("/accounts/deposit").request()
                .put(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE));

        verifyZeroInteractions(accountService);
        assertThat(response.readEntity(String.class)).isEqualTo(AMOUNT_NOT_NULL_VALIDATION_FAIL_MSG);
        assertThat(response.getStatus()).isEqualTo(HTTP_UNPROCESSABLE_ENTITY_CODE);
    }

    @Test
    public void testDeposit_WithNegativeAmount() throws Exception {
        DepositOrWithdrawRequest request = new DepositOrWithdrawRequest(1L, new BigDecimal("-1"));

        Response response = resources.target("/accounts/deposit").request()
                .put(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE));

        verifyZeroInteractions(accountService);
        assertThat(response.readEntity(String.class)).isEqualTo(AMOUNT_GREATER_THAN_ZERO_VALIDATION_FAIL_MSG);
        assertThat(response.getStatus()).isEqualTo(HTTP_UNPROCESSABLE_ENTITY_CODE);
    }

    @Test
    public void testDeposit_WithZeroAmount() throws Exception {
        DepositOrWithdrawRequest request = new DepositOrWithdrawRequest(1L, BigDecimal.ZERO);

        Response response = resources.target("/accounts/deposit").request()
                .put(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE));

        verifyZeroInteractions(accountService);
        assertThat(response.readEntity(String.class)).isEqualTo(AMOUNT_GREATER_THAN_ZERO_VALIDATION_FAIL_MSG);
        assertThat(response.getStatus()).isEqualTo(HTTP_UNPROCESSABLE_ENTITY_CODE);

    }

    @Test
    public void testDeposit_WithNotValidAmount() throws Exception {
        DepositOrWithdrawRequest request = new DepositOrWithdrawRequest(1L, new BigDecimal("0.123456789"));

        Response response = resources.target("/accounts/deposit").request()
                .put(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE));

        verifyZeroInteractions(accountService);
        assertThat(response.readEntity(String.class)).isEqualTo(AMOUNT_NUMERIC_VALUE_VALIDATION_FAIL_MSG);
        assertThat(response.getStatus()).isEqualTo(HTTP_UNPROCESSABLE_ENTITY_CODE);

    }

    @Test
    public void testDeposit_WithNotExistedId() throws Exception {
        BigDecimal amount = new BigDecimal("0.12345678");
        DepositOrWithdrawRequest request = new DepositOrWithdrawRequest(1L, amount);
        when(accountService.deposit(1L, amount)).thenThrow(new NoSuchAccountException(1L));

        Response response = resources.target("/accounts/deposit").request()
                .put(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE));

        verify(accountService).deposit(1L, amount);
        assertThat(response.readEntity(String.class)).isEqualTo("There's no account with id: 1");
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testDeposit() throws Exception {
        BigDecimal amount = new BigDecimal("0.12345678");
        Account expectedAccount = new Account(1L, BigDecimal.TEN);
        DepositOrWithdrawRequest request = new DepositOrWithdrawRequest(1L, amount);
        when(accountService.deposit(1L, amount)).thenReturn(expectedAccount);

        Response response = resources.target("/accounts/deposit").request()
                .put(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE));
        CrudAccountResponse crudAccountResponse = response.readEntity(CrudAccountResponse.class);

        CrudAccountResponse expectedResponse = new CrudAccountResponse(expectedAccount, OperationStatus.UPDATED);

        assertThat(crudAccountResponse).isEqualToComparingFieldByField(expectedResponse);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(accountService).deposit(1L, amount);
    }

    @Test
    public void testTransfer_WithNullFromId() throws Exception {
        TransferRequest request = new TransferRequest(null, 2L, BigDecimal.ONE);

        Response response = resources.target("/accounts/transfer").request()
                .put(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE));

        verifyZeroInteractions(accountService);
        assertThat(response.readEntity(String.class)).isEqualTo("{\"errors\":[\"fromId may not be null\"]}");
        assertThat(response.getStatus()).isEqualTo(HTTP_UNPROCESSABLE_ENTITY_CODE);
    }

    @Test
    public void testTransfer_WithNullToId() throws Exception {
        TransferRequest request = new TransferRequest(1L, null, BigDecimal.ONE);

        Response response = resources.target("/accounts/transfer").request()
                .put(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE));

        verifyZeroInteractions(accountService);
        assertThat(response.readEntity(String.class)).isEqualTo("{\"errors\":[\"toId may not be null\"]}");
        assertThat(response.getStatus()).isEqualTo(HTTP_UNPROCESSABLE_ENTITY_CODE);
    }

    @Test
    public void testTransfer_WithNullAmount() throws Exception {
        TransferRequest request = new TransferRequest(1L, 2L, null);

        Response response = resources.target("/accounts/transfer").request()
                .put(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE));

        verifyZeroInteractions(accountService);
        assertThat(response.readEntity(String.class)).isEqualTo(AMOUNT_NOT_NULL_VALIDATION_FAIL_MSG);
        assertThat(response.getStatus()).isEqualTo(HTTP_UNPROCESSABLE_ENTITY_CODE);
    }

    @Test
    public void testTransfer_WithNegativeAmount() throws Exception {
        TransferRequest request = new TransferRequest(1L, 2L, new BigDecimal("-10"));

        Response response = resources.target("/accounts/transfer").request()
                .put(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE));

        verifyZeroInteractions(accountService);
        assertThat(response.readEntity(String.class)).isEqualTo(AMOUNT_GREATER_THAN_ZERO_VALIDATION_FAIL_MSG);
        assertThat(response.getStatus()).isEqualTo(HTTP_UNPROCESSABLE_ENTITY_CODE);

    }

    @Test
    public void testTransfer_WithZeroAmount() throws Exception {
        TransferRequest request = new TransferRequest(1L, 2L, BigDecimal.ZERO);

        Response response = resources.target("/accounts/transfer").request()
                .put(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE));

        verifyZeroInteractions(accountService);
        assertThat(response.readEntity(String.class)).isEqualTo(AMOUNT_GREATER_THAN_ZERO_VALIDATION_FAIL_MSG);
        assertThat(response.getStatus()).isEqualTo(HTTP_UNPROCESSABLE_ENTITY_CODE);
    }

    @Test
    public void testTransfer_WithNotValidAmount() throws Exception {
        TransferRequest request = new TransferRequest(1L, 2L, new BigDecimal("0.123456789"));

        Response response = resources.target("/accounts/transfer").request()
                .put(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE));

        verifyZeroInteractions(accountService);
        assertThat(response.readEntity(String.class)).isEqualTo(AMOUNT_NUMERIC_VALUE_VALIDATION_FAIL_MSG);
        assertThat(response.getStatus()).isEqualTo(HTTP_UNPROCESSABLE_ENTITY_CODE);
    }

    @Test
    public void testTransfer_WithNotExistedId() throws Exception {
        BigDecimal amount = new BigDecimal("0.12345678");
        TransferRequest request = new TransferRequest(1L, 2L, amount);
        when(accountService.transfer(1L, 2L, amount)).thenThrow(new NoSuchAccountException(1L));

        Response response = resources.target("/accounts/transfer").request()
                .put(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE));

        verify(accountService).transfer(1L, 2L, amount);
        assertThat(response.readEntity(String.class)).isEqualTo("There's no account with id: 1");
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testTransfer_WithInsufficientFunds() throws Exception {
        BigDecimal amount = new BigDecimal("0.12345678");
        TransferRequest request = new TransferRequest(1L, 2L, amount);
        when(accountService.transfer(1L, 2L, amount)).thenThrow(new InsufficientFundsException(BigDecimal.ZERO, amount, 1L));

        Response response = resources.target("/accounts/transfer").request()
                .put(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE));

        String expectedEntityAsString = "Current amount (0) is less than amount to withdraw (0.12345678) for the account with id = 1";

        verify(accountService).transfer(1L, 2L, amount);
        assertThat(response.readEntity(String.class)).isEqualTo(expectedEntityAsString);
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testTransfer_WithTransferToTheSameAccount() throws Exception {
        BigDecimal amount = new BigDecimal("0.12345678");
        TransferRequest request = new TransferRequest(1L, 2L, amount);
        when(accountService.transfer(1L, 2L, amount)).thenThrow(new TransferToTheSameAccountException());

        Response response = resources.target("/accounts/transfer").request()
                .put(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE));

        verify(accountService).transfer(1L, 2L, amount);
        assertThat(response.readEntity(String.class)).isEqualTo(TRANSFER_MONEY_TO_THE_SAME_ACCOUNT_MSG);
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testTransfer() throws Exception {
        BigDecimal amount = new BigDecimal("0.12345678");
        Account expectedAccount = new Account(1L, BigDecimal.TEN);
        TransferRequest request = new TransferRequest(1L, 2L, amount);
        when(accountService.transfer(1L, 2L, amount)).thenReturn(expectedAccount);

        Response response = resources.target("/accounts/transfer").request()
                .put(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE));
        CrudAccountResponse crudAccountResponse = response.readEntity(CrudAccountResponse.class);
        CrudAccountResponse expectedResponse = new CrudAccountResponse(expectedAccount, OperationStatus.UPDATED);

        verify(accountService).transfer(1L, 2L, amount);
        assertThat(crudAccountResponse).isEqualToComparingFieldByField(expectedResponse);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void testDelete_WithNullId() throws Exception {
        Response response = resources.target("/accounts/delete").queryParam("id").request().delete();

        verifyZeroInteractions(accountService);
        assertThat(response.readEntity(String.class)).isEqualTo(QUERY_PARAM_ID_NOT_NULL_VALIDATION_FAIL_MSG);
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testDelete_WithNotExistedId() throws Exception {
        when(accountService.delete(2L)).thenThrow(new NoSuchAccountException(2L));

        Response response = resources.target("/accounts/delete").queryParam("id", 2L).request().delete();
        String expectedEntityAsString = "There's no account with id: 2";

        assertThat(response.readEntity(String.class)).isEqualTo(expectedEntityAsString);
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        verify(accountService).delete(2L);
    }

    @Test
    public void testDelete() throws Exception {
        Account account = new Account(1L, BigDecimal.TEN);
        when(accountService.delete(1L)).thenReturn(account);

        Response response = resources.target("/accounts/delete").queryParam("id", 1).request().delete();
        CrudAccountResponse crudAccountResponse = response.readEntity(CrudAccountResponse.class);
        CrudAccountResponse expectedResponse = new CrudAccountResponse(account, OperationStatus.DELETED);
        assertThat(crudAccountResponse).isEqualToComparingFieldByField(expectedResponse);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(accountService).delete(1L);
    }
}