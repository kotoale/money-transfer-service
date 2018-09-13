package com.task.rest.model.dbo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.task.rest.exceptions.InsufficientFundsException;
import io.dropwizard.jackson.Jackson;
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
    private Account account = new Account();

    private static final ObjectMapper MAPPER = Jackson.newObjectMapper();

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
    public void testDeposit_PositiveCase() throws Exception {
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

    @Test(expected = InsufficientFundsException.class)
    public void testWithdraw_ThrowsInsufficientFundsException_WhenTryToWithdrawAmountGreaterThanCurrentAmount() throws Exception {
        account.setAmount(new BigDecimal("1.01"));
        account.withdraw(new BigDecimal("1.02"));
    }

    @Test
    public void testWithdraw_PositiveCase() throws Exception {
        BigDecimal initAmount = new BigDecimal("140.06");
        account.setAmount(initAmount);
        BigDecimal amountToWithdraw = new BigDecimal("120.32");
        account.withdraw(amountToWithdraw);

        assertTrue(initAmount.subtract(amountToWithdraw).compareTo(account.getAmount()) == 0);
    }

}