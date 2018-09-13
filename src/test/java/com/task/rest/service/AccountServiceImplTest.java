package com.task.rest.service;

import com.task.rest.exceptions.InsufficientFundsException;
import com.task.rest.exceptions.NoSuchAccountException;
import com.task.rest.exceptions.TransferToTheSameAccountException;
import com.task.rest.model.dao.AccountDao;
import com.task.rest.model.dbo.Account;
import com.task.rest.utils.concurrency.ConcurrentCache;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

/**
 * @author Alexander Kotov (kotov.alex.22@gmail.com)
 */
public class AccountServiceImplTest {

    private AccountServiceImpl accountService;

    private CacheBuilder cacheBuilder = new CacheBuilder();

    private final class CacheBuilder {
        ConcurrentCache<Long, Lock> buildCache() {
            ConcurrentCache<Long, Lock> cache = mock(ConcurrentCache.class);

            Answer<Lock> answer = invocation -> {
                if (invocation == null) {
                    return null;
                }
                Long id = invocation.getArgumentAt(0, Long.class);
                return cacheStub.computeIfAbsent(id, key -> new ReentrantLock());
            };

            when(cache.get(anyLong())).thenAnswer(answer);
            return cache;
        }
    }

    private final static Map<Long, Lock> cacheStub = new ConcurrentHashMap<>();

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Test
    public void testCreate_ShouldThrowIllegalArgumentException_WhenInitAmountIsNull() throws Exception {
        ConcurrentCache<Long, Lock> cache = cacheBuilder.buildCache();
        AccountDao dao = mock(AccountDao.class);
        accountService = new AccountServiceImpl(dao, cache);
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("init amount amount is null");

        // create
        accountService.create(null);

        // check
        verifyZeroInteractions(cache);
        verifyZeroInteractions(dao);
    }

    @Test
    public void testCreate() throws Exception {
        ConcurrentCache<Long, Lock> cache = cacheBuilder.buildCache();
        AccountDao dao = mock(AccountDao.class);
        accountService = new AccountServiceImpl(dao, cache);

        // prepare mock
        BigDecimal amount = BigDecimal.TEN;
        Account account = mock(Account.class);
        when(dao.create(any())).thenReturn(account);

        // create
        Account result = accountService.create(amount);

        // check
        verifyZeroInteractions(cache);
        verifyZeroInteractions(account);

        Mockito.verify(dao, times(1)).create(any());
        verifyNoMoreInteractions(dao);

        assertTrue(account == result);
    }

    @Test
    public void testListAll() throws Exception {
        ConcurrentCache<Long, Lock> cache = cacheBuilder.buildCache();
        AccountDao dao = mock(AccountDao.class);
        accountService = new AccountServiceImpl(dao, cache);
        // prepare mock
        List<Account> accounts = new ArrayList<>();
        accounts.add(new Account(1L, BigDecimal.ONE));
        accounts.add(new Account(2L, BigDecimal.TEN));
        accounts.add(new Account(3L, BigDecimal.ZERO));

        when(dao.getAll()).thenReturn(accounts);

        // listAll
        List<Account> result = accountService.listAll();

        // check
        verifyZeroInteractions(cache);

        Mockito.verify(dao, times(1)).getAll();
        verifyNoMoreInteractions(dao);

        assertTrue(accounts == result);
    }

    @Test
    public void testGet_ShouldThrowIllegalArgumentException_WhenIdIsNull() throws Exception {
        ConcurrentCache<Long, Lock> cache = cacheBuilder.buildCache();
        AccountDao dao = mock(AccountDao.class);
        accountService = new AccountServiceImpl(dao, cache);
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("try to find account with null id");
        // get
        accountService.get(null);

        // check
        verifyZeroInteractions(cache);
        verifyZeroInteractions(dao);
    }

