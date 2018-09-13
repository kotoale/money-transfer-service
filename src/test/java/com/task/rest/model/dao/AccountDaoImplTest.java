package com.task.rest.model.dao;

import com.task.rest.exceptions.NoSuchAccountException;
import com.task.rest.model.dbo.Account;
import io.dropwizard.testing.junit.DAOTestRule;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * @author Alexander Kotov (kotov.alex.22@gmail.com)
 */
public class AccountDaoImplTest {

    @Rule
    public DAOTestRule database = DAOTestRule.newBuilder().addEntityClass(Account.class).build();

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    private AccountDaoImpl accountDao;

    @Before
    public void setUp() {
        accountDao = new AccountDaoImpl(database.getSessionFactory());
    }

    @Test
    public void testGetAll_ShouldReturnEmptyList_WhenThereAreNoAccounts() throws Exception {
        CriteriaBuilder builder = database.getSessionFactory().getCurrentSession().getCriteriaBuilder();
        CriteriaQuery<Account> criteria = builder.createQuery(Account.class);
        Root<Account> contactRoot = criteria.from(Account.class);
        criteria.select(contactRoot);

        // pre-check
        assertThat(database.getSessionFactory().getCurrentSession().createQuery(criteria).getResultList()).isEmpty();

        // check
        assertThat(accountDao.getAll()).isEmpty();
    }

    @Test
    public void testGetAll() throws Exception {
        List<Account> accounts = new ArrayList<>();
        accounts.add(new Account(new BigDecimal("100.03")));
        accounts.add(new Account(new BigDecimal("190.07")));
        accounts.add(new Account(new BigDecimal("100.0345")));

        // create
        database.inTransaction(() -> accounts.forEach(acc -> accountDao.create(acc)));

        // check
        assertTrue(CollectionUtils.isEqualCollection(accounts, accountDao.getAll()));
    }

    @Test
    public void testFindById_ShouldThrowIllegalArgumentException_WhenIdIsNull() throws Exception {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("try to find account with null id");
        accountDao.findById(null);
    }

    @Test
    public void testFindById_ShouldReturnEmptyOptional_WhenIAccountDoesNotExist() throws Exception {
        // pre check
        assertThat(database.getSessionFactory().getCurrentSession().get(Account.class, 1L)).isNull();

        assertThat(accountDao.findById(1L).orElse(null)).isNull();
        assertThat(database.getSessionFactory().getCurrentSession().get(Account.class, 1L)).isNull();
    }

    @Test
    public void testFindById_ShouldReturnAccount_WhenItExists() throws Exception {
        // pre checks
        assertThat(database.getSessionFactory().getCurrentSession().get(Account.class, 1L)).isNull();

        // create
        Account account = new Account(new BigDecimal("100.03"));
        accountDao.create(account);

        assertThat(database.getSessionFactory().getCurrentSession().get(Account.class, 1L)).
                isEqualToComparingFieldByField(accountDao.findById(1L).orElse(null));
    }

    @Test
    public void testDelete_DoesNothing_WhenAccountDoesNotHaveIdOrWhenItDoesNotExist() throws Exception {
        // pre checks
        assertThat(database.getSessionFactory().getCurrentSession().get(Account.class, 1L)).isNull();

        accountDao.delete(new Account(BigDecimal.ZERO));
        accountDao.delete(new Account(1L, BigDecimal.ZERO));
        assertThat(database.getSessionFactory().getCurrentSession().get(Account.class, 1L)).isNull();
    }

    @Test
    public void testDelete_ShouldThrowIllegalArgumentException_WhenAccountIsNull() throws Exception {
        // pre checks
        assertThat(database.getSessionFactory().getCurrentSession().get(Account.class, 1L)).isNull();
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("try to delete null account");
        accountDao.delete(null);
    }

    @Test
    public void testDelete() throws Exception {
        // pre checks
        assertThat(database.getSessionFactory().getCurrentSession().get(Account.class, 1L)).isNull();

        // create
        Account account = new Account(new BigDecimal("100.03"));
        accountDao.create(account);

        // post create check
        assertThat(database.getSessionFactory().getCurrentSession().get(Account.class, 1L)).isEqualToComparingFieldByField(account);

        // delete
        accountDao.delete(account);

        // check
        assertThat(database.getSessionFactory().getCurrentSession().get(Account.class, 1L)).isNull();
    }

    @Test
    public void testUpdate_ShouldThrowIllegalArgumentException_WhenAccountIsNull() throws Exception {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("try to update null account");
        accountDao.update(null);
    }

    @Test
    public void testUpdate_ShouldThrowIllegalArgumentException_WhenAccountHasNoId() throws Exception {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("try to update contract with null id");
        accountDao.update(new Account(BigDecimal.TEN));
    }

    @Test
    public void testUpdate_ShouldThrowNoSuchAccountException_WhenAccountDoesNotExist() throws Exception {
        expectedEx.expect(NoSuchAccountException.class);
        expectedEx.expectMessage("There's no account with id: 1");
        accountDao.update(new Account(1L, BigDecimal.TEN));
    }

    @Test
    public void testUpdate() throws Exception {
        // create
        Account account = new Account(new BigDecimal("100.03"));
        accountDao.create(account);

        // update explicitly with account having the same id
        Account newAccount = new Account(1L, new BigDecimal("55"));
        accountDao.update(newAccount);

        // check
        assertThat(database.getSessionFactory().getCurrentSession().get(Account.class, 1L)).isEqualToComparingFieldByField(newAccount);
    }

    @Test
    public void testUpdate_ShouldUpdateContractImplicitly() throws Exception {
        // create
        Account account = new Account(BigDecimal.ZERO);
        accountDao.create(account);

        account.setAmount(BigDecimal.TEN);

        // check
        assertThat(database.getSessionFactory().getCurrentSession().get(Account.class, 1L)).isEqualToComparingFieldByField(account);
    }

    @Test
    public void testCrete_ShouldThrowIllegalArgumentException_WhenAccountIsNull() throws Exception {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("try to create null account");
        accountDao.create(null);
    }

    @Test
    public void testCrete_ShouldThrowIllegalArgumentException_WhenAccountHasId() throws Exception {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("account for insertion can't have id");

        Account account = new Account(1L, new BigDecimal("100.03"));
        accountDao.create(account);
    }

    @Test
    public void testCreate_ShouldCreateAccount_WithoutId() throws Exception {
        // pre check
        assertThat(database.getSessionFactory().getCurrentSession().get(Account.class, 1L)).isNull();

        // create
        Account account = new Account(new BigDecimal("100.03"));
        Account insertedAccount = accountDao.create(account);

        // check
        assertThat(account == insertedAccount);
        assertThat(database.getSessionFactory().getCurrentSession().get(Account.class, 1L)).isEqualToComparingFieldByField(account);
    }

}