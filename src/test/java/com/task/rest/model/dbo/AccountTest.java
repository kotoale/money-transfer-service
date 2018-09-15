package com.task.rest.model.dbo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.task.rest.exceptions.InsufficientFundsException;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.testing.junit.DAOTestRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigDecimal;

import static io.dropwizard.testing.FixtureHelpers.fixture;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * @author Alexander Kotov (kotov.alex.22@gmail.com)
 */
public class AccountTest {
    private Account account = new Account(1L, BigDecimal.ZERO);

    private static final ObjectMapper MAPPER = Jackson.newObjectMapper();

    @Rule
    public DAOTestRule database = DAOTestRule.newBuilder().addEntityClass(Account.class).build();

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Test
    public void testSerializationToJSON() throws Exception {
        account = new Account(1L, new BigDecimal("100.00100000"));

        final String expected = MAPPER.writeValueAsString(
                MAPPER.readValue(fixture("fixtures/account.json"), Account.class));

        assertThat(MAPPER.writeValueAsString(account)).isEqualTo(expected);
    }

    @Test
    public void testDeserializationFromJSON() throws Exception {
        account = new Account(1L, new BigDecimal("100.00100000"));

        assertThat(MAPPER.readValue(fixture("fixtures/account.json"), Account.class))
                .isEqualToComparingFieldByField(account);
    }

    @Test
    public void testDeposit_ThrowsIllegalArgumentException_WhenTryToDepositNullAmount() throws Exception {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("amountToDeposit can't be null");
        account.deposit(null);
    }

    @Test
    public void testDeposit_ThrowsIllegalArgumentException_WhenTryToDepositNegativeAmount() throws Exception {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("amountToDeposit can't be negative or zero");
        account.deposit(new BigDecimal("-1"));
    }

    @Test
    public void testDeposit_ThrowsIllegalArgumentException_WhenTryToDepositZeroAmount() throws Exception {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("amountToDeposit can't be negative or zero");
        account.deposit(BigDecimal.ZERO);
    }

    @Test
    public void testDeposit() throws Exception {
        BigDecimal initAmount = new BigDecimal("120.06");
        account.setAmount(initAmount);
        BigDecimal amountToDeposit = new BigDecimal("140.32");
        account.deposit(amountToDeposit);

        assertTrue(initAmount.add(amountToDeposit).compareTo(account.getAmount()) == 0);
    }

    @Test
    public void testWithdraw_ThrowsIllegalArgumentException_WhenTryToWithdrawNullAmount() throws Exception {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("amountToWithdraw can't be null");
        account.withdraw(null);
    }

    @Test
    public void testWithdraw_ThrowsIllegalArgumentException_WhenTryToWithdrawNegativeAmount() throws Exception {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("amountToWithdraw can't be negative or zero");
        account.withdraw(new BigDecimal("-1"));
    }

    @Test
    public void testWithdraw_ThrowsIllegalArgumentException_WhenTryToWithdrawZeroAmount() throws Exception {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("amountToWithdraw can't be negative or zero");
        account.withdraw(BigDecimal.ZERO);
    }

    @Test
    public void testWithdraw_ThrowsInsufficientFundsException_WhenTryToWithdrawAmountGreaterThanCurrentAmount() throws Exception {
        expectedEx.expect(InsufficientFundsException.class);
        expectedEx.expectMessage("Current amount (1.01) is less than amount to withdraw (1.02) for the account with id = 1");
        account.setAmount(new BigDecimal("1.01"));
        account.withdraw(new BigDecimal("1.02"));
    }

    @Test
    public void testWithdraw() throws Exception {
        BigDecimal initAmount = new BigDecimal("140.06");
        account.setAmount(initAmount);
        BigDecimal amountToWithdraw = new BigDecimal("120.32");
        account.withdraw(amountToWithdraw);

        assertTrue(initAmount.subtract(amountToWithdraw).compareTo(account.getAmount()) == 0);
    }

    @Test
    public void testIdConstancy() throws Exception {
        expectedEx.expect(javax.persistence.PersistenceException.class);
        expectedEx.expectMessage("org.hibernate.HibernateException: identifier of an instance of com.task.rest.model.dbo.Account was altered from 1 to 2");
        database.inTransaction(() -> database.getSessionFactory().getCurrentSession().save(account));
        account.setId(2L);
        database.inTransaction(() -> database.getSessionFactory().getCurrentSession().update(account));
    }

    @Test(expected = javax.validation.ConstraintViolationException.class)
    public void testNotNullValidationFails_ForAmount_WhenSaveAccount() throws Exception {
        account = new Account(null);
        database.inTransaction(() -> database.getSessionFactory().getCurrentSession().save(account));
    }

    @Test(expected = javax.validation.ConstraintViolationException.class)
    public void testNotNullValidationFails_ForAmount_WhenUpdateAccount() throws Exception {
        database.inTransaction(() -> database.getSessionFactory().getCurrentSession().save(account));
        account.setAmount(null);
        database.inTransaction(() -> database.getSessionFactory().getCurrentSession().update(account));
    }

    @Test(expected = javax.validation.ConstraintViolationException.class)
    public void testAmountValidationFails_WhenSaveAccount() throws Exception {
        // numeric value out of bounds (<37 digits>.<8 digits> expected
        account = new Account(new BigDecimal("123.000000001"));
        database.inTransaction(() -> database.getSessionFactory().getCurrentSession().save(account));
    }

    @Test(expected = javax.validation.ConstraintViolationException.class)
    public void testAmountValidationFails_WhenUpdateAccount() throws Exception {
        database.inTransaction(() -> database.getSessionFactory().getCurrentSession().save(account));
        // numeric value out of bounds (<37 digits>.<8 digits> expected
        account.setAmount(new BigDecimal("123.000000001"));
        database.inTransaction(() -> database.getSessionFactory().getCurrentSession().update(account));
    }

    @Test
    public void testAmountValidationPasses_WhenSaveAccount() throws Exception {
        account = new Account(new BigDecimal("123.00000001"));
        database.inTransaction(() -> database.getSessionFactory().getCurrentSession().save(account));
        assertThat(database.getSessionFactory().getCurrentSession().get(Account.class, 1L)).isEqualToComparingFieldByField(account);
    }

    @Test
    public void testAmountValidationPasses_WhenUpdateAccount() throws Exception {
        account = new Account(1L, BigDecimal.ZERO);
        database.inTransaction(() -> database.getSessionFactory().getCurrentSession().save(account));
        account.setAmount(new BigDecimal("123.00000001"));
        database.inTransaction(() -> database.getSessionFactory().getCurrentSession().update(account));
        assertThat(database.getSessionFactory().getCurrentSession().get(Account.class, 1L)).isEqualToComparingFieldByField(account);
    }

}