    @Test
    public void testGet_ShouldThrowNoSuchAccountException_WhenAccountWithSuchIdDoesNotExist() throws Exception {
        ConcurrentCache<Long, Lock> cache = cacheBuilder.buildCache();
        AccountDao dao = mock(AccountDao.class);
        accountService = new AccountServiceImpl(dao, cache);
        expectedEx.expect(NoSuchAccountException.class);
        expectedEx.expectMessage("There's no account with id: 2");

        // prepare mock
        long id = 2L;
        when(dao.findById(id)).thenReturn(Optional.empty());

        // get
        accountService.get(id);

        // check
        verifyZeroInteractions(cache);
        Mockito.verify(dao, times(1)).findById(id);
        verifyNoMoreInteractions(dao);
    }

    @Test
    public void testGet() throws Exception {
        ConcurrentCache<Long, Lock> cache = cacheBuilder.buildCache();
        AccountDao dao = mock(AccountDao.class);
        accountService = new AccountServiceImpl(dao, cache);
        // prepare mock
        long id = 1L;
        Account account = mock(Account.class);

        when(dao.findById(id)).thenReturn(Optional.of(account));

        // get
        Account result = accountService.get(id);

        // check
        verifyZeroInteractions(cache);
        verifyZeroInteractions(account);

        Mockito.verify(dao, times(1)).findById(1L);
        verifyNoMoreInteractions(dao);

        assertTrue(account == result);
    }

    @Test
    public void testWithdraw_ShouldThrowIllegalArgumentException_WhenIdIsNull() throws Exception {
        ConcurrentCache<Long, Lock> cache = cacheBuilder.buildCache();
        AccountDao dao = mock(AccountDao.class);
        accountService = new AccountServiceImpl(dao, cache);
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("try to modify account with null id");
        // withdraw
        accountService.withdraw(null, BigDecimal.TEN);
        // check
        verifyZeroInteractions(cache);
        verifyZeroInteractions(dao);
    }

    @Test
    public void testWithdraw_ShouldThrowIllegalArgumentException_WhenAmountIsNull() throws Exception {
        ConcurrentCache<Long, Lock> cache = cacheBuilder.buildCache();
        AccountDao dao = mock(AccountDao.class);
        accountService = new AccountServiceImpl(dao, cache);
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("amount is null");
        // withdraw
        accountService.withdraw(1L, null);
        // check
        verifyZeroInteractions(cache);
        verifyZeroInteractions(dao);
    }

    @Test
    public void testWithdraw_ShouldThrowIllegalArgumentException_WhenAmountIsNegative() throws Exception {
        ConcurrentCache<Long, Lock> cache = cacheBuilder.buildCache();
        AccountDao dao = mock(AccountDao.class);
        accountService = new AccountServiceImpl(dao, cache);
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("amount is non-positive");
        // withdraw
        accountService.withdraw(1L, new BigDecimal("-1.2"));
        // check
        verifyZeroInteractions(cache);
        verifyZeroInteractions(dao);
    }

    @Test
    public void testWithdraw_ShouldThrowIllegalArgumentException_WhenAmountIsZero() throws Exception {
        ConcurrentCache<Long, Lock> cache = cacheBuilder.buildCache();
        AccountDao dao = mock(AccountDao.class);
        accountService = new AccountServiceImpl(dao, cache);
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("amount is non-positive");
        // withdraw
        accountService.withdraw(1L, BigDecimal.ZERO);
        // check
        verifyZeroInteractions(cache);
        verifyZeroInteractions(dao);
    }

    @Test
    public void testWithdraw_ShouldThrowNoSuchAccountException_WhenAccountWithSuchIdDoesNotExist() throws Exception {
        ConcurrentCache<Long, Lock> cache = cacheBuilder.buildCache();
        AccountDao dao = mock(AccountDao.class);
        accountService = new AccountServiceImpl(dao, cache);
        expectedEx.expect(NoSuchAccountException.class);
        expectedEx.expectMessage("There's no account with id: 2");

        // prepare mock
        long id = 2L;
        when(dao.findById(id)).thenReturn(Optional.empty());

        // withdraw
        accountService.withdraw(id, BigDecimal.ONE);
        verifyZeroInteractions(cache);

        // check
        Mockito.verify(dao, times(1)).findById(id);
        verifyNoMoreInteractions(dao);
    }

