package com.task.rest.model.dao;

import com.task.rest.model.dbo.Account;
import io.dropwizard.hibernate.AbstractDAO;
import org.hibernate.SessionFactory;

import java.util.List;
import java.util.Optional;

/**
 * @author Alexander Kotov (kotov.alex.22@gmail.com)
 */
public class AccountDao extends AbstractDAO<Account> {

    public AccountDao(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    @SuppressWarnings("unchecked")
    public List<Account> getAll() {
        return (List<Account>) currentSession().createQuery("from Account").list();
    }

    public Optional<Account> findById(Long id) {
        return Optional.ofNullable(get(id));
    }

    public void delete(Account account) {
        currentSession().delete(account);
    }

    public void update(Account account) {
        currentSession().saveOrUpdate(account);
    }

    public Account insert(Account account) {
        return persist(account);
    }
}
