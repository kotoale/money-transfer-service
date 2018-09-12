package com.task.rest.app;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import com.task.rest.bootstrap.AccountServiceConfiguration;
import com.task.rest.exceptions.mappers.DefaultExceptionsProvider;
import com.task.rest.exceptions.mappers.ServiceExceptionsProvider;
import com.task.rest.model.dao.AccountDao;
import com.task.rest.model.dao.AccountDaoImpl;
import com.task.rest.model.dbo.Account;
import com.task.rest.resource.AccountServiceResource;
import com.task.rest.service.AccountService;
import com.task.rest.service.AccountServiceImpl;
import com.task.rest.utils.concurrency.ConcurrentCache;
import com.task.rest.utils.concurrency.HibernateConcurrentCache;
import io.dropwizard.Application;
import io.dropwizard.configuration.ResourceConfigurationSourceProvider;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.hibernate.HibernateBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.hibernate.SessionFactory;

import java.math.BigDecimal;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

import static com.task.rest.resource.AccountServiceResource.INIT_AMOUNT_FIELD_NAME;

/**
 * Main class of the Dropwizard application
 *
 * @author Alexander Kotov (kotov.alex.22@gmail.com)
 * @see io.dropwizard.Application
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
        bootstrap.setConfigurationSourceProvider(new ResourceConfigurationSourceProvider());
        bootstrap.addBundle(hibernate);
    }

    @Override
    public void run(AccountServiceConfiguration configuration,
                    Environment environment) {
        Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(new TypeLiteral<ConcurrentCache<Long, Lock>>() {
                }).to(new TypeLiteral<HibernateConcurrentCache<Long, Lock>>() {
                });
                bind(AccountService.class).to(AccountServiceImpl.class);
                bind(AccountDao.class).to(AccountDaoImpl.class);
                bind(BigDecimal.class).annotatedWith(Names.named(INIT_AMOUNT_FIELD_NAME)).toInstance(configuration.getInitialMoneyAmount());
                bind(AccountServiceResource.class);
            }

            @Provides
            SessionFactory provideSessionFactory() {
                return hibernate.getSessionFactory();
            }

            @Provides
            Supplier<Lock> provideSupplier() {
                return ReentrantLock::new;
            }
        });

        AccountServiceResource accountServiceResource = injector.getInstance(AccountServiceResource.class);

        environment.jersey().register(new ServiceExceptionsProvider());
        environment.jersey().register(new DefaultExceptionsProvider());
        environment.jersey().register(accountServiceResource);
    }

}