    @Test
    public void testWithdraw_ShouldThrowInsufficientFundsException() throws Exception {
        ConcurrentCache<Long, Lock> cache = cacheBuilder.buildCache();
        AccountDao dao = mock(AccountDao.class);
        accountService = new AccountServiceImpl(dao, cache);
        expectedEx.expect(InsufficientFundsException.class);
        expectedEx.expectMessage("Current amount (0) is less than amount to withdraw (1) for the account with id = 1");

        // prepare mock
        long id = 1L;
        BigDecimal amount = BigDecimal.ONE;

        Account account = mock(Account.class);
        when(account.withdraw(amount)).thenThrow(new InsufficientFundsException(BigDecimal.ZERO, amount, id));
        when(dao.findById(id)).thenReturn(Optional.of(account));

        // withdraw
        accountService.withdraw(id, amount);

        // check
        Mockito.verify(cache, times(1)).get(id);

        Mockito.verify(account, times(1)).withdraw(amount);
        verifyNoMoreInteractions(account);

        Mockito.verify(dao, times(1)).findById(id);
        verifyNoMoreInteractions(dao);
    }

    @Test
    public void testWithdraw() throws Exception {
        ConcurrentCache<Long, Lock> cache = cacheBuilder.buildCache();
        AccountDao dao = mock(AccountDao.class);
        accountService = new AccountServiceImpl(dao, cache);
        // prepare mock
        long id = 1L;
        BigDecimal amount = BigDecimal.ONE;

        Account account = mock(Account.class);
        when(account.withdraw(amount)).thenReturn(account);
        when(dao.findById(id)).thenReturn(Optional.of(account));

        // withdraw
        Account result = accountService.withdraw(id, amount);

        // check
        Mockito.verify(cache, times(1)).get(id);

        Mockito.verify(account, times(1)).withdraw(amount);
        verifyNoMoreInteractions(account);

        Mockito.verify(dao, times(1)).findById(id);
        verifyNoMoreInteractions(dao);
        assertTrue(account == result);
    }


    @Test
    public void testDeposit_ShouldThrowIllegalArgumentException_WhenIdIsNull() throws Exception {
        ConcurrentCache<Long, Lock> cache = cacheBuilder.buildCache();
        AccountDao dao = mock(AccountDao.class);
        accountService = new AccountServiceImpl(dao, cache);
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("try to modify account with null id");
        // deposit
        accountService.deposit(null, BigDecimal.TEN);
        // check
        verifyZeroInteractions(cache);
        verifyZeroInteractions(dao);
    }

    @Test
    public void testDeposit_ShouldThrowIllegalArgumentException_WhenAmountIsNull() throws Exception {
        ConcurrentCache<Long, Lock> cache = cacheBuilder.buildCache();
        AccountDao dao = mock(AccountDao.class);
        accountService = new AccountServiceImpl(dao, cache);
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("amount is null");
        // deposit
        accountService.deposit(1L, null);
        // check
        verifyZeroInteractions(cache);
        verifyZeroInteractions(dao);
    }

    @Test
    public void testDeposit_ShouldThrowIllegalArgumentException_WhenAmountIsNegative() throws Exception {
        ConcurrentCache<Long, Lock> cache = cacheBuilder.buildCache();
        AccountDao dao = mock(AccountDao.class);
        accountService = new AccountServiceImpl(dao, cache);
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("amount is non-positive");
        // deposit
        accountService.deposit(1L, new BigDecimal("-1.2"));
        // check
        verifyZeroInteractions(cache);
        verifyZeroInteractions(dao);
    }

    @Test
    public void testDeposit_ShouldThrowIllegalArgumentException_WhenAmountIsZero() throws Exception {
        ConcurrentCache<Long, Lock> cache = cacheBuilder.buildCache();
        AccountDao dao = mock(AccountDao.class);
        accountService = new AccountServiceImpl(dao, cache);
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("amount is non-positive");
        // deposit
        accountService.deposit(1L, BigDecimal.ZERO);
        // check
        verifyZeroInteractions(cache);
        verifyZeroInteractions(dao);
    }

