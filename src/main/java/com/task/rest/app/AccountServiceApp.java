package com.task.rest.app;

import com.task.rest.bootstrap.AccountServiceConfiguration;
import com.task.rest.exceptions.DefaultExceptionsProvider;
import com.task.rest.exceptions.ServiceExceptionsProvider;
import com.task.rest.model.dao.AccountDao;
import com.task.rest.model.dbo.Account;
import com.task.rest.resource.AccountServiceResource;
import com.task.rest.service.AccountServiceImpl;
import io.dropwizard.Application;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.hibernate.HibernateBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

/**
 * @author Alexander Kotov (kotov.alex.22@gmail.com)
 */
public class AccountServiceApp extends Application<AccountServiceConfiguration> {

    private static final String APP_NAME = "account-rest-resource-app";

    public static void main(String[] args) throws Exception {
        new AccountServiceApp().run(args);
    }

    private final HibernateBundle<AccountServiceConfiguration> hibernate = new HibernateBundle<AccountServiceConfiguration>(Account.class) {
        @Override
        public DataSourceFactory getDataSourceFactory(AccountServiceConfiguration configuration) {
            return configuration.getDataSourceFactory();
        }
    };

    @Override
    public String getName() {
        return APP_NAME;
    }

    @Override
    public void initialize(Bootstrap<AccountServiceConfiguration> bootstrap) {
        bootstrap.addBundle(hibernate);
    }

    @Override
    public void run(AccountServiceConfiguration configuration,
                    Environment environment) {
        final AccountServiceResource service = new AccountServiceResource(new AccountServiceImpl(new AccountDao(hibernate.getSessionFactory())), configuration.getInitialMoneyAmount());
        environment.jersey().register(new ServiceExceptionsProvider());
        environment.jersey().register(new DefaultExceptionsProvider());
        environment.jersey().register(service);
    }

}
