package com.task.rest.service;

import com.task.rest.model.dao.AccountDao;
import com.task.rest.model.dbo.Account;

/**
 * @author Alexander Kotov (kotov.alex.22@gmail.com)
 */
public class AccountServiceImpl implements AccountService {
    private final AccountDao accountDao;

    public AccountServiceImpl(AccountDao accountDao) {
        this.accountDao = accountDao;
    }

    @Override
    public Long create(Account account) {
        return accountDao.insert(account).getId();
    }
}
