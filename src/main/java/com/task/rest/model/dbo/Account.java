package com.task.rest.model.dbo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.task.rest.exceptions.InsufficientFundsException;
import com.task.rest.utils.serialization.BigDecimalSerializer;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * This class represents accounts - domain objects for the money transfer service
 *
 * @author Alexander Kotov (kotov.alex.22@gmail.com)
 */
@Entity
@Table(name = "ACCOUNT")
public class Account {

    public static final int PRECISION = 37;
    public static final int SCALE = 8;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "account_generator")
    @SequenceGenerator(name = "account_generator", sequenceName = "account_seq", allocationSize = 1)
    @Column(name = "id", updatable = false, nullable = false)
    @JsonProperty
    private Long id;

    @Column(name = "amount", nullable = false, precision = PRECISION, scale = SCALE)
    @NotNull
    @JsonProperty
    @Digits(integer = PRECISION, fraction = SCALE)
    @JsonSerialize(using = BigDecimalSerializer.class)
    private BigDecimal amount;


    public Long getId() {
        return id;
    }

    public Account setId(Long id) {
        this.id = id;
        return this;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Account setAmount(BigDecimal amount) {
        this.amount = amount;
        return this;
    }

    /**
     * Updates amount by adding amountToDeposit to the current value
     *
     * @param amountToDeposit amount to be deposited on the account
     * @return updated {@link Account} object
     */
    public Account deposit(BigDecimal amountToDeposit) {
        amount = amount.add(amountToDeposit);
        return this;
    }

    /**
     * Updates amount by subtracting amountToDeposit from to the current value
     *
     * @param amountToWithdraw amount to be withdrawn from the account
     * @return updated {@link Account} object
     * @throws InsufficientFundsException when current amount is less than amountToWithdraw
     * @see InsufficientFundsException
     */
    public Account withdraw(BigDecimal amountToWithdraw) {
        if (amount.compareTo(amountToWithdraw) < 0) {
            throw new InsufficientFundsException(amount, amountToWithdraw, id);
        }
        amount = amount.subtract(amountToWithdraw);
        return this;
    }

    public Account(BigDecimal amount) {
        this.amount = amount;
    }

    public Account() {
    }
}
