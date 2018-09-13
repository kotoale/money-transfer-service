package com.task.rest.service;

import com.task.rest.model.dbo.Account;

import java.math.BigDecimal;
import java.util.List;

/**
 * Represents all operations available for execution via REST service
 *
 * @author Alexander Kotov (kotov.alex.22@gmail.com)
 */
public interface AccountService {
    /**
     * Creates new account in the service
     *
     * @param amount initial money amount
     * @return created account
     * @throws IllegalArgumentException if account amount is null
     */
    Account create(BigDecimal amount);

    /**
     * @param id requested account id
     * @return {@link Account} object associated with id
     * @throws IllegalArgumentException if id is null
     * @throws com.task.rest.exceptions.NoSuchAccountException if storage does not contain account with specified id
     */
    Account get(Long id);

    /**
     *
     * @return list of all accounts in the service
     */
    List<Account> listAll();

    /**
     * Withdraws amount from the specified account
     *
     * @param id specified account id
     * @param amount money value to be withdrawn from the account
     * @return updated {@link Account} object
     * @throws IllegalArgumentException if id is null
     * @throws IllegalArgumentException if amount is null or non-positive
     * @throws com.task.rest.exceptions.NoSuchAccountException if storage does not contain account with specified id
     * @throws com.task.rest.exceptions.InsufficientFundsException if account has insufficient funds for withdraw
     */
    Account withdraw(Long id, BigDecimal amount);

    /**
     * Deposits amount to the specified account
     *
     * @param id id specified account id
     * @param amount money value to be deposited to the account
     * @return updated {@link Account} object
     * @throws IllegalArgumentException if id is null
     * @throws IllegalArgumentException if amount is null or non-positive
     * @throws com.task.rest.exceptions.NoSuchAccountException if storage does not contain account with specified id
     */
    Account deposit(Long id, BigDecimal amount);

    /**
     * Transfers money from one specified account to another
     *
     * @param fromId id for the account transfer from
     * @param toId id for the account transfer to
     * @param amount value to be transferred
     * @return updated {@link Account} object from which money has been transferred
     * @throws IllegalArgumentException if fromId or toId is null
     * @throws IllegalArgumentException if amount is null or non-positive
     * @throws com.task.rest.exceptions.NoSuchAccountException if storage does not contain account with specified fromId or toId
     * @throws com.task.rest.exceptions.InsufficientFundsException if from account has insufficient funds for transfer
     * @throws com.task.rest.exceptions.TransferToTheSameAccountException if fromId.equals(toId)
     */
    Account transfer(Long fromId, Long toId, BigDecimal amount);

    /**
     * Removes account with the specified id from the storage
     *
     * @param id account id
     * @return deleted (previously associated) {@link Account} object with the specified id
     * @throws IllegalArgumentException if fromId or toId is null
     * @throws com.task.rest.exceptions.NoSuchAccountException if storage does not contain account with specified id
     */
    Account delete(Long id);

}