    @Test
    public void testDeposit_ShouldThrowNoSuchAccountException_WhenAccountWithSuchIdDoesNotExist() throws Exception {
        ConcurrentCache<Long, Lock> cache = cacheBuilder.buildCache();
        AccountDao dao = mock(AccountDao.class);
        accountService = new AccountServiceImpl(dao, cache);
        expectedEx.expect(NoSuchAccountException.class);
        expectedEx.expectMessage("There's no account with id: 2");

        // prepare mock
        long id = 2L;
        when(dao.findById(id)).thenReturn(Optional.empty());

        // deposit
        accountService.deposit(id, BigDecimal.ONE);
        verifyZeroInteractions(cache);

        // check
        Mockito.verify(dao, times(1)).findById(id);
        verifyNoMoreInteractions(dao);
    }

    @Test
    public void testDeposit() throws Exception {
        ConcurrentCache<Long, Lock> cache = cacheBuilder.buildCache();
        AccountDao dao = mock(AccountDao.class);
        accountService = new AccountServiceImpl(dao, cache);
        // prepare mock
        long id = 1L;
        BigDecimal amount = BigDecimal.ONE;

        Account account = mock(Account.class);
        when(account.deposit(amount)).thenReturn(account);
        when(dao.findById(id)).thenReturn(Optional.of(account));

        // deposit
        Account result = accountService.deposit(id, amount);

        // check
        Mockito.verify(cache, times(1)).get(id);

        Mockito.verify(account, times(1)).deposit(amount);
        verifyNoMoreInteractions(account);

        Mockito.verify(dao, times(1)).findById(id);
        verifyNoMoreInteractions(dao);
        assertTrue(account == result);
    }


    @Test
    public void testDelete_ShouldThrowIllegalArgumentException_WhenIdIsNull() throws Exception {
        ConcurrentCache<Long, Lock> cache = cacheBuilder.buildCache();
        AccountDao dao = mock(AccountDao.class);
        accountService = new AccountServiceImpl(dao, cache);
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("try to delete account with null id");
        //delete
        accountService.delete(null);
        // check
        verifyZeroInteractions(cache);
        verifyZeroInteractions(dao);
    }

    @Test
    public void testDelete_ShouldThrowNoSuchAccountException_WhenAccountWithSuchIdDoesNotExist() throws Exception {
        ConcurrentCache<Long, Lock> cache = cacheBuilder.buildCache();
        AccountDao dao = mock(AccountDao.class);
        accountService = new AccountServiceImpl(dao, cache);
        expectedEx.expect(NoSuchAccountException.class);
        expectedEx.expectMessage("There's no account with id: 2");

        // prepare mock
        long id = 2L;
        when(dao.findById(id)).thenReturn(Optional.empty());

        // delete
        accountService.delete(id);
        verifyZeroInteractions(cache);

        // check
        Mockito.verify(dao, times(1)).findById(id);
        verifyNoMoreInteractions(dao);
    }

    @Test
    public void testDelete() throws Exception {
        ConcurrentCache<Long, Lock> cache = cacheBuilder.buildCache();
        AccountDao dao = mock(AccountDao.class);
        accountService = new AccountServiceImpl(dao, cache);

        // prepare mock
        long id = 2L;
        Account account = mock(Account.class);
        when(dao.findById(id)).thenReturn(Optional.of(account));
        when(dao.delete(account)).thenReturn(account);

        // delete
        Account result = accountService.delete(id);

        // check
        Mockito.verify(cache, times(1)).get(id);
        verifyNoMoreInteractions(cache);

        Mockito.verify(dao, times(1)).findById(id);
        Mockito.verify(dao, times(1)).delete(account);
        verifyNoMoreInteractions(dao);

        assertTrue(result == account);
    }

    @Test
    public void TestTransfer_ShouldThrowIllegalArgumentException_WhenFirstIdIsNull() throws Exception {
        ConcurrentCache<Long, Lock> cache = cacheBuilder.buildCache();
        AccountDao dao = mock(AccountDao.class);
        accountService = new AccountServiceImpl(dao, cache);
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("try to modify account with null id");
        // transfer
        accountService.transfer(null, 2L, BigDecimal.TEN);
        // check
        verifyZeroInteractions(cache);
        verifyZeroInteractions(dao);
    }

