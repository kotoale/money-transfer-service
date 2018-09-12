package com.task.rest.service;

import com.task.rest.exceptions.NoSuchAccountException;
import com.task.rest.exceptions.TransferToTheSameAccountException;
import com.task.rest.model.dao.AccountDao;
import com.task.rest.model.dbo.Account;
import com.task.rest.utils.concurrency.ConcurrentCache;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.locks.Lock;

/**
 * Implementation of the {@link AccountService}
 *
 * @author Alexander Kotov (kotov.alex.22@gmail.com)
 * @see AccountService
 */
public class AccountServiceImpl implements AccountService {
    private final AccountDao accountDao;
    private final ConcurrentCache<Long, Lock> lockByIdCache;

    @Inject
    public AccountServiceImpl(AccountDao accountDao, ConcurrentCache<Long, Lock> lockByIdCache) {
        this.accountDao = accountDao;
        this.lockByIdCache = lockByIdCache;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Account create(Account account) {
        return accountDao.insert(account);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Account> listAll() {
        return accountDao.getAll();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Account get(Long id) {
        return accountDao.findById(id).orElseThrow(() -> new NoSuchAccountException(id));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Account withdraw(Long id, BigDecimal amount) {
        Lock lock = lockByIdCache.get(id);
        lock.lock();
        try {
            Account account = accountDao.findById(id).orElseThrow(() -> new NoSuchAccountException(id));
            account.withdraw(amount);
            accountDao.update(account);
            return account;
        } finally {
            lock.unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Account deposit(Long id, BigDecimal amount) {
        Lock lock = lockByIdCache.get(id);
        lock.lock();
        try {
            Account account = accountDao.findById(id).orElseThrow(() -> new NoSuchAccountException(id));
            account.deposit(amount);
            accountDao.update(account);
            return account;
        } finally {
            lock.unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Account delete(Long id) {
        Lock lock = lockByIdCache.get(id);
        lock.lock();
        try {
            Account account = accountDao.findById(id).orElseThrow(() -> new NoSuchAccountException(id));
            accountDao.delete(account);
            return account;
        } finally {
            lock.unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Account transfer(Long fromId, Long toId, BigDecimal amount) {
        if (fromId.equals(toId)) {
            throw new TransferToTheSameAccountException();
        }
        long firstId = Math.min(fromId, toId);
        long secondId = Math.max(fromId, toId);
        Lock firstLock = lockByIdCache.get(firstId);
        Lock secondLock = lockByIdCache.get(secondId);

        firstLock.lock();
        try {
            secondLock.lock();
            try {
                Account fromAccount = accountDao.findById(fromId).orElseThrow(() -> new NoSuchAccountException(fromId));
                Account toAccount = accountDao.findById(toId).orElseThrow(() -> new NoSuchAccountException(toId));
                fromAccount.withdraw(amount);
                toAccount.deposit(amount);
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
