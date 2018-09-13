package com.task.rest.model.dao;

import com.task.rest.model.dbo.Account;

import java.util.List;
import java.util.Optional;

/**
 * Data access (CRUD) interface for {@link Account} object
 *
 * @author Alexander Kotov (kotov.alex.22@gmail.com)
 */
public interface AccountDao {
    /**
     * @return list of all stored accounts
     */
    List<Account> getAll();

    /**
     * @param id id of the requested account
     * @return {@link Optional} {@link Account} object for the specified id
     * returns empty object if there is no account with specified id
     * @throws IllegalArgumentException if id is null
     */
    Optional<Account> findById(Long id);

    /**
     * Removes specified account from the storage
     *
     * @param account {@link Account} object to be deleted
     * @throws IllegalArgumentException if account is null
     * does nothing if account has null id or does not exist in the storage
     */
    Account delete(Account account);

    /**
     * Updates specified account in the storage
     *
     * @param account {@link Account} object to be updated
     * @return account
     * @throws IllegalArgumentException                        if account is null or it does not have id
     * @throws com.task.rest.exceptions.NoSuchAccountException when try to update not existed account
     */
    Account update(Account account);

    /**
     * Inserts specified account to the storage
     *
     * @param account {@link Account} object to be inserted
     * @return inserted {@link Account} object
     * @throws IllegalArgumentException if account is null or it does have id
     */
    Account create(Account account);
}
