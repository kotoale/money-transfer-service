package com.task.rest.resource;

import com.task.rest.api.response.CrudAccountResponse;
import com.task.rest.api.response.ListAllResponse;
import com.task.rest.api.response.OperationStatus;
import com.task.rest.exceptions.NoSuchAccountException;
import com.task.rest.exceptions.mappers.DefaultExceptionsProvider;
import com.task.rest.exceptions.mappers.ServiceExceptionsProvider;
import com.task.rest.model.dbo.Account;
import com.task.rest.service.AccountService;
import io.dropwizard.testing.junit.ResourceTestRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import javax.ws.rs.core.Response;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Alexander Kotov (kotov.alex.22@gmail.com)
 */
public class AccountServiceResourceTest {


    private static final AccountService accountService = mock(AccountService.class);

    @Rule
    public final ResourceTestRule resources = ResourceTestRule.builder()
            .addResource(new AccountServiceResource(accountService, BigDecimal.TEN))
            .addResource(new ServiceExceptionsProvider())
            .addResource(new DefaultExceptionsProvider())
            .build();

    @Before
    public void setup() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testGetByIdWithNullParameter() {
        Response response = resources.target("/accounts").queryParam("id").request().get();
        String expectedEntityAsString = "{\"errors\":[\"query param id may not be null\"]}";

        assertThat(response.readEntity(String.class)).isEqualTo(expectedEntityAsString);
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testGetByIdWithNotExistedId() {
        when(accountService.get(2L)).thenThrow(new NoSuchAccountException(2L));

        Response response = resources.target("/accounts").queryParam("id", 2L).request().get();
        String expectedEntityAsString = "There's no account with id: 2";

        assertThat(expectedEntityAsString).isEqualTo(response.readEntity(String.class));
        assertThat(Response.Status.BAD_REQUEST.getStatusCode()).isEqualTo(response.getStatus());
        verify(accountService).get(2L);
    }

    @Test
    public void testGetById() {
        Account account = new Account(1L, BigDecimal.TEN);
        when(accountService.get(1L)).thenReturn(account);

        CrudAccountResponse response = resources.target("/accounts").queryParam("id", 1).request().get().readEntity(CrudAccountResponse.class);
        CrudAccountResponse expectedResponse = new CrudAccountResponse(account.getId(), account.getAmount(), OperationStatus.READ);
        assertThat(response).isEqualToComparingFieldByField(expectedResponse);
        verify(accountService).get(1L);

    }

    @Test
    public void testCreateAccountWithEmptyBody() throws Exception {
        Account account = new Account(3L, BigDecimal.TEN);
        when(accountService.create(BigDecimal.TEN)).thenReturn(account);

        CrudAccountResponse response = resources.target("/accounts/create").request().post(null).readEntity(CrudAccountResponse.class);
        CrudAccountResponse expectedResponse = new CrudAccountResponse(account.getId(), account.getAmount(), OperationStatus.CREATED);
        assertThat(response).isEqualToComparingFieldByField(expectedResponse);
        verify(accountService).create(BigDecimal.TEN);
    }

    @Test
    public void testCreateAccountWithNullAmountInRequestBody() throws Exception {

    }

    @Test
    public void testCreateAccount() throws Exception {

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
    public void testWithdraw() throws Exception {

    }

    @Test
    public void testDeposit() throws Exception {

    }

    @Test
    public void testTransfer() throws Exception {

    }

    @Test
    public void testDelete() throws Exception {

    }

}