    @Test
    public void TestTransfer_ShouldThrowIllegalArgumentException_WhenSecondIdIsNull() throws Exception {
        ConcurrentCache<Long, Lock> cache = cacheBuilder.buildCache();
        AccountDao dao = mock(AccountDao.class);
        accountService = new AccountServiceImpl(dao, cache);
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("try to modify account with null id");
        // transfer
        accountService.transfer(3L, null, BigDecimal.TEN);
        // check
        verifyZeroInteractions(cache);
        verifyZeroInteractions(dao);
    }

    @Test
    public void TestTransfer_ShouldThrowIllegalArgumentException_WhenAmountIsNull() throws Exception {
        ConcurrentCache<Long, Lock> cache = cacheBuilder.buildCache();
        AccountDao dao = mock(AccountDao.class);
        accountService = new AccountServiceImpl(dao, cache);
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("amount is null");
        // transfer
        accountService.transfer(3L, 2L, null);
        // check
        verifyZeroInteractions(cache);
        verifyZeroInteractions(dao);
    }

    @Test
    public void TestTransfer_ShouldThrowIllegalArgumentException_WhenAmountIsNegative() throws Exception {
        ConcurrentCache<Long, Lock> cache = cacheBuilder.buildCache();
        AccountDao dao = mock(AccountDao.class);
        accountService = new AccountServiceImpl(dao, cache);
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("amount is non-positive");
        // transfer
        accountService.transfer(3L, 2L, new BigDecimal("-10"));
        // check
        verifyZeroInteractions(cache);
        verifyZeroInteractions(dao);
    }

    @Test
    public void TestTransfer_ShouldThrowIllegalArgumentException_WhenAmountIsZero() throws Exception {
        ConcurrentCache<Long, Lock> cache = cacheBuilder.buildCache();
        AccountDao dao = mock(AccountDao.class);
        accountService = new AccountServiceImpl(dao, cache);
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("amount is non-positive");
        // transfer
        accountService.transfer(3L, 32L, BigDecimal.ZERO);
        // check
        verifyZeroInteractions(cache);
        verifyZeroInteractions(dao);
    }

    @Test
    public void TestTransfer_ShouldThrowTransferToTheSameAccountException_WhenIdsAreEqual() throws Exception {
        ConcurrentCache<Long, Lock> cache = cacheBuilder.buildCache();
        AccountDao dao = mock(AccountDao.class);
        accountService = new AccountServiceImpl(dao, cache);
        expectedEx.expect(TransferToTheSameAccountException.class);
        expectedEx.expectMessage("Transfer money to the same account is forbidden");
        // transfer
        accountService.transfer(3L, 3L, BigDecimal.TEN);
        // check
        verifyZeroInteractions(cache);
        verifyZeroInteractions(dao);
    }

    @Test
    public void TestTransfer_ShouldThrowNoSuchAccountException_WhenAccountWithFirstIdDoesNotExist() throws Exception {
        ConcurrentCache<Long, Lock> cache = cacheBuilder.buildCache();
        AccountDao dao = mock(AccountDao.class);
        accountService = new AccountServiceImpl(dao, cache);
        expectedEx.expect(NoSuchAccountException.class);
        expectedEx.expectMessage("There's no account with id: 2");

        // prepare mock
        long id = 2L;
        when(dao.findById(id)).thenReturn(Optional.empty());

        // transfer
        accountService.transfer(id, 3L, BigDecimal.ONE);

        // check
        verify(cache, times(1)).get(id);
        verify(cache, times(1)).get(3L);
        verifyNoMoreInteractions(cache);

        Mockito.verify(dao, times(1)).findById(id);
        verifyNoMoreInteractions(dao);
    }

