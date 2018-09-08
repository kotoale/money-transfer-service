package com.task.rest.bootstrap;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.dropwizard.db.DataSourceFactory;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * @author Alexander Kotov (kotov.alex.22@gmail.com)
 */
public class AccountServiceConfiguration extends Configuration {

    @Valid
    @NotNull
    private DataSourceFactory database = new DataSourceFactory();

    @JsonProperty("database")
    public DataSourceFactory getDataSourceFactory() {
        return database;
    }

    @NotNull
    private BigDecimal initialMoneyAmount = BigDecimal.ZERO;

    @JsonProperty
    public BigDecimal getInitialMoneyAmount() {
        return initialMoneyAmount;
    }

    @JsonProperty
    public void setInitialMoneyAmount(BigDecimal initialMoneyAmount) {
        this.initialMoneyAmount = initialMoneyAmount;
    }

    @JsonProperty("database")
    public void setDataSourceFactory(DataSourceFactory database) {
        this.database = database;
    }
}
