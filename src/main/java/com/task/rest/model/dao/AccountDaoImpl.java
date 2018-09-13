package com.task.rest.model.dao;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.task.rest.exceptions.NoSuchAccountException;
import com.task.rest.model.dbo.Account;
import io.dropwizard.hibernate.AbstractDAO;
import org.hibernate.SessionFactory;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.Optional;

/**
 * Implementation for the {@link AccountDao}
 *
 * @author Alexander Kotov (kotov.alex.22@gmail.com)
 * @see AccountDao
 */
public class AccountDaoImpl extends AbstractDAO<Account> implements AccountDao {

    @Inject
    public AccountDaoImpl(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Account> getAll() {
        CriteriaBuilder builder = currentSession().getCriteriaBuilder();
        CriteriaQuery<Account> criteria = builder.createQuery(Account.class);
        Root<Account> contactRoot = criteria.from(Account.class);
        criteria.select(contactRoot);
        return currentSession().createQuery(criteria).getResultList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Account> findById(Long id) {
        Preconditions.checkArgument(id != null, "try to find account with null id");
        return Optional.ofNullable(get(id));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Account delete(Account account) {
        Preconditions.checkArgument(account != null, "try to delete null account");
        currentSession().delete(account);
        return account;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Account update(Account account) {
        Preconditions.checkArgument(account != null, "try to update null account");
        Preconditions.checkArgument(account.getId() != null, "try to update contract with null id");
        Account currentAccount = findById(account.getId()).orElseThrow(() -> new NoSuchAccountException(account.getId()));
        currentAccount.setAmount(account.getAmount());
        return account;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Account create(Account account) {
        Preconditions.checkArgument(account != null, "try to create null account");
        Preconditions.checkArgument(account.getId() == null, "account for insertion can't have id");
        currentSession().save(account);
        return account;
    }
}
