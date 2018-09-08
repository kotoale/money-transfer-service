package com.task.rest.service;

import com.task.rest.exceptions.InsufficientFundsException;
import com.task.rest.exceptions.NoSuchAccountException;
import com.task.rest.exceptions.TransferToTheSameAccountException;
import com.task.rest.model.dao.AccountDao;
import com.task.rest.model.dbo.Account;
import com.task.rest.utils.concurrency.ConcurrentCache;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Alexander Kotov (kotov.alex.22@gmail.com)
 */
public class AccountServiceImpl implements AccountService {
    private final AccountDao accountDao;
    private final ConcurrentCache<Long, Lock> accountLocks = new ConcurrentCache<>(ReentrantLock::new);

    public AccountServiceImpl(AccountDao accountDao) {
        this.accountDao = accountDao;
    }

    @Override
    public Account create(Account account) {
        return accountDao.insert(account);
    }

    @Override
    public List<Account> listAll() {
        return accountDao.getAll();
    }

    @Override
    public Account withdraw(Long id, BigDecimal amount) {
        Lock lock = accountLocks.get(id);
        lock.lock();
        try {
            Account account = accountDao.findById(id).orElseThrow(() -> new NoSuchAccountException(id));
            BigDecimal currentAmount = account.getAmount();
            if (currentAmount.compareTo(amount) < 0) {
                throw new InsufficientFundsException(currentAmount, amount);
            }
            account.setAmount(currentAmount.subtract(amount));
            accountDao.update(account);
            return account;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Account deposit(Long id, BigDecimal amount) {
        Lock lock = accountLocks.get(id);
        lock.lock();
        try {
            Account account = accountDao.findById(id).orElseThrow(() -> new NoSuchAccountException(id));
            BigDecimal currentAmount = account.getAmount();
            account.setAmount(currentAmount.add(amount));
            accountDao.update(account);
            return account;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Account transfer(Long fromId, Long toId, BigDecimal amount) {
        long firstId = Math.min(fromId, toId);
        long secondId = Math.max(fromId, toId);
        if (firstId == secondId) {
            throw new TransferToTheSameAccountException();
        }
        Lock firstLock = accountLocks.get(firstId);
        Lock secondLock = accountLocks.get(secondId);

        firstLock.lock();
        try {
            secondLock.lock();
            try {
                Account fromAccount = accountDao.findById(fromId).orElseThrow(() -> new NoSuchAccountException(fromId));
                Account toAccount = accountDao.findById(toId).orElseThrow(() -> new NoSuchAccountException(toId));
                BigDecimal currentAmountFrom = fromAccount.getAmount();
                if (currentAmountFrom.compareTo(amount) < 0) {
                    throw new InsufficientFundsException(currentAmountFrom, amount);
                }
                fromAccount.setAmount(currentAmountFrom.subtract(amount));
                toAccount.setAmount(toAccount.getAmount().add(amount));
                accountDao.update(fromAccount);
                accountDao.update(toAccount);
                return fromAccount;
            } finally {
                secondLock.unlock();
            }
        } finally {
            firstLock.unlock();
        }

    }
}
