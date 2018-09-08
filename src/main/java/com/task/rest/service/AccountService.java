package com.task.rest.service;

import com.task.rest.model.dbo.Account;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author Alexander Kotov (kotov.alex.22@gmail.com)
 */
public interface AccountService {
    Account create(Account account);

    List<Account> listAll();

    Account withdraw(Long id, BigDecimal amount);

    Account deposit(Long id, BigDecimal amount);

    Account transfer(Long fromId, Long toId, BigDecimal amount);

}
