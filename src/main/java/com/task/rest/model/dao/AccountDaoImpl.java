package com.task.rest.model.dao;

import com.google.inject.Inject;
import com.task.rest.model.dbo.Account;
import io.dropwizard.hibernate.AbstractDAO;
import org.hibernate.SessionFactory;

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
    @SuppressWarnings("unchecked")
    @Override
    public List<Account> getAll() {
        return (List<Account>) currentSession().createQuery("from Account").list();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Account> findById(Long id) {
        return Optional.ofNullable(get(id));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(Account account) {
        currentSession().delete(account);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void update(Account account) {
        currentSession().saveOrUpdate(account);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Account insert(Account account) {
        return persist(account);
    }
}