    @Test
    public void TestTransfer_ShouldThrowNoSuchAccountException_WhenAccountWithSecondIdDoesNotExist() throws Exception {
        ConcurrentCache<Long, Lock> cache = cacheBuilder.buildCache();
        AccountDao dao = mock(AccountDao.class);
        accountService = new AccountServiceImpl(dao, cache);
        expectedEx.expect(NoSuchAccountException.class);
        expectedEx.expectMessage("There's no account with id: 3");

        // prepare mock
        long firstId = 2L;
        long secondId = 3L;
        Account account = mock(Account.class);
        when(dao.findById(firstId)).thenReturn(Optional.of(account));
        when(dao.findById(secondId)).thenReturn(Optional.empty());

        // transfer
        accountService.transfer(firstId, secondId, BigDecimal.ONE);

        // check
        verify(cache, times(1)).get(firstId);
        verify(cache, times(1)).get(secondId);
        verifyNoMoreInteractions(cache);

        Mockito.verify(dao, times(1)).findById(firstId);
        verifyNoMoreInteractions(dao);
    }

    @Test
    public void TestTransfer_ShouldThrowInsufficientFundsException_WhenItIsNotEnoughFundsForTransfer() throws Exception {
        ConcurrentCache<Long, Lock> cache = cacheBuilder.buildCache();
        AccountDao dao = mock(AccountDao.class);
        accountService = new AccountServiceImpl(dao, cache);
        expectedEx.expect(InsufficientFundsException.class);
        expectedEx.expectMessage("Current amount (0) is less than amount to withdraw (1) for the account with id = 2");

        // prepare mock
        long firstId = 2L;
        long secondId = 3L;
        Account accountFrom = mock(Account.class);
        Account accountTo = mock(Account.class);
        when(dao.findById(firstId)).thenReturn(Optional.of(accountFrom));
        when(dao.findById(secondId)).thenReturn(Optional.of(accountTo));
        BigDecimal amountToWithdraw = BigDecimal.ONE;
        when(accountFrom.withdraw(amountToWithdraw)).thenThrow(new InsufficientFundsException(BigDecimal.ZERO, BigDecimal.ONE, firstId));

        // transfer
        accountService.transfer(firstId, secondId, BigDecimal.ONE);

        // check
        verify(cache, times(1)).get(firstId);
        verify(cache, times(1)).get(secondId);
        verifyNoMoreInteractions(cache);

        verify(accountFrom.withdraw(amountToWithdraw), times(1));
        verifyNoMoreInteractions(accountFrom);
        verifyZeroInteractions(accountTo);

        Mockito.verify(dao, times(1)).findById(firstId);
        Mockito.verify(dao, times(1)).findById(secondId);
        verifyNoMoreInteractions(dao);
    }

    @Test
    public void TestTransfer() throws Exception {
        ConcurrentCache<Long, Lock> cache = cacheBuilder.buildCache();
        AccountDao dao = mock(AccountDao.class);
        accountService = new AccountServiceImpl(dao, cache);

        // prepare mock
        long firstId = 2L;
        long secondId = 3L;
        Account accountFrom = mock(Account.class);
        Account accountTo = mock(Account.class);
        when(dao.findById(firstId)).thenReturn(Optional.of(accountFrom));
        when(dao.findById(secondId)).thenReturn(Optional.of(accountTo));
        BigDecimal amountToWithdraw = BigDecimal.ONE;
        when(accountFrom.withdraw(amountToWithdraw)).thenReturn(accountFrom);
        when(accountTo.deposit(amountToWithdraw)).thenReturn(accountTo);

        // transfer
        Account result = accountService.transfer(firstId, secondId, BigDecimal.ONE);

        // check
        verify(cache, times(1)).get(firstId);
        verify(cache, times(1)).get(secondId);
        verifyNoMoreInteractions(cache);

        verify(accountFrom, times(1)).withdraw(amountToWithdraw);
        verifyNoMoreInteractions(accountFrom);
        verify(accountTo, times(1)).deposit(amountToWithdraw);
        verifyNoMoreInteractions(accountTo);

        Mockito.verify(dao, times(1)).findById(firstId);
        Mockito.verify(dao, times(1)).findById(secondId);
        verifyNoMoreInteractions(dao);

        assertTrue(result == accountFrom);
    }